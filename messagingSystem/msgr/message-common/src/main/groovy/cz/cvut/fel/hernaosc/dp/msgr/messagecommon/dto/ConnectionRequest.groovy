package cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class ConnectionRequest {
    String deviceId
    String deviceToken

    String userId
    String userName

    String platformName
}
