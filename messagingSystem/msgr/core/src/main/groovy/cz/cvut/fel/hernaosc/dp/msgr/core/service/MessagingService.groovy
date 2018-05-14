package cz.cvut.fel.hernaosc.dp.msgr.core.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IGroup
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.IPlatformAdapter
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class MessagingService implements IMessagingService {
    @Autowired
    AdapterService adapterService

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
