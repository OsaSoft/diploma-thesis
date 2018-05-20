package cz.cvut.fel.hernaosc.dp.msgr.websocket.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IReceiver
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.ISender
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.IPlatformAdapter
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.PlatformAdapter
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IEntityService
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IMessagingService
import cz.cvut.fel.hernaosc.dp.msgr.core.util.MsgrUtils
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.MessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.consts.StatusCodes
import cz.cvut.fel.hernaosc.dp.msgr.websocket.util.JsonMessage
import groovy.json.JsonException
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

import javax.annotation.PostConstruct
import java.util.concurrent.ConcurrentHashMap

@Service
@PlatformAdapter("websocket")
@Slf4j
class WebSocketService extends TextWebSocketHandler implements IPlatformAdapter {

    static final PLATFORM_NAME = "websocket"

    @Autowired
    private IDeviceRepository deviceRepository

    @Autowired
    private IMessagingService messagingService

    @Autowired
    private IEntityService entityService

    @Autowired
    private IReceiver<String, String> mqReceiver

    @Autowired
    private ISender<String, String> mqSender

    //maps deviceID to session
    private Map<String, WebSocketSession> sessions = [:] as ConcurrentHashMap
    private localDevices

    @PostConstruct
    void init() {
        entityService.findOrCreateByName(PLATFORM_NAME, IPlatform, [stateless: false])
    }

    @Override
    void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.debug "Websocket connection session '${session?.id}' closed with status '$status.code:$status.reason'"

        def deviceId = getDeviceId(session)
        sessions.remove(deviceId)
        mqReceiver.unsubscribe(["${platformQueueName}.$deviceId"])
    }

    @Override
    void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error "Transport error on websocket session '${session?.id}', reason: $exception.message"
        log.trace "$exception.message", exception
    }

    private onMessageForDevice = { String topic, String messageText ->
        def message = MsgrUtils.parseMessageFromJson(messageText)
        def deviceId = topic - "${platformQueueName}."

        message instanceof NotificationDto ? sendNotification(message.title, message.body, deviceId) : sendMessage(message.content, deviceId)
    }

    @Override
    void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //TODO: Possible further improvement. Check number of bound sessions on node and available mq connections and if over threshold tell client to connect to other node

        def deviceId = getDeviceId(session)
        sessions[deviceId] = session

        log.debug "Websocket connection established. Session id: '$session.id'. Device id: '$deviceId'"

        sendWsMessage(session, [status: "CONNECTED", code: StatusCodes.WS_CONNECTED])
        mqReceiver.subscribe(["$platformQueueName.$deviceId"], onMessageForDevice)
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        log.debug "Message received in session '${session?.id}': $textMessage.payload"

        try {
            def message = MsgrUtils.parseMessageFromJson(textMessage.payload)

            //check if any device is connected to this node
            def localDevices = []
            GParsPool.withPool {
                localDevices = message.targetDevices?.findAllParallel { sessions.containsKey(it) }
            }

            message.targetDevices?.removeAll(localDevices)

            Thread.start {
                GParsPool.withPool {
                    localDevices.eachParallel {
                        message instanceof NotificationDto ? sendNotification(message.title, message.body, it) : sendMessage(message.content, it)
                    }
                }
            }

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
        sendNotification(title, body, device.id)
    }

    boolean sendNotification(String title, String body, String deviceId) {
        log.debug "Sending notification to device $deviceId"

        def session = sessions[deviceId]

        if (!session) {
            log.debug "Device $deviceId is not connected to this Node."
            return true
        }

        sendWsMessage(session, [code: StatusCodes.WS_RECEIVED, title: title, body: body, notification: true])
    }

    @Override
    boolean sendMessage(Map payload, IDevice device) {
        sendMessage(payload, device.id)
    }

    boolean sendMessage(Map payload, String deviceId) {
        log.debug "Sending message to device $deviceId"

        def session = sessions[deviceId]

        if (!session) {
            log.debug "Device $deviceId is not connected to this Node."
            return true
        }

        sendWsMessage(session, [code: StatusCodes.WS_RECEIVED, payload: payload, notification: false])
    }

    private boolean forwardMessage(MessageDto message, String type) {
        if (message instanceof NotificationDto) {
            messagingService."sendNotification${type}Ids"(message.title, message.body, message."target$type")
        } else {
            messagingService."sendMessage${type}Ids"(message."target$type", message.content)
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

    String getPlatformQueueName() {
        PLATFORM_NAME
    }

    @Override
    long getBoundClientsNum() {
        sessions.size()
    }
}
