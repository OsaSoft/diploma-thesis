package cz.cvut.fel.hernaosc.dp.msgr.coordinator.service

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.MsgrNode
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.util.concurrent.ConcurrentHashMap

@Service
@Slf4j
class CoordinatorService {
    @Value('${coordinator.list.node.amount:#{10}}')
    private int numListedNodes = 10
    @Value('${coordinator.list.node.max-load:#{0.9}}')
    private float maxLoad = 0.9
    @Value('${coordinator.node.unhealthy.timeout:#{15}}')
    private int nodeTimeout = 15

    Map<MsgrNode, NodeStatus> nodes = [:] as ConcurrentHashMap

    void addNode(MsgrNode node) {
        log.info "Node '$node' has connected"

        if (!nodes[node]) {
            def status = checkNodeHealth(node)
            if (status) {
                nodes[node] = status
            } else {
                log.warn "Could not add node $node because initial healt check failed"
            }
        }
    }

    void removeNode(MsgrNode node) {
        log.info "Disconnecting node '$node'"
        nodes.remove(node)
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
                    currentStatus.responding = false
                    if (new Date().time - currentStatus.lastSuccessfulCheck.time > nodeTimeout * 1000) {
                        nodesToRemove << node
                    }
                }
            }

            nodesToRemove.eachParallel { nodes.remove(it) }
        }

        log.debug "Health check finished"
    }

    private NodeStatus checkNodeHealth(MsgrNode node) {
        log.trace "Starting health check on node $node"
        def client = new RESTClient("http://$node.address")
        try {
            def response = client.get(
                    path: "/health",
                    contentType: ContentType.JSON,
                    headers: [Accept: 'application/json']
            )

            if (response.status == HttpStatus.SC_OK) {
                new NodeStatus(load: response.data.load)
            } else {
                log.warn "Health check on Node '$node.address' returned code '$response.status'"
            }
        } catch (Exception ex) {
            log.warn "Error getting health from node '$node'. Reason: $ex.message"
            log.trace "Error '$ex.message': ", ex
        }
    }

    List<MsgrNode> getLeastLoadedNodes() {
        log.debug "Getting $numListedNodes least loaded nodes"

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

    Map<MsgrNode, NodeStatus> getNodes() {
        nodes
    }
}

class NodeStatus {
    float load
    Date lastSuccessfulCheck = new Date()
    boolean responding = true
}
