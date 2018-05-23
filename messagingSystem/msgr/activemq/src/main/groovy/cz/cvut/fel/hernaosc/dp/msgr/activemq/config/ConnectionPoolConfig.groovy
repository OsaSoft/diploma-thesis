package cz.cvut.fel.hernaosc.dp.msgr.activemq.config

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.pool.PooledConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConnectionPoolConfig {

    @Value('${spring.activemq.broker-url:#{null}}')
    private String brokerUrl = null

    @Value('${msgr.activemq.max.connections:#{1}}')
    private int maxConnections = 1

    @Bean(name = "pooledConnectionFactory")
    PooledConnectionFactory pooledConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = brokerUrl ? new ActiveMQConnectionFactory(brokerUrl) : new ActiveMQConnectionFactory()
        def pooledFactory = new PooledConnectionFactory(activeMQConnectionFactory)
        pooledFactory.maxConnections = maxConnections
        pooledFactory
    }
}
