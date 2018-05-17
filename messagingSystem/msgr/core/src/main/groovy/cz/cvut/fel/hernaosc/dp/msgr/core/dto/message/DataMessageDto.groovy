package cz.cvut.fel.hernaosc.dp.msgr.core.dto.message

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false, includeSuperProperties = true)
class DataMessageDto extends MessageDto {
    Map content
}
