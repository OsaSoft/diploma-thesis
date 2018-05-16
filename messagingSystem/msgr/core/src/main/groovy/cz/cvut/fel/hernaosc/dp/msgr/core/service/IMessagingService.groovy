package cz.cvut.fel.hernaosc.dp.msgr.core.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IGroup
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser

interface IMessagingService {
    boolean sendNotification(String title, String body, IGroup group)

    boolean sendNotification(String title, String body, IUser user)

    boolean sendNotification(String title, String body, IDevice device)

    boolean sendNotificationGroups(String title, String body, List<IGroup> groups)

    boolean sendNotificationUsers(String title, String body, List<IUser> users)

    boolean sendNotificationDevices(String title, String body, List<IDevice> devices)

    boolean sendNotificationGroupsIds(String title, String body, List<String> groupIds)

    boolean sendNotificationUsersIds(String title, String body, List<String> userIds)

    boolean sendNotificationDevicesIds(String title, String body, List<String> deviceIds)

    boolean sendMessage(IGroup group, Map data)

    boolean sendMessage(IUser user, Map data)

    boolean sendMessage(IDevice device, Map data)

    boolean sendMessageGroups(List<IGroup> groups, Map data)

    boolean sendMessageUsers(List<IUser> users, Map data)

    boolean sendMessageDevices(List<IDevice> devices, Map data)

    boolean sendMessageGroupsIds(List<String> groupIds, Map data)

    boolean sendMessageUsersIds(List<String> userIds, Map data)

    boolean sendMessageDevicesIds(List<String> deviceIds, Map data)
}
