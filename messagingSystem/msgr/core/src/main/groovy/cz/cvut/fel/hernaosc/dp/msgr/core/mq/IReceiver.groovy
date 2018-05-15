package cz.cvut.fel.hernaosc.dp.msgr.core.mq

import java.util.function.BiConsumer

interface IReceiver<K, V> {
    void subscribe(List<K> topics, boolean isQueue, BiConsumer<K, V> listener)

    void subscribe(List<K> topics, BiConsumer<K, V> listener)

    void unsubscribe(List<K> topics)

    void receiveMessage(K topic, V payload)
}