package cz.cvut.fel.hernaosc.dp.msgr.activemq.mq

import cz.cvut.fel.hernaosc.dp.msgr.core.dto.MqStatsDto
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IMqStats
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.management.JMSStatsImpl
import org.apache.activemq.pool.PooledConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ActiveMqStats implements IMqStats {

    @Autowired
    private PooledConnectionFactory connectionFactory

    @Override
    MqStatsDto getStats() {
        JMSStatsImpl mqStats = (connectionFactory.connectionFactory as ActiveMQConnectionFactory).stats as JMSStatsImpl
        def connections = mqStats.connections
        def sessions = mqStats.connections*.sessions.flatten()

        new MqStatsDto(totalConnections: connections.size(), sessions: sessions.size(), origins: sessions*.consumers*.origin.flatten())
    }
}
