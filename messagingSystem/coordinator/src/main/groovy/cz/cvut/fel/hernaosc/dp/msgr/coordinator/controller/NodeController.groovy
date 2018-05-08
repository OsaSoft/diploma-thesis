package cz.cvut.fel.hernaosc.dp.msgr.coordinator.controller

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.MsgrNode
import cz.cvut.fel.hernaosc.dp.msgr.coordinator.service.CoordinatorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

@RestController
class NodeController {
    @Autowired
    private CoordinatorService coordinatorService

    @Value('${coordinator.node.cache.time:#{5}}')
    private int nodesUpdateTimeout = 5

    private List<MsgrNode> freeNodes
    private Date lastUpdate


    @RequestMapping(path = "/free-nodes", method = RequestMethod.GET)
    List<MsgrNode> getLeastLoadedNodes() {
        if (!(freeNodes && lastUpdate) || (new Date().time - lastUpdate.time > nodesUpdateTimeout * 1000) ) {
            freeNodes = coordinatorService.leastLoadedNodes
            lastUpdate = new Date()
        }

        freeNodes
    }

    @RequestMapping(path = "/connect", method = RequestMethod.POST)
    void connectNode(@RequestBody MsgrNode node) {
        coordinatorService.addNode(node)
    }

    @RequestMapping(path = "/disconnect/{nodeId}", method = RequestMethod.DELETE)
    void disconnectNode(@PathVariable String nodeId) {
        disconnectNode(new MsgrNode(nodeId: nodeId))
    }

    void disconnectNode(@RequestBody MsgrNode node) {
        coordinatorService.removeNode(node)
    }
}
