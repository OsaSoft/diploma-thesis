package cz.cvut.fel.hernaosc.dp.msgr.core.queue

import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IReceiver
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.ISender
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@Slf4j
class UserProcessor {

    @Autowired
    private IDeviceRepository deviceRepository

    @Autowired
    private IReceiver<String, String> mqReceiver

    @Autowired
    private ISender<String, String> mqSender

    @PostConstruct
    void init() {
        mqReceiver.subscribe(["q.user.*"], true, processMessage)
    }

    private processMessage = { String topic, String messageText ->
        def userId = topic - "q.user."

        log.debug "Processing user '$userId'"

        def devices = deviceRepository.findAllByUserId(userId)
        log.debug "Found user devices $devices"

        GParsPool.withPool {
            devices.eachParallel { device ->
                mqSender.send(["${device.platform.name}.$device.id"], messageText)
            }
        }
    }
}
