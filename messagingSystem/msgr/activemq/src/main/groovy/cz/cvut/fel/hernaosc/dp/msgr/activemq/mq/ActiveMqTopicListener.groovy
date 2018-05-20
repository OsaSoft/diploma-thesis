package cz.cvut.fel.hernaosc.dp.msgr.activemq.mq

import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IReceiver

import javax.jms.*

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
