package cz.cvut.fel.hernaosc.dp.msgr.coordinator.controller

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.MsgrNode
import cz.cvut.fel.hernaosc.dp.msgr.coordinator.service.CoordinatorService
import groovy.time.TimeCategory
import spock.lang.Specification

class NodeControllerTest extends Specification {

    NodeController nodeController = new NodeController()

    void setup() {
    }

    void cleanup() {
    }

    def "Least loaded node endpoint caches results"() {
        given:
            def coordinatorService = Mock(CoordinatorService)
            nodeController.coordinatorService = coordinatorService

        when: "first call"
            nodeController.leastLoadedNodes
        then: "nodes are reloaded"
            1 * coordinatorService.getLeastLoadedNodes() >> [new MsgrNode().withNewId()]

        when: "second call, within cache time"
            nodeController.leastLoadedNodes
        then: "nodes are pulled from cache"
            0 * coordinatorService.getLeastLoadedNodes()

        when: "third call, after cache time"
            use(TimeCategory) {
                nodeController.lastUpdate = new Date() - (nodeController.nodesUpdateTimeout + 1).second
            }
            nodeController.leastLoadedNodes
        then: "nodes are reloaded"
            1 * coordinatorService.getLeastLoadedNodes()
    }
}
