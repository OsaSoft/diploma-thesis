package cz.cvut.fel.hernaosc.dp.msgr.activemq.mq

import cz.cvut.fel.hernaosc.dp.msgr.core.mq.ISender
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
@Slf4j
class ActiveMqSender implements ISender<String, String> {

    @Autowired
    private JmsTemplate queueJmsTemplate

    @Autowired
    private JmsTemplate topicJmsTemplate

    @Override
    void send(List<String> topics, String payload, boolean isQueue = false) {
        log.debug "Sending message to ${isQueue ? 'queues' : 'topics'} '$topics'. Payload: $payload"

        GParsPool.withPool {
            topics.eachParallel { String topic ->
                isQueue ? queueJmsTemplate.convertAndSend(topic, payload) : topicJmsTemplate.convertAndSend(topic, payload)
            }
        }
    }
}
