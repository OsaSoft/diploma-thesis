package cz.cvut.fel.hernaosc.dp.msgr.activemq.config

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.pool.PooledConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConnectionPoolConfig {

    @Value('${msgr.activemq.max.connections:#{1}}')
    private int maxConnections = 1

    @Bean(name = "pooledConnectionFactory")
    PooledConnectionFactory pooledConnectionFactory() {
        def pooledFactory = new PooledConnectionFactory(new ActiveMQConnectionFactory())
        pooledFactory.maxConnections = maxConnections
        pooledFactory
    }
}
