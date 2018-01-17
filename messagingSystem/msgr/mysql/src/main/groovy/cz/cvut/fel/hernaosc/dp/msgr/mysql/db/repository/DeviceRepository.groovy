package cz.cvut.fel.hernaosc.dp.msgr.mysql.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Device
import org.springframework.data.repository.CrudRepository

interface DeviceRepository extends CrudRepository<Device, String>, IDeviceRepository {

}