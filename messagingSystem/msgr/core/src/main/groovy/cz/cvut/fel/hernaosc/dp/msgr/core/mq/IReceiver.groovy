package cz.cvut.fel.hernaosc.dp.msgr.core.mq

import java.util.function.Consumer

interface IReceiver<K, V> {
    void subscribe(List<K> topics, Consumer<V> listener)

    void unsubscribe(K... topics)

    void receiveMessage(K topic, V payload)
}