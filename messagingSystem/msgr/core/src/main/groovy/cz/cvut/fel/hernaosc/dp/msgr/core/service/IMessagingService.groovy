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
}
