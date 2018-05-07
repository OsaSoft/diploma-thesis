package cz.cvut.fel.hernaosc.dp.msgr.coordinator.service

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.MsgrNode
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import groovyx.net.http.RESTClient
import net.sf.json.JSON
import org.apache.http.HttpStatus
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

    void addNode(MsgrNode node) {
        log.info "Node '$node' has connected."

        if (!nodes[node]) {
            def status = checkNodeHealth(node)
            if (status) {
                nodes[node] = status
            }
        }
    }

    void doHealthCheck() {
        log.debug "Starting health check on ${nodes.size()} connected nodes"

        def nodesToRemove = [].asSynchronized()
        GParsPool.withPool {
            nodes.eachParallel { node, status ->
                def currentStatus = checkNodeHealth(node)
                if (currentStatus) {
                    nodes[node] = currentStatus
                } else {
                    currentStatus = nodes[node]
                    if (currentStatus.responding) {
                        currentStatus.responding = false
                    } else {
                        nodesToRemove << node
                    }
                }
            }

            nodesToRemove.eachParallel { nodes.remove(it) }
        }
    }

    private NodeStatus checkNodeHealth(MsgrNode node) {
        def client = new RESTClient(node.address, JSON)
        try {
            def response = client.get path: "/health"
            if (response.status == HttpStatus.SC_OK) {
                new NodeStatus(load: response.data.load)
            } else {
                log.warn "Health check on Node '$node.address' returned code '$response.status'"
            }
        } catch (Exception ex) {
            log.warn "Error getting health from node '$node'", ex
        }
    }

    List<MsgrNode> getLeastLoadedNodes() {
        GParsPool.withPool {
            //first filter by responding and load less than max threshold
            nodes.findAllParallel { node, status ->
                status.responding && status.load <= maxLoad
            }
            //next sort by load and only get Nodes
                    .toSorted { a, b -> a.value.load <=> b.value.load }.keySet()
            //finally take first numListedNodes elems
                    .take(numListedNodes) as List
        }
    }
}

class NodeStatus {
    float load
    Date lastSuccessfulCheck = new Date()
    boolean responding = true
}
