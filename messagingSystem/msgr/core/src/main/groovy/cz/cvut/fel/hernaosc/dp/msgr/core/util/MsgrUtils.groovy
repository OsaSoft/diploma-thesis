package cz.cvut.fel.hernaosc.dp.msgr.core.util

import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.MessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto
import groovy.json.JsonSlurper

import java.util.function.Function

class MsgrUtils {
    static <T> T fromOptional(Optional<T> optional) {
        optional.present ? optional.get() : null
    }

    static <K, V> Map<K, ?> flattenMap(Map<K, ?> map, String separator = '_', Function<?, V> valueProcessor = {
        it.toString()
    }) {
        map.collectEntries { k, v ->
            switch (v) {
                case Map:
                    flattenMap(v, separator, valueProcessor).collectEntries { q, r ->
                        [(k + separator + q): r]
                    }
                    break
                case Collection:
                    int i = 0
                    flattenCollection(v.flatten(), valueProcessor).collectEntries {
                        [(k + separator + i++): it]
                    }
                    break
                default:
                    [(k): valueProcessor.apply(v)]
                    break
            }
        } as Map<K, V>
    }

    static <T, V> Collection flattenCollection(Collection<T> col, Function<?, V> valueProcessor = { it.toString() }) {
        col.collect { elem ->
            switch (elem) {
                case Collection:
                    flattenCollection(elem, valueProcessor)
                    break
                default:
                    valueProcessor.apply(elem)
                    break
            }
        }
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
