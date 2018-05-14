package cz.cvut.fel.hernaosc.dp.msgr.core.mq

interface IReceiver<K, V> {
    void subscribe(K... topics)
    void unsubscribe(K... topics)
    void receiveMessage(K topic, V payload)
}