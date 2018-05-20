package cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.util

import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.MessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto
import groovy.json.JsonSlurper

class MsgrMessageUtils {
    static <E> E randomElement(List<E> elements) {
        elements[new Random().nextInt(elements.size())]
    }

    static MessageDto parseMessageFromJson(String messageText) {
        def json = new JsonSlurper().parseText(messageText)
        def payload = json.payload ?: json
        def message

        if (json.notification) {
            message = new NotificationDto(payload)
        } else {
            message = new DataMessageDto(payload)
        }

        message
    }
}
