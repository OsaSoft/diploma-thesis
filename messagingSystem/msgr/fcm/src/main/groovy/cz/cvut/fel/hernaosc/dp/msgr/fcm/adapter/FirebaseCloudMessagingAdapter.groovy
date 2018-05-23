package cz.cvut.fel.hernaosc.dp.msgr.fcm.adapter

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IReceiver
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.IPlatformAdapter
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.PlatformAdapter
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IEntityService
import cz.cvut.fel.hernaosc.dp.msgr.core.util.MsgrUtils
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.util.MsgrMessageUtils
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.BeanInitializationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import us.raudi.pushraven.FcmResponse
import us.raudi.pushraven.Message
import us.raudi.pushraven.Notification
import us.raudi.pushraven.Pushraven

import javax.annotation.PostConstruct

@Component
@PlatformAdapter("FCM")
@Slf4j
class FirebaseCloudMessagingAdapter implements IPlatformAdapter {

    static final String PLATFORM_NAME = "FCM"

    @Value('${msgr.adapter.fcm.projectId:#{null}}')
    private String projectId

    @Value('${msgr.adapter.fcm.serviceAccountFilename:#{null}}')
    private String serviceAccountFilename

    @Autowired
    private IDeviceRepository deviceRepository

    @Autowired
    private IEntityService entityService

    @Autowired
    private IReceiver<String, String> mqReceiver

    @PostConstruct
    void init() {
        if (!projectId || !serviceAccountFilename) {
            log.error "FCM not configured"
        }

        try {
            Pushraven.accountFile = new File(serviceAccountFilename)
            Pushraven.projectId = projectId
        } catch (IOException ex) {
            throw new BeanInitializationException("Could not find FCM service account file called '$serviceAccountFilename'", ex)
        }

        entityService.findOrCreateByName(PLATFORM_NAME, IPlatform)

        mqReceiver.subscribe([platformQueueName], true, onMessageForDevice)
    }

    private onMessageForDevice = { String topic, messageText ->
        log.debug "Processing message $messageText"
        def message = MsgrMessageUtils.parseMessageFromJson(messageText)

        def devices = deviceRepository.findAllByUserIdInAndPlatformName(message.targetUsers, PLATFORM_NAME)

        GParsPool.withPool {
            devices.eachParallel {
                if (message instanceof NotificationDto) {
                    sendNotification(message.title, message.body, it)
                } else {
                    //data messages on FCM have a few reserved words, so lets just prepend everything to eliminate
                    //any chance of matching them
                    def content = [:]
                    message.content.keySet().each {
                        content["msgr.$it"] = message.content[it]
                    }

                    sendMessage(content, it)
                }
            }
        }
    }

    @Override
    boolean sendNotification(String title, String body, IDevice device) {
        if (!device) {
            log.error("Attempting to send notification to null device")
            return false
        }

        log.debug "Sending FCM notification to $device."
        log.debug "Title: $title"
        log.debug "Body: $body"

        Notification notification =
                new Notification()
                        .title(title)
                        .body(body)

        Message msg =
                new Message()
                        .notification(notification)
                        .token(device.token)

        FcmResponse response = Pushraven.push(msg)
        log.debug "Received response $response"
        true
    }

    @Override
    boolean sendMessage(Map payload, IDevice device) {
        if (!device) {
            log.error("Attempting to send notification to null device")
            return false
        }

        log.debug "Sending FCM message to $device."
        log.debug "Message: $payload"

        Message msg = new Message()
                .data(MsgrUtils.flattenMap(payload))
                .token(device.token)

        log.trace "Payload to FCM is " + msg.toJson().toString()

        FcmResponse response = Pushraven.push(msg)
        log.debug "Received response $response"
        true
    }

    String getPlatformQueueName() {
        PLATFORM_NAME
    }

    @Override
    long getBoundClientsNum() {
        0
    }
}
