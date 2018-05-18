package cz.cvut.fel.hernaosc.dp.msgr.activemq.mq

import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IMqStats
import org.apache.activemq.management.JMSStatsImpl
import org.apache.activemq.pool.PooledConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ActiveMqStats implements IMqStats {

    @Autowired
    private PooledConnectionFactory connectionFactory

    @Override
    def getStats() {
        JMSStatsImpl mqStats = connectionFactory.connectionFactory.stats
        def connections = mqStats.connections
        def sessions = mqStats.connections*.sessions.flatten()
        [totalConnections: connections.size(), sessions: sessions.size(), origins: sessions*.consumers*.origin.flatten()]
    }
}