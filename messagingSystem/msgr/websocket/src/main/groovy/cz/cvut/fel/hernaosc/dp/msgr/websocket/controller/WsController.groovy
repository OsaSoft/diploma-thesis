package cz.cvut.fel.hernaosc.dp.msgr.websocket.controller

import cz.cvut.fel.hernaosc.dp.msgr.core.CoordinatorConnector
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IEntityService
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.dto.ConnectionRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class WsController {
    static final PLATFORM_NAME = "websocket"

    @Value('${msgr.node.address}')
    private String address

    @Autowired
    private IEntityService entityService

    @Autowired
    private CoordinatorConnector coordinatorConnector

    @RequestMapping(path = "/wsConnect", method = RequestMethod.POST)
    def connect(@RequestBody ConnectionRequest connectionRequest) {
        def platform = entityService.findOrCreateByName(PLATFORM_NAME, IPlatform)

        def user = entityService.findOrCreateById(connectionRequest.userId, IUser, [
                name: connectionRequest.userName ?: UUID.randomUUID().toString()
        ])

        def device = entityService.findOrCreateById(connectionRequest.deviceId, IDevice, [
                platform: platform,
                token   : connectionRequest.deviceToken ?: UUID.randomUUID().toString(),
                user    : user
        ])

        return [
                // if theres nothing in leastLoadedNodes return this node (probably not connected to Coordinator)
                addresses : coordinatorConnector.leastLoadedNodes*.address ?: [address],
                deviceData: new ConnectionRequest(userId: user.id, userName: user.name, deviceId: device.id, deviceToken: device.token)
        ]
    }
}
