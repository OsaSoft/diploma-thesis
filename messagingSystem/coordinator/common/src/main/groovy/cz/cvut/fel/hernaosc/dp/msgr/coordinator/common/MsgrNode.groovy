package cz.cvut.fel.hernaosc.dp.msgr.coordinator.common

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class MsgrNode implements Serializable {
    private static final long serialVersionUID = 1

    String nodeId
    String address

    MsgrNode withNewId() {
        nodeId = UUID.randomUUID().toString()
        this
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        MsgrNode msgrNode = (MsgrNode) o

        if (nodeId != msgrNode.nodeId) return false

        return true
    }

    int hashCode() {
        return (nodeId != null ? nodeId.hashCode() : 0)
    }
}
