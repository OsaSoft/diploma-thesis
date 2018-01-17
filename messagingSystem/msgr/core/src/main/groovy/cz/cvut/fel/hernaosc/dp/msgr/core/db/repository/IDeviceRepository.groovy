package cz.cvut.fel.hernaosc.dp.msgr.core.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser

interface IDeviceRepository {
	List<IDevice> findByUser(IUser user)

	IDevice findByToken(String token)
}