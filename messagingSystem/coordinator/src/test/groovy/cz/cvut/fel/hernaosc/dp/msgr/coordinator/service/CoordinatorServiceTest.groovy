package cz.cvut.fel.hernaosc.dp.msgr.coordinator.service

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.MsgrNode
import groovy.time.TimeCategory
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.apache.http.conn.ConnectTimeoutException
import spock.lang.Specification
import spock.lang.Unroll

class CoordinatorServiceTest extends Specification {

    CoordinatorService coordinatorService = new CoordinatorService()

    void setup() {
    }

    void cleanup() {
    }

    def "Can add node"() {
        given:
            coordinatorService = Spy(CoordinatorService)
            def node = new MsgrNode(address: "test").withNewId()
            def status = new NodeStatus(load: 0.4)

        when:
            coordinatorService.addNode(node)
        then:
            1 * coordinatorService.invokeMethod('checkNodeHealth', [node]) >> status
        and:
            coordinatorService.nodes[node].load == status.load
    }

    def "When adding existing node, nothing is changed"() {
        given:
            coordinatorService = Spy(CoordinatorService)
            def node = new MsgrNode(address: "test").withNewId()
            def nodeStatus = new NodeStatus(load: 0.1)
        and:
            coordinatorService.nodes[node] = nodeStatus

        when:
            coordinatorService.addNode(node)
        then:
            0 * coordinatorService.invokeMethod('checkNodeHealth', _) >> new NodeStatus(load: 0.4)
        and:
            coordinatorService.nodes[node] == nodeStatus
    }

    def "Can remove node"() {
        given:
            def node = initBasicNodes(10).first()

        when:
            coordinatorService.removeNode(node)
        then:
            coordinatorService.nodes.size() == 9
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

    def "list of least loaded nodes doesnt include unresponsive nodes"() {
        given:
            coordinatorService.numListedNodes = 10
            coordinatorService.maxLoad = 1
        and:
            def nodes = initBasicNodes(10)
        and:
            coordinatorService.nodes[nodes[2]].responding = false
            coordinatorService.nodes[nodes[5]].responding = false
            coordinatorService.nodes[nodes[8]].responding = false

        when:
            def result = coordinatorService.leastLoadedNodes
        then:
            result.size() == 7
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
            numNodes * new RESTClient("http://test", _) >> restClientMock
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

    def "Handles node failing a single health check"() {
        given:
            def node = initBasicNodes(1).first()
        and:
            def restClientMock = GroovyMock(RESTClient, global: true)

        when: "health check is performed"
            coordinatorService.doHealthCheck()
        then:
            1 * new RESTClient("http://test", _) >> restClientMock
        and: "health check on node fails"
            1 * restClientMock.get(_) >> {
                throw new ConnectTimeoutException()
            }
        and: "node is set as unresponsive"
            !coordinatorService.nodes[node].responding

        when: "another health check is performed"
            coordinatorService.doHealthCheck()
        then:
            1 * new RESTClient("http://test", _) >> restClientMock
        and: "node is responding again"
            1 * restClientMock.get(_) >> [
                    status: HttpStatus.SC_OK,
                    data  : [load: 0.5]
            ]
        and: "node is responsive again and updated"
            def status = coordinatorService.nodes[node]
            status.responding
            status.load == 0.5
    }

    def "Handles node failing two consecutive health checks within timeout"() {
        given:
            def node = initBasicNodes(1).first()
        and:
            def restClientMock = GroovyMock(RESTClient, global: true)

        when: "health check is performed"
            coordinatorService.doHealthCheck()
        then:
            1 * new RESTClient("http://test", _) >> restClientMock
        and: "health check on node fails"
            1 * restClientMock.get(_) >> {
                throw new ConnectTimeoutException()
            }
        and: "node is set as unresponsive"
            !coordinatorService.nodes[node].responding

        when: "another health check is performed"
            coordinatorService.doHealthCheck()
        then:
            1 * new RESTClient("http://test", _) >> restClientMock
        and: "health check on node fails"
            1 * restClientMock.get(_) >> {
                throw new ConnectTimeoutException()
            }
        and: "node is still unresponsive"
            !coordinatorService.nodes[node].responding
    }

    def "Handles node failing two consecutive health checks outside timeout"() {
        given:
            def node = new MsgrNode(address: "test").withNewId()
            def status = new NodeStatus(load: 0.1)
        and:
            coordinatorService.nodes[node] = status
        and:
            def restClientMock = GroovyMock(RESTClient, global: true)

        when: "health check is performed"
            coordinatorService.doHealthCheck()
        then:
            1 * new RESTClient("http://test", _) >> restClientMock
        and: "health check on node fails"
            1 * restClientMock.get(_) >> {
                throw new ConnectTimeoutException()
            }
        and: "node is set as unresponsive"
            !coordinatorService.nodes[node].responding

        when: "last success was over threshold"
            use(TimeCategory) {
                coordinatorService.nodes[node].lastSuccessfulCheck = new Date() - (coordinatorService.nodeTimeout + 1).seconds
            }
        and: "another health check is performed"
            coordinatorService.doHealthCheck()
        then:
            1 * new RESTClient("http://test", _) >> restClientMock
        and: "health check on node fails"
            1 * restClientMock.get(_) >> {
                throw new ConnectTimeoutException()
            }
        and: "node is removed from connected nodes"
            !coordinatorService.nodes[node]
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
