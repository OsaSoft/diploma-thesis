package cz.cvut.fel.hernaosc.dp.msgr.coordinator.common

import groovy.transform.ToString

@ToString
class MsgrNode implements Serializable {
    private static final long serialVersionUID = 1

    String nodeId
    String address

    MsgrNode withNewId() {
        nodeId = UUID.randomUUID().toString()
        this
    }
}
