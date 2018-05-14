package cz.cvut.fel.hernaosc.dp.msgr.activemq.mq

import cz.cvut.fel.hernaosc.dp.msgr.core.mq.ISender
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class ActiveMqSender implements ISender<String, String> {

    @Autowired
    private JmsTemplate jmsTemplate

    @PostConstruct
    void init() {
        jmsTemplate.with {
            pubSubDomain = true
            deliveryPersistent = true
        }
    }

    @Override
    void send(List<String> topics, String payload) {
        GParsPool.withPool {
            topics.eachParallel { String topic ->
                jmsTemplate.convertAndSend(topic, payload)
            }
        }
    }
}
