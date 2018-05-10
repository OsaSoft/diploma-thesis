package cz.cvut.fel.hernaosc.dp.msgr.websocket.util

import groovy.json.JsonOutput
import org.springframework.web.socket.TextMessage

class JsonMessage {
    static TextMessage create(payload) {
        new TextMessage(JsonOutput.toJson(payload))
    }
}
