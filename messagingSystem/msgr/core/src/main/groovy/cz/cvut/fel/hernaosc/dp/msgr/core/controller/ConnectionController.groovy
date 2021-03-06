package cz.cvut.fel.hernaosc.dp.msgr.core.controller

import cz.cvut.fel.hernaosc.dp.msgr.core.CoordinatorConnector
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IPlatformRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IEntityService
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.ConnectionRequest
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@Slf4j
class ConnectionController {
    @Value('${msgr.node.address}')
    private String address

    @Autowired
    private IDeviceRepository deviceRepository

    @Autowired
    private IPlatformRepository platformRepository

    @Autowired
    private IEntityService entityService

    @Autowired
    private CoordinatorConnector coordinatorConnector

    @RequestMapping(path = "/connect", method = RequestMethod.POST)
    def connect(@RequestBody ConnectionRequest connectionRequest) {
        log.debug "Received connection request $connectionRequest"
        def platform = platformRepository.findByName(connectionRequest.platformName)

        IUser user
        if (connectionRequest.userId) {
            user = entityService.findOrCreateById(connectionRequest.userId, IUser, [
                    name: connectionRequest.userName ?: UUID.randomUUID().toString()
            ])
        } else {
            user = entityService.findOrCreateByName(connectionRequest.userName, IUser)
        }

        IDevice device
        if (connectionRequest.deviceToken) {
            device = entityService.findOrCreateByName(connectionRequest.deviceToken, IDevice, [platform: platform])
        } else {
            device = entityService.findOrCreateById(connectionRequest.deviceId, IDevice, [
                    platform: platform,
                    token   : connectionRequest.deviceToken ?: UUID.randomUUID().toString()
            ])
        }

        device.user = user
        deviceRepository.save(device)

        return [
                // if theres nothing in leastLoadedNodes return this node (probably not connected to Coordinator)
                addresses : coordinatorConnector.leastLoadedNodes*.address ?: [address],
                deviceData: new ConnectionRequest(userId: user.id, userName: user.name, deviceId: device.id, deviceToken: device.token)
        ]
    }
}
