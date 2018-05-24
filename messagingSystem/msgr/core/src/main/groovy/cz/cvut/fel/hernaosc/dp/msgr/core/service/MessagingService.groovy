package cz.cvut.fel.hernaosc.dp.msgr.core.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IGroup
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IUserRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.ISender
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.IPlatformAdapter
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.MessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto
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

        sendNotificationGroupsIds(title, body, [group.id])
    }

    @Override
    boolean sendNotification(String title, String body, IUser user) {
        if (!user) {
            log.warn "Trying to send message to null user"
            return false
        }

        sendNotificationUsersIds(title, body, [user.id])
    }

    @Override
    boolean sendNotification(String title, String body, IDevice device) {
        if (!device) {
            log.warn "Trying to send message to null device"
            return false
        }

        sendNotificationDevicesIds(title, body, [device.id])
    }

    @Override
    boolean sendNotificationGroups(String title, String body, List<IGroup> groups) {
        sendNotificationGroupsIds(title, body, groups*.id)
    }

    @Override
    boolean sendNotificationUsers(String title, String body, List<IUser> users) {
        sendNotificationUsersIds(title, body, users*.id)
    }

    @Override
    boolean sendNotificationDevices(String title, String body, List<IDevice> devices) {
        sendNotificationDevicesIds(title, body, devices*.id)
    }

    @Override
    boolean sendMessage(IGroup group, Map data) {
        if (!group) {
            log.warn "Trying to send message to null group"
            return false
        }

        sendMessageGroupsIds([group.id], data)
    }

    @Override
    boolean sendMessage(IUser user, Map data) {
        if (!user) {
            log.warn "Trying to send message to null user"
            return false
        }

        sendMessageUsersIds([user.id], data)
    }

    @Override
    boolean sendMessage(IDevice device, Map data) {
        if (!device) {
            log.warn "Trying to send message to null device"
            return false
        }

        sendMessageDevicesIds([device.id], data)
    }

    @Override
    boolean sendMessageGroups(List<IGroup> groups, Map data) {
        sendMessageGroupsIds(groups*.id, data)
    }

    @Override
    boolean sendMessageUsers(List<IUser> users, Map data) {
        sendMessageUsersIds(users*.id, data)
    }

    @Override
    boolean sendMessageDevices(List<IDevice> devices, Map data) {
        sendMessageDevicesIds(devices*.id, data)
    }

    @Override
    boolean sendNotificationGroupsIds(String title, String body, List<String> groupIds) {
        sendPageMessages(groupIds, new NotificationDto(title: title, body: body, targetGroups: groupIds))
        true
    }

    @Override
    boolean sendNotificationUsersIds(String title, String body, List<String> userIds) {
        def topics
        GParsPool.withPool {
            topics = userIds.collectParallel { "q.user.$it" }
        }

        mqSender.send(topics, new JsonBuilder([payload: new NotificationDto(title: title, body: body, targetUsers: userIds), notification: true]).toString(), true)
        true
    }

    @Override
    boolean sendNotificationDevicesIds(String title, String body, List<String> deviceIds) {
        def devices = deviceRepository.findAllById(deviceIds)

        GParsPool.withPool {
            devices.eachParallel { device ->
                if (device.platform.stateless) {
                    mqSender.send([device.platform.name], new JsonBuilder([payload: new NotificationDto(title: title, body: body, targetDevices: deviceIds), notification: true]).toString(), true)
                } else {
                    mqSender.send(["${device.platform.name}.$device.id"], new JsonBuilder([payload: new NotificationDto(title: title, body: body, targetDevices: deviceIds), notification: true]).toString(), true)
                }
            }
        }
        true
    }

    @Override
    boolean sendMessageGroupsIds(List<String> groupIds, Map data) {
        sendPageMessages(groupIds, new DataMessageDto(content: data, targetGroups: groupIds))
        true
    }

    @Override
    boolean sendMessageUsersIds(List<String> userIds, Map data) {
        def topics
        GParsPool.withPool {
            topics = userIds.collectParallel { "q.user.$it" }
        }

        mqSender.send(topics, new JsonBuilder([payload: new DataMessageDto(content: data, targetUsers: userIds), notification: false]).toString(), true)
        true
    }

    @Override
    boolean sendMessageDevicesIds(List<String> deviceIds, Map data) {
        def devices = deviceRepository.findAllById(deviceIds)

        GParsPool.withPool {
            devices.eachParallel { device ->
                if (device.platform.stateless) {
                    mqSender.send([device.platform.name], new JsonBuilder([payload: new DataMessageDto(content: data, targetDevices: deviceIds), notification: false]).toString(), true)
                } else {
                    mqSender.send(["${device.platform.name}.$device.id"], new JsonBuilder([payload: new DataMessageDto(content: data, targetDevices: deviceIds), notification: false]).toString(), true)
                }
            }
        }
        true
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
}
