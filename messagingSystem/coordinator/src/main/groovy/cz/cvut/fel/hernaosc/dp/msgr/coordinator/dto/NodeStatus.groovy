package cz.cvut.fel.hernaosc.dp.msgr.coordinator.dto

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class NodeStatus {
    float load
    float memory
    Date lastSuccessfulCheck = new Date()
    boolean responding = true
}
