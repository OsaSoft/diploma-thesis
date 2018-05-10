package cz.cvut.fel.hernaosc.dp.msgr.websocket.common.dto.message

abstract class MessageDto {
    List<String> targetDevices
    List<String> targetUsers
    List<String> targetGroups
}
