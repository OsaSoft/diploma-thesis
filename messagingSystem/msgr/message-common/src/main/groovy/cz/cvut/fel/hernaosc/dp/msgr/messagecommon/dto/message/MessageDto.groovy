package cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message

abstract class MessageDto {
    List<String> targetDevices
    List<String> targetUsers
    List<String> targetGroups

    String senderDeviceId
    String senderUsername
}
