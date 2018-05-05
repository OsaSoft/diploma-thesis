package cz.cvut.fel.hernaosc.dp.msgr.coordinator.service

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.MsgrNode
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.util.concurrent.ConcurrentHashMap

@Service
@Slf4j
class CoordinatorService {
    @Value('coordinator.list.node.amount:#{10}')
    private int numListedNodes
    @Value('coordinator.list.node.max-load:#{0.9}')
    private float maxLoad

    Map<MsgrNode, NodeStatus> nodes = [:] as ConcurrentHashMap

    void doHealthCheck() {
        log.debug "Starting health check on ${nodes.size()} connected nodes"
        nodes.each { node, status ->
            //TODO
        }
    }

    List<MsgrNode> getLeastLoadedNodes() {
        //first filter by responding and load less than max threshold
        nodes.findAll { node, status ->
            status.responding && status.load <= maxLoad
        }
        //next sort by load and only get Nodes
        .toSorted { a, b -> a.value.load <=> b.value.load }.keySet()
        //finally take first numListedNodes elems
        .take(numListedNodes) as List
    }
}

class NodeStatus {
    float load
    Date lastSuccessfulCheck = new Date()
    boolean responding = true
}
