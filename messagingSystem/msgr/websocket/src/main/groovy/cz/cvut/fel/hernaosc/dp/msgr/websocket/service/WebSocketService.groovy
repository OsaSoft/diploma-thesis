package cz.cvut.fel.hernaosc.dp.msgr.websocket.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.IPlatformAdapter
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IMessagingService
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.consts.StatusCodes
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.dto.message.MessageDto
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.dto.message.NotificationDto
import cz.cvut.fel.hernaosc.dp.msgr.websocket.util.JsonMessage
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

import java.util.concurrent.ConcurrentHashMap

@Service
@Slf4j
class WebSocketService extends TextWebSocketHandler implements IPlatformAdapter {

    @Autowired
    private IMessagingService messagingService

    //maps deviceID to session
    private Map<String, WebSocketSession> sessions = [:] as ConcurrentHashMap

    @Override
    void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.debug "Websocket connection session '${session?.id}' closed with status '$status.code:$status.reason'"

        sessions.remove(getDeviceId(session))
    }

    @Override
    void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error "Transport error on websocket session '${session?.id}', reason: $exception.message"
        log.trace "$exception.message", exception
    }

    @Override
    void afterConnectionEstablished(WebSocketSession session) throws Exception {
        def deviceId = getDeviceId(session)
        sessions[deviceId] = session

        log.debug "Websocket connection established. Session id: '$session.id'. Device id: '$deviceId'"

        sendWsMessage session, [status: "CONNECTED", code: StatusCodes.WS_CONNECTED]
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        log.debug "Message received in session '${session?.id}': $textMessage.payload"

        try {
            def json = new JsonSlurper().parseText(textMessage.payload)

            def message = json.notification ? new NotificationDto(json.payload) : new DataMessageDto(json.payload)

            ["Groups", "Users", "Devices"].each { type ->
                if (message."target$type") {
                    forwardMessage(message, type)
                }
            }
        } catch (Exception ex) {
            log.error "Error parsing received message in session '${session?.id}': $textMessage.payload", ex

            switch (ex) {
                case JsonException:
                    sendWsMessage session, [status: "ERROR", code: StatusCodes.WS_BAD_MESSAGE, message: ex.message]
                    break
                default:
                    sendWsMessage session, [status: "ERROR", code: StatusCodes.WS_ERROR_SEND]
                    break
            }
        }
    }

    @Override
    boolean sendNotification(String title, String body, IDevice device) {
        log.debug "Sending notification to device $device"

        def session = sessions[device.id]

        if (!session) return false

        sendWsMessage session, [title: title, body: body, notification: true]
    }

    @Override
    boolean sendMessage(Map payload, IDevice device) {
        log.debug "Sending message to device $device"

        def session = sessions[device.id]

        if (!session) return false

        sendWsMessage session, [payoad: payload, notification: false]
    }

    private boolean forwardMessage(MessageDto message, String type) {
        if (message instanceof NotificationDto) {
            messagingService."sendNotification$type"(message.title, message.body, message."target$type")
        } else {
            messagingService."sendMessage$type" message.targetGroups, message.content
        }
    }

    private boolean sendWsMessage(WebSocketSession session, Map content) {
        boolean success = false
        try {
            session.sendMessage(JsonMessage.create(content))
            success = true
        } catch (Exception ex) {
            log.error "Exception sending message to session '${session?.id}'. Content: '$content'", ex
            try {
                session.close(CloseStatus.PROTOCOL_ERROR)
            } catch (IOException ex2) {
                log.error "Exception closing session '${session?.id}'", ex2
            }
        }

        success
    }

    private String getDeviceId(WebSocketSession session) {
        //path example /ws/device123
        session.uri.path.split("/")[2]
    }
}
