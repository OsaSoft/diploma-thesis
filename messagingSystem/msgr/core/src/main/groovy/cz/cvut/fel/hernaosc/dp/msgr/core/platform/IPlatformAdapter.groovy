package cz.cvut.fel.hernaosc.dp.msgr.core.platform

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice

interface IPlatformAdapter {
	boolean sendNotification(String title, String body, IDevice device)
	boolean sendMessage(Map payload, IDevice device)
	String getPlatformQueueName()
    long getBoundClientsNum()
}