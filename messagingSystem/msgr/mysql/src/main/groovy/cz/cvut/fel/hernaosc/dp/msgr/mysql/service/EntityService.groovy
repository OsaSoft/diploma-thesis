package cz.cvut.fel.hernaosc.dp.msgr.mysql.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.IEntity
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IGroup
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IGroupRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IPlatformRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IUserRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IEntityService
import cz.cvut.fel.hernaosc.dp.msgr.core.util.MsgrUtils
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Device
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Group
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Platform
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EntityService implements IEntityService {

    @Autowired
    private IPlatformRepository platformRepository

    @Autowired
    private IDeviceRepository deviceRepository

    @Autowired
    private IUserRepository userRepository

    @Autowired
    private IGroupRepository groupRepository

    @Override
    IEntity findOrCreateById(String id, Class clazz, Map params = [:]) {
        def entity
        params.id = id

        switch (clazz) {
            case IPlatform:
                entity = MsgrUtils.fromOptional(platformRepository.findById(id)) ?: platformRepository.save(new Platform(params))
                break
            case IDevice:
                entity = MsgrUtils.fromOptional(deviceRepository.findById(id)) ?: deviceRepository.save(new Device(params))
                break
            case IUser:
                entity = MsgrUtils.fromOptional(userRepository.findById(id)) ?: userRepository.save(new User(params))
                break
            case IGroup:
                entity = MsgrUtils.fromOptional(groupRepository.findById(id)) ?: groupRepository.save(new Group(params))
                break
            default:
                throw new IllegalArgumentException("Improper type: '$clazz'")
                break
        }

        entity
    }

    @Override
    IEntity findOrCreateByName(String name, Class clazz, Map params = [:]) {
        def entity
        params.name = name

        switch (clazz) {
            case IPlatform:
                entity = platformRepository.findByName(name) ?: platformRepository.save(new Platform(params))
                break
            case IDevice:
                params.remove("name")
                params.token = name
                entity = deviceRepository.findByToken(name) ?: deviceRepository.save(new Device(params))
                break
            case IUser:
                entity = userRepository.findByName(name) ?: userRepository.save(new User(params))
                break
            case IGroup:
                entity = groupRepository.findByName(name) ?: groupRepository.save(new Group(params))
                break
            default:
                throw new IllegalArgumentException("Improper type: '$clazz'")
                break
        }

        entity
    }

    @Override
    IEntity create(Map params, Class clazz) {
        def entity

        switch (clazz) {
            case IPlatform:
                entity = platformRepository.save(new Platform(params))
                break
            case IDevice:
                entity = deviceRepository.save(new Device(params))
                break
            case IUser:
                entity = userRepository.save(new User(params))
                break
            case IGroup:
                entity = groupRepository.save(new Group(params))
                break
            default:
                throw new IllegalArgumentException("Improper type: '$clazz'")
                break
        }

        entity
    }
}
