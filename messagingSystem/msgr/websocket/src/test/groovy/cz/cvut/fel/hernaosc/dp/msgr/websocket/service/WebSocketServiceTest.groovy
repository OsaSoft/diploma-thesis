package cz.cvut.fel.hernaosc.dp.msgr.websocket.service

import cz.cvut.fel.hernaosc.dp.msgr.core.service.IMessagingService
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.dto.message.NotificationDto
import groovy.json.JsonBuilder
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import spock.lang.Specification
import spock.lang.Unroll

class WebSocketServiceTest extends Specification {

    WebSocketService webSocketService = new WebSocketService()

    void setup() {
    }

    void cleanup() {
    }

    def "When connection is established, session is mapped to device and connection message is sent"() {
        given:
            def deviceId = "device-test"
            def session = Mock(WebSocketSession) {
                getId() >> "test"
                getUri() >> new URI("/ws/$deviceId")
            }

        when:
            webSocketService.afterConnectionEstablished(session)
        then:
            webSocketService.sessions[deviceId] == session
        and:
            1 * session.sendMessage(_)
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
            def messagingService = webSocketService.messagingService = Mock(IMessagingService)
        and:
            def textMessage = new TextMessage(new JsonBuilder([notification: notification, payload: message]).toString())
        and:
            webSocketService.sessions[deviceId] = session

        when:
            webSocketService.handleTextMessage(session, textMessage)
        then:
            if (notification) {
                1 * messagingService.sendNotificationGroups(message.title, message.body, message.targetGroups)
            } else {
                1 * messagingService.sendMessageGroups(message.targetGroups, message.content)
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
