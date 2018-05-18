package cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false, includeSuperProperties = true)
class DataMessageDto extends MessageDto {
    Map content
}
