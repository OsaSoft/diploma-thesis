package cz.cvut.fel.hernaosc.dp.msgr.core.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser

interface IDeviceRepository extends IBaseRepository<IDevice> {
    List<IDevice> findAllByIdAndPlatformName(List<String> ids, String platformName)

    List<IDevice> findAllByUser(IUser user)

    List<IDevice> findAllByUserId(String userId)

    IDevice findByToken(String token)

    List<IDevice> findAllByUserIdInAndPlatformName(List<String> userIds, String platformName)
}