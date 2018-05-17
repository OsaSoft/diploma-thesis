package cz.cvut.fel.hernaosc.dp.msgr.activemq.config

import org.apache.activemq.pool.PooledConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate

@Configuration
class JmsTemplateConfig {
    @Autowired
    PooledConnectionFactory pooledConnectionFactory

    @Bean
    JmsTemplate queueJmsTemplate() {
        def template = new JmsTemplate(pooledConnectionFactory)
        template.with {
            pubSubDomain = false
            deliveryPersistent = true
        }
        template
    }

    @Bean
    JmsTemplate topicJmsTemplate() {
        def template = new JmsTemplate(pooledConnectionFactory)
        template.with {
            pubSubDomain = true
            deliveryPersistent = true
        }
        template
    }
}
