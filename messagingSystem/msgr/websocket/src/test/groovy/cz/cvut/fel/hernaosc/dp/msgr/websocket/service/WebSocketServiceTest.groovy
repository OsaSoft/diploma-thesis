package cz.cvut.fel.hernaosc.dp.msgr.websocket.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IReceiver
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.ISender
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IEntityService
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IMessagingService
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto
import groovy.json.JsonBuilder
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import spock.lang.Specification
import spock.lang.Unroll

class WebSocketServiceTest extends Specification {

    WebSocketService webSocketService = new WebSocketService()

    void setup() {
        webSocketService.deviceRepository = Mock(IDeviceRepository)
        webSocketService.messagingService = Mock(IMessagingService)
        webSocketService.entityService = Mock(IEntityService)
        webSocketService.mqReceiver = Mock(IReceiver)
        webSocketService.mqSender = Mock(ISender)
    }

    void cleanup() {
    }

    def "Established connection is handled"() {
        given:
            def deviceId = "device-test"
            def session = Mock(WebSocketSession) {
                getId() >> "test"
                getUri() >> new URI("/ws/$deviceId")
            }

        when: "Connection is established"
            webSocketService.afterConnectionEstablished(session)
        then: "session is mapped to device"
            webSocketService.sessions[deviceId] == session
        and: "connection message is sent"
            1 * session.sendMessage(_)
        and: "mq is subscribed"
            1 * webSocketService.mqReceiver.subscribe(["$webSocketService.platformQueueName.$deviceId"], _)
    }

    def "When connection is closed, session is removed from device map"() {
        given:
            def deviceId = "device-test"
            def session = Mock(WebSocketSession) {
                getId() >> "test"
                getUri() >> new URI("/ws/$deviceId")
            }
        and:
            webSocketService.sessions[deviceId] = session

        when:
            webSocketService.afterConnectionClosed(session, CloseStatus.NORMAL)
        then:
            !webSocketService.sessions[deviceId]
    }

    @Unroll
    def "Parses incoming message (notification=#notification)"() {
        given:
            def deviceId = "device-test"
            def session = Mock(WebSocketSession) {
                getId() >> "test"
                getUri() >> new URI("/ws/$deviceId")
            }
        and:
            def messagingService = webSocketService.messagingService
        and:
            def textMessage = new TextMessage(new JsonBuilder([notification: notification, payload: message]).toString())
        and:
            webSocketService.sessions[deviceId] = session

        when:
            webSocketService.handleTextMessage(session, textMessage)
        then:
            if (notification) {
                1 * messagingService.sendNotificationGroupsIds(message.title, message.body, message.targetGroups)
            } else {
                1 * messagingService.sendMessageGroupsIds(message.targetGroups, message.content)
            }

        where:
            notification | message
            true         | new NotificationDto(title: "test title", body: "test body", targetGroups: ["group1"])
            false        | new DataMessageDto(content: [foo: "bar", test: 1], targetGroups: ["group1"])
    }

    @Unroll
    def "Received bad message sends error"() {
        given:
            def deviceId = "device-test"
            def session = Mock(WebSocketSession) {
                getId() >> "test"
                getUri() >> new URI("/ws/$deviceId")
            }
        and:
            def textMessage = new TextMessage(message)
        and:
            webSocketService.sessions[deviceId] = session

        when:
            webSocketService.handleTextMessage(session, textMessage)
        then:
            1 * session.sendMessage(_)

        where:
            message << ["bad message", '{"notification":true,"title":"test","body":"test"}']
    }
}
