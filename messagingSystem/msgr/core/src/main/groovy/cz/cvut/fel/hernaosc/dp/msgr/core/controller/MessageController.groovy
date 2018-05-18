package cz.cvut.fel.hernaosc.dp.msgr.core.controller

import cz.cvut.fel.hernaosc.dp.msgr.core.mq.ISender
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IMessagingService
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/send")
@Slf4j
class MessageController {

    @Autowired
    IMessagingService messagingService

    @Autowired
    ISender<String, String> sender

    @RequestMapping(path = "/notification", method = RequestMethod.POST)
    void sendNotification(@RequestBody NotificationDto notificationDto) {
        log.debug "Received notification $notificationDto"

        ["Devices", "Users", "Groups"].each {
            if (notificationDto."target$it") {
                messagingService."sendNotification${it}Ids"(notificationDto.title, notificationDto.body, notificationDto."target$it")
            }
        }
    }

    @RequestMapping(path = "/message", method = RequestMethod.POST)
    void sendMessage(@RequestBody DataMessageDto dataMessageDto) {
        log.debug "Received message $dataMessageDto"

        ["Devices", "Users", "Groups"].each {
            if (dataMessageDto."target$it") {
                messagingService."sendMessage${it}Ids"(dataMessageDto."target$it", dataMessageDto.content)
            }
        }
    }
}
