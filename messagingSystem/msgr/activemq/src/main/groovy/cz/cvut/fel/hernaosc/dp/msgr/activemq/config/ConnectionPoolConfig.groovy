package cz.cvut.fel.hernaosc.dp.msgr.activemq.config

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.pool.PooledConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConnectionPoolConfig {

    @Bean(name = "pooledConnectionFactory")
    PooledConnectionFactory pooledConnectionFactory() {
        def pooledFactory = new PooledConnectionFactory(new ActiveMQConnectionFactory())
        pooledFactory.maxConnections = 1
        pooledFactory
    }
}
