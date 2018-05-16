package cz.cvut.fel.hernaosc.dp.msgr.core.queue

import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IUserRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IReceiver
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IMessagingService
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@Slf4j
class GroupProcessor {

    @Value('${msgr.group.user.page.size:#{50}}')
    private int pageSize = 50

    @Autowired
    private IUserRepository userRepository

    @Autowired
    private IMessagingService messagingService

    @Autowired
    private IReceiver<String, String> mqReceiver

    @PostConstruct
    void init() {
        mqReceiver.subscribe(["q.group.*"], true, processMessage)
    }

    private processMessage = { String topic, String messageText ->
        def groupId = topic - "q.group."

        def json = new JsonSlurper().parseText(messageText)

        log.debug "Processing group '$groupId', page $json.page"

        def userIds = userRepository.findAllByGroups_Id(groupId, new PageRequest(json.page, pageSize)).content*.id

        if (json.notification) {
            messagingService.sendNotificationUsersIds(json.msg.title, json.msg.body, userIds)
        } else {
            messagingService.sendMessageUsersIds(userIds, json.msg.content)
        }
    }
}
