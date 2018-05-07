package cz.cvut.fel.hernaosc.dp.msgr.coordinator.service

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.MsgrNode
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

class CoordinatorServiceTest extends Specification {

    CoordinatorService coordinatorService = new CoordinatorService()

    void setup() {
    }

    void cleanup() {
    }

    @Unroll
    def "gets a list of least loaded nodes, threshold #threshold, numListedNodes #numListedNodes"() {
        given:
            coordinatorService.numListedNodes = numListedNodes
            coordinatorService.maxLoad = threshold
        and:
            initBasicNodes(20)

        when:
            def result = coordinatorService.leastLoadedNodes
        then:
            result.size() == resultSize
        and:
            result == result.sort { coordinatorService.nodes[it].load }

        where:
            numListedNodes | threshold | resultSize
            5              | 1         | 5
            5              | 0.5       | 5
            5              | 0.2       | 3
            5              | 0         | 1
            5              | -1        | 0
            10             | 1         | 10
            10             | 0.5       | 6
            10             | 0.3       | 4
            10             | 0         | 1
            3              | 1         | 3
            3              | 0.4       | 3
            3              | 0.1       | 2
            3              | 0         | 1
    }

    @Unroll
    def "health check is done on all nodes and updates their status (#numNodes nodes)"() {
        given:
            def nodes = initBasicNodes(numNodes)
        and:
            def restClientMock = GroovyMock(RESTClient, global: true)

        when:
            coordinatorService.doHealthCheck()
        then:
            numNodes * new RESTClient("test", _) >> restClientMock
        and:
            numNodes * restClientMock.get(_) >> [
                    status: HttpStatus.SC_OK,
                    data  : [load: 0.5]
            ]
        and:
            nodes.every { coordinatorService.nodes[it].load == 0.5 }

        where:
            numNodes << [10, 5, 1]
    }

    private initBasicNodes(int num) {
        def nodes = makeNodes(num)
        def status = makeStatus(num)
        nodes.eachWithIndex { node, i ->
            coordinatorService.nodes[node] = status[i]
        }

        nodes
    }

    private makeNodes(int num) {
        def nodes = []
        num.times { nodes << new MsgrNode(address: "test").withNewId() }
        nodes
    }

    private makeStatus(int num) {
        def status = []
        (0..(num - 1)).each { status << new NodeStatus(load: it / 10) }
        //now we have a list of load 0 to 0.9, but its ordered, so lets shuffle it
        Collections.shuffle(status)
        status
    }
}
