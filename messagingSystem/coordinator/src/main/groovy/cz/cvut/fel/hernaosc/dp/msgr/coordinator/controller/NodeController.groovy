package cz.cvut.fel.hernaosc.dp.msgr.coordinator.controller

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.MsgrNode
import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.sys.SysUtils
import cz.cvut.fel.hernaosc.dp.msgr.coordinator.service.CoordinatorService
import groovy.xml.MarkupBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
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
        if (!(freeNodes && lastUpdate) || (new Date().time - lastUpdate.time > nodesUpdateTimeout * 1000)) {
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

    @RequestMapping(path = "/monitor", produces = MediaType.TEXT_HTML_VALUE)
    def monitorPage() {
        def writer = new StringWriter()

        new MarkupBuilder(writer).html {
            head {
                title "Msgr Status Monitor"
            }
            body {
                h1 "Msgr NodeCoordinator status"
                table("border": 1) {
                    thead {
                        tr {
                            th "CPU Load (%)"
                            th "Free Memory (%)"
                            th "Memory (free/total)"
                        }
                    }
                    tbody {
                        tr {
                            td SysUtils.processCpuLoad

                            def memory = SysUtils.memoryStatus
                            td memory.percent
                            td "$memory.free/$memory.total B"
                        }
                    }
                }

                h1 "Msgr Node status monitor"

                table("border": 1) {
                    caption "Total connected nodes: ${coordinatorService.nodes.size()}"
                    thead {
                        tr {
                            th "ID"
                            th "Address"
                            th "Responding"
                            th "CPU Load (%)"
                            th "Free Memory (%)"
                            th "Last Succesful Check"
                            th "More"
                        }
                    }
                    tbody {
                        coordinatorService.nodes.each { node, status ->
                            tr {
                                td node.nodeId
                                td node.address
                                td(status.responding ? "✔" : "✖")
                                td((status.load * 100).round(2))
                                td status.memory
                                td status.lastSuccessfulCheck
                                td {
                                    a(href: "http://$node.address/stats", target: "_blank", "🔍")
                                }
                            }
                        }
                    }
                }
            }
        }

        writer.toString()
    }
}
