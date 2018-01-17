package cz.cvut.fel.hernaosc.dp.msgr.core.db.entities

import cz.cvut.fel.hernaosc.dp.msgr.core.db.IEntity

interface IUser extends IEntity {
	String getName()

	void setName(String name)

	List<IDevice> getDevices()

	void setDevices(List<IDevice> devices)

	void addDevice(IDevice device)

	List<IGroup> getGroups()

	void setGroups(List<IGroup> groups)
}
