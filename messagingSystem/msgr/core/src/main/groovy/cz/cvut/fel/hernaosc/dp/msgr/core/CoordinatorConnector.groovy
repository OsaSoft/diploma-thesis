package cz.cvut.fel.hernaosc.dp.msgr.core

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.MsgrNode
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@Slf4j
class CoordinatorConnector {
    @Value('${msgr.node.id:#{null}}')
    private String nodeId

    @Value('${msgr.node.address}')
    private String address

    @Value('${msgr.coordinator.address}')
    private String coordinatorAddress

    private boolean connected = false

    @Value('${msgr.node.cache.time:#{5}}')
    private int nodesUpdateTimeout = 5

    private List<MsgrNode> freeNodes
    private Date lastUpdate

    @PostConstruct
    void init() {
        if (!nodeId) {
            nodeId = UUID.randomUUID().toString()
            log.warn "NodeId is not configured. Setting to UUID: '$nodeId'"
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    void connectToCoordinator() {
        def node = new MsgrNode(nodeId: nodeId, address: address)
        log.info "Connecting to Node Coordinator on address '$coordinatorAddress' as node $node"

        def client = new RESTClient("http://$coordinatorAddress")
        def response
        try {
            response = client.post(path: "/connect",
                    contentType: ContentType.JSON,
                    body: node,
                    headers: [Accept: 'application/json']
            )

            if (response.status == HttpStatus.SC_OK) {
                log.info "Successfully connected to Coordinator"
                connected = true
            } else {
                throw new RuntimeException("Connection attempt returned status code $response.status")
            }
        } catch (Exception ex) {
            log.error "Failed to connect to Coordinator. Node will be running in standalone mode. Reason: $ex.message"
            log.trace "Connection failed reason: $ex.message", ex
        }
    }

    List<MsgrNode> getLeastLoadedNodes() {
        if (!connected) {
            return []
        }

        if (!(freeNodes && lastUpdate) || (new Date().time - lastUpdate.time > nodesUpdateTimeout * 1000)) {
            updateLeastLoadedNodes()
            lastUpdate = new Date()
        }

        freeNodes ?: []
    }

    void updateLeastLoadedNodes() {
        log.debug "Updating least loaded nodes list"

        def client = new RESTClient("http://$coordinatorAddress")
        HttpResponseDecorator response
        try {
            response = client.get(
                    path: "/free-nodes",
                    contentType: ContentType.JSON,
                    headers: [Accept: 'application/json']
            )

            if (response.status == HttpStatus.SC_OK) {
                freeNodes = response.data.collectParallel { new MsgrNode(it) }
                log.debug "Received nodes: $freeNodes"
            } else {
                throw new RuntimeException("Connection attempt returned status code $response.status")
            }
        } catch (Exception ex) {
            log.error "Failed to connect to Coordinator to update least loaded nodes. Reason: $ex.message"
            log.trace "Connection failed reason: $ex.message", ex
        }
    }
}
