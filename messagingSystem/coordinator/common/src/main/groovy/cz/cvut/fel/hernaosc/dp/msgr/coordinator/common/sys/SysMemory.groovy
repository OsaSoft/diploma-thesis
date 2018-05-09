package cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.sys

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class SysMemory {
    long total
    long free

    float getPercent() {
        (free/total * 100f).round(2)
    }
}
