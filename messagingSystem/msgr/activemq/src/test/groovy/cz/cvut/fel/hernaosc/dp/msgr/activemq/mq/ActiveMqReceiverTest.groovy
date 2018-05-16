package cz.cvut.fel.hernaosc.dp.msgr.activemq.mq

import spock.lang.Specification
import spock.lang.Unroll

class ActiveMqReceiverTest extends Specification {
    ActiveMqReceiver receiver = new ActiveMqReceiver()

    void setup() {
    }

    void cleanup() {
    }

    @Unroll
    def "converts specific topic to general (#topic => #result)"() {
        expect:
            receiver.convertToGenericTopic(topic) == result

        where:
            topic                 | result
            "websocket.device123" | "websocket.device123"
            "FCM"                 | "FCM"
            "q.user.user123"      | "q.user.*"
            "q.group.group42"     | "q.group.*"
    }
}
