package cz.cvut.fel.hernaosc.dp.msgr.core.util

import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto
import groovy.json.JsonBuilder
import spock.lang.Specification

class MsgrUtilsTest extends Specification {
    void setup() {
    }

    void cleanup() {
    }

    def "Parses notification"() {
        given:
            def notification = new NotificationDto(
                    title: "test",
                    body: "test body",
                    targetDevices: (1..3).collect { it as String },
                    targetUsers: (4..7).collect { it as String },
                    targetGroups: (8..10).collect { it as String },
            )
        and:
            def jsonMessage = new JsonBuilder([notification: true, payload: notification]).toString()

        when:
            def message = (NotificationDto) MsgrUtils.parseMessageFromJson(jsonMessage)
        then:
            message.title == notification.title
            message.body == notification.body
            message.targetDevices.containsAll(notification.targetDevices)
            message.targetUsers.containsAll(notification.targetUsers)
            message.targetGroups.containsAll(notification.targetGroups)
    }

    def "Parses data message"() {
        given:
            def dataMessage = new DataMessageDto(
                    content: [foo: "bar", test: "test"],
                    targetDevices: (1..3).collect { it as String },
                    targetUsers: (4..7).collect { it as String },
                    targetGroups: (8..10).collect { it as String },
            )
        and:
            def jsonMessage = new JsonBuilder([notification: false, payload: dataMessage]).toString()

        when:
            def message = (DataMessageDto) MsgrUtils.parseMessageFromJson(jsonMessage)
        then:
            dataMessage.content.keySet().every { message.content.containsKey(it) }
            message.targetDevices.containsAll(dataMessage.targetDevices)
            message.targetUsers.containsAll(dataMessage.targetUsers)
            message.targetGroups.containsAll(dataMessage.targetGroups)
    }
}
