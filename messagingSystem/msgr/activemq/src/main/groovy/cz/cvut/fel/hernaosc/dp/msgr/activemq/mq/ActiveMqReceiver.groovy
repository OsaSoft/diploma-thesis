package cz.cvut.fel.hernaosc.dp.msgr.activemq.mq

import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IReceiver
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.listener.DefaultMessageListenerContainer
import org.springframework.stereotype.Component

import javax.jms.ConnectionFactory
import javax.jms.Message
import javax.jms.MessageListener
import javax.jms.TextMessage
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

@Component
@Slf4j
class ActiveMqReceiver implements IReceiver<String, String> {

    @Autowired
    private ConnectionFactory connectionFactory

    private Map<String, DefaultMessageListenerContainer> containers = [:] as ConcurrentHashMap

    private Map<String, Consumer<String>> listeners = [:] as ConcurrentHashMap

    @Override
    void subscribe(List<String> topics, Consumer<String> listener) {
        GParsPool.withPool {
            topics.eachParallel { topic ->
                listeners[topic] = listener
                startListening(topic, new ActiveMqTopicListener(topic: topic, messageReceiver: this))
            }
        }
    }

    @Override
    void unsubscribe(String... topics) {
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

        def listener = listeners[topic]
        if (!listener) {
            log.error "Received message in topic '$topic', but this topic has no listener assigned"
            return
        }

        listener.accept(payload)
    }

    private void startListening(String topic, ActiveMqTopicListener listener) {
        log.debug "Subscribing to topic '$topic'"

        if (containers[topic]) {
            log.warn "Trying to subscribe to already subscribed topic '$topic'"
            return
        }

        def container = new DefaultMessageListenerContainer(
                connectionFactory: connectionFactory,
                pubSubDomain: true,
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
    String topic
    IReceiver<String, String> messageReceiver

    @Override
    void onMessage(Message message) {
        messageReceiver.receiveMessage(topic, ((TextMessage) message).text)
    }
}
