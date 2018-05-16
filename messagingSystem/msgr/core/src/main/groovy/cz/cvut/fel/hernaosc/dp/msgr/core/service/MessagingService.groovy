package cz.cvut.fel.hernaosc.dp.msgr.core.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IGroup
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IUserRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.core.dto.message.MessageDto
import cz.cvut.fel.hernaosc.dp.msgr.core.dto.message.NotificationDto
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.ISender
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.IPlatformAdapter
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Slf4j
class MessagingService implements IMessagingService {

    @Value('${msgr.group.user.page.size:#{50}}')
    private int pageSize = 50

    @Autowired
    private IUserRepository userRepository

    @Autowired
    private IDeviceRepository deviceRepository

    @Autowired
    private AdapterService adapterService

    @Autowired
    private ISender<String, String> mqSender

    @Override
    boolean sendNotification(String title, String body, IGroup group) {
        if (!group) {
            log.warn "Trying to send message to null group"
            return false
        }

        sendNotificationUsers(title, body, group.users)
    }

    @Override
    boolean sendNotification(String title, String body, IUser user) {
        if (!user) {
            log.warn "Trying to send message to null user"
            return false
        }

        sendNotificationDevices(title, body, user.devices)
    }

    @Override
    boolean sendNotification(String title, String body, IDevice device) {
        if (!device) {
            log.warn "Trying to send message to null device"
            return false
        }

        //TODO:
        IPlatform platform = device.platform

        if (!platform) {
            log.warn "Device $device.id has no platform. Cannot send notification"
            return false
        }

        IPlatformAdapter platformAdapter = adapterService.getAdapter(platform)
        platformAdapter.sendNotification(title, body, device)
    }

    @Override
    boolean sendNotificationGroups(String title, String body, List<IGroup> groups) {
        GParsPool.withPool {
            groups?.everyParallel { sendNotification(title, body, it) }
        }
    }

    @Override
    boolean sendNotificationUsers(String title, String body, List<IUser> users) {
        GParsPool.withPool {
            users?.everyParallel { sendNotificationDevices(title, body, it.devices) }
        }
    }

    @Override
    boolean sendNotificationDevices(String title, String body, List<IDevice> devices) {
        GParsPool.withPool {
            devices?.everyParallel { sendNotification(title, body, it) }
        }
    }

    private int numPages(long count) {
        count.intdiv(pageSize) + (count % pageSize == 0 ? 0 : 1)
    }

    private void sendPageMessages(List<String> groupIds, MessageDto message) {
        GParsPool.withPool {
            groupIds.eachParallel { groupId ->
                int page = 0
                numPages(userRepository.countByGroups_Id(groupId)).times {
                    def payload = [page: page++, msg: message, notification: message instanceof NotificationDto]
                    mqSender.send(["q.group.$groupId"], new JsonBuilder(payload).toString(), true)
                }
            }
        }
    }

    @Override
    boolean sendNotificationGroupsIds(String title, String body, List<String> groupIds) {
        sendPageMessages(groupIds, new NotificationDto(title: title, body: body))
        true
    }

    @Override
    boolean sendNotificationUsersIds(String title, String body, List<String> userIds) {
        def topics
        GParsPool.withPool {
            topics = userIds.collectParallel { "q.user.$it" }
        }

        mqSender.send(topics, new JsonBuilder([payload: new NotificationDto(title: title, body: body), notification: true]).toString(), true)
        true
    }

    @Override
    boolean sendNotificationDevicesIds(String title, String body, List<String> deviceIds) {
        def devices = deviceRepository.findAllById(deviceIds)

        GParsPool.withPool {
            devices.eachParallel { device ->
                mqSender.send(["${device.platform.name}.$device.id"], new JsonBuilder([payload:  new NotificationDto(title: title, body: body), notification: true] ).toString())
            }
        }
        true
    }

    @Override
    boolean sendMessageGroupsIds(List<String> groupIds, Map data) {
        sendPageMessages(groupIds, new DataMessageDto(content: data))
        true
    }

    @Override
    boolean sendMessageUsersIds(List<String> userIds, Map data) {
        def topics
        GParsPool.withPool {
            topics = userIds.collectParallel { "q.user.$it" }
        }

        mqSender.send(topics, new JsonBuilder([payload: new DataMessageDto(content: data), notification: false] ).toString(), true)
        true
    }

    @Override
    boolean sendMessageDevicesIds(List<String> deviceIds, Map data) {
        def devices = deviceRepository.findAllById(deviceIds)

        GParsPool.withPool {
            devices.eachParallel { device ->
                mqSender.send(["${device.platform.name}.$device.id"], new JsonBuilder([payload: new DataMessageDto(content: data), notification: false] ).toString())
            }
        }
        true
    }

    @Override
    boolean sendMessage(IGroup group, Map data) {
        if (!group) {
            log.warn "Trying to send message to null group"
            return false
        }

        sendMessageUsers(group.users, data)
    }

    @Override
    boolean sendMessage(IUser user, Map data) {
        if (!user) {
            log.warn "Trying to send message to null user"
            return false
        }

        sendMessageDevices(user.devices, data)
    }

    @Override
    boolean sendMessage(IDevice device, Map data) {
        //TODO:
        IPlatform platform = device.platform

        if (!platform) {
            log.warn "Device $device.id has no platform. Cannot send notification"
            return false
        }

        IPlatformAdapter platformAdapter = adapterService.getAdapter(platform)
        platformAdapter.sendMessage(data, device)
    }

    @Override
    boolean sendMessageGroups(List<IGroup> groups, Map data) {
        GParsPool.withPool {
            groups.everyParallel { sendMessage(it, data) }
        }
    }

    @Override
    boolean sendMessageUsers(List<IUser> users, Map data) {
        GParsPool.withPool {
            users.everyParallel { sendMessage(it, data) }
        }
    }

    @Override
    boolean sendMessageDevices(List<IDevice> devices, Map data) {
        GParsPool.withPool {
            devices.everyParallel { sendMessage(it, data) }
        }
    }
}
