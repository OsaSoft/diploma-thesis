package cz.cvut.fel.hernaosc.dp.msgr.activemq.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate

import javax.jms.ConnectionFactory

@Configuration
class ActiveMqConfig {

    @Autowired
    ConnectionFactory connectionFactory

    @Bean
    JmsTemplate queueJmsTemplate() {
        def template = new JmsTemplate(connectionFactory)
        template.with {
            pubSubDomain = false
            deliveryPersistent = true
        }
        template
    }

    @Bean
    JmsTemplate topicJmsTemplate() {
        def template = new JmsTemplate(connectionFactory)
        template.with {
            pubSubDomain = true
            deliveryPersistent = true
        }
        template
    }
}
