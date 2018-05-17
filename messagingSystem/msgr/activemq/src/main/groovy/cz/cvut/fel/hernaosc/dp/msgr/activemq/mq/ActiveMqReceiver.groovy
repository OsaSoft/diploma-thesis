package cz.cvut.fel.hernaosc.dp.msgr.activemq.mq

import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IReceiver
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.apache.activemq.pool.PooledConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.listener.AbstractMessageListenerContainer
import org.springframework.jms.listener.DefaultMessageListenerContainer
import org.springframework.stereotype.Component

import javax.jms.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer

@Component
@Slf4j
class ActiveMqReceiver implements IReceiver<String, String> {

    @Autowired
    private PooledConnectionFactory pooledConnectionFactory

    private Map<String, AbstractMessageListenerContainer> containers = [:] as ConcurrentHashMap

    private Map<String, BiConsumer<String, String>> listeners = [:] as ConcurrentHashMap

    @Override
    void subscribe(List<String> topics, boolean isQueue = false, BiConsumer<String, String> listener) {
        GParsPool.withPool {
            topics.eachParallel { topic ->
                listeners[topic] = listener
                startListening(topic, new ActiveMqTopicListener(isQueue: isQueue, messageReceiver: this))
            }
        }
    }

    @Override
    void unsubscribe(List<String> topics) {
        GParsPool.withPool {
            topics.eachParallel { topic ->
                listeners.remove topic
                stopListening topic
            }
        }
    }

    @Override
    void receiveMessage(String topic, String payload) {
        log.debug "Received message in topic '$topic'. Payload: $payload"

        def genericTopic = convertToGenericTopic(topic)

        def listener = listeners[genericTopic]
        if (!listener) {
            log.error "Received message in topic '$topic', but this topic has no listener assigned to general topic '$genericTopic'"
            return
        }

        listener.accept(topic, payload)
    }

    private String convertToGenericTopic(String topic) {
        def genericTopic = topic

        if (topic.startsWith("q.")) {
            genericTopic = genericTopic.take(topic.lastIndexOf(".")) + ".*"
        }

        genericTopic
    }

    private void startListening(String topic, ActiveMqTopicListener listener) {
        log.debug "Subscribing to topic '$topic'"

        if (containers[topic]) {
            log.warn "Trying to subscribe to already subscribed topic '$topic'"
            return
        }

        def container = new DefaultMessageListenerContainer(
                connectionFactory: pooledConnectionFactory,
                cacheLevel: DefaultMessageListenerContainer.CACHE_SESSION,
                pubSubDomain: !listener.isQueue,
                messageListener: listener,
                destinationName: topic
        )

        container.initialize()
        container.start()

        containers[topic] = container
    }

    private void stopListening(String topic) {
        log.debug "Unsubscribing from topic '$topic'"

        def container = containers.remove topic
        if (!container) {
            log.warn "Trying to unsubscribe from topic '$topic', which is not subscribed"
            return
        }

        container.stop()
        container.shutdown()
    }
}

class ActiveMqTopicListener implements MessageListener {
    boolean isQueue
    IReceiver<String, String> messageReceiver

    @Override
    void onMessage(Message message) {
        def topicName
        def destination = message.JMSDestination
        switch (destination) {
            case Topic:
                topicName = destination.topicName
                break
            case Queue:
                topicName = destination.queueName
                break
            default:
                return
        }

        messageReceiver.receiveMessage(topicName, ((TextMessage) message).text)
    }
}
