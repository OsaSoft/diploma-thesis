package cz.cvut.fel.hernaosc.dp.msgr.core.mq

interface ISender<K, V> {
    void send(List<K> topics, V payload)
    void send(List<K> topics, V payload, boolean isQueue)
}