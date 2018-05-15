package cz.cvut.fel.hernaosc.dp.msgr.core.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IGroup
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IUserRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.core.dto.message.NotificationDto
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.ISender
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.IPlatformAdapter
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class MessagingService implements IMessagingService {

    @Autowired
    private IUserRepository userRepository

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

    @Override
    boolean sendNotificationGroupsIds(String title, String body, List<String> groupIds) {
        def rv
        GParsPool.withPool {
            rv = groupIds.everyParallel {
                def userIds = userRepository.getUserIdsByGroup(it)
                sendNotificationUsersIds(title, body, userIds)
            }
        }
        rv
    }

    @Override
    boolean sendNotificationUsersIds(String title, String body, List<String> userIds) {
        def topics
        GParsPool.withPool {
            topics = userIds.collectParallel { "u.$it" }
        }

        mqSender.send(topics, new JsonBuilder(new NotificationDto(title: title, body: body)).toString())
        true
    }

    @Override
    boolean sendNotificationDevicesIds(String title, String body, List<String> deviceIds) {
        mqSender.send(deviceIds, new JsonBuilder(new NotificationDto(title: title, body: body)).toString())
        true
    }

    @Override
    boolean sendMessageGroupsIds(List<String> groupIds, Map data) {
        def rv
        GParsPool.withPool {
            rv = groupIds.everyParallel {
                def userIds = userRepository.getUserIdsByGroup(it)
                sendMessageUsersIds(userIds, data)
            }
        }
        rv
    }

    @Override
    boolean sendMessageUsersIds(List<String> userIds, Map data) {
        def topics
        GParsPool.withPool {
            topics = userIds.collectParallel { "u.$it" }
        }

        //TODO this works for binded sessions (such as WS), but we need to separate this by platform to get it into different queues
        mqSender.send(topics, new JsonBuilder(new DataMessageDto(content: data)).toString())
        true
    }

    @Override
    boolean sendMessageDevicesIds(List<String> deviceIds, Map data) {
        mqSender.send(deviceIds, new JsonBuilder(new DataMessageDto(content: data)).toString())
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
