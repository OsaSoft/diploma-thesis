package cz.cvut.fel.hernaosc.dp.msgr.nodestats.controller

import cz.cvut.fel.hernaosc.dp.msgr.core.controller.HealthController
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IPlatformRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.mq.IMqStats
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IAdapterService
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.util.concurrent.ConcurrentHashMap

@RestController
class NodeStatsController {

    @Autowired
    private HealthController healthController

    @Autowired
    private IAdapterService adapterService

    @Autowired
    private IPlatformRepository platformRepository

    @Autowired(required = false)
    private IMqStats mqStats

    @RequestMapping(path = "/stats")
    def getStats() {
        def platforms = platformRepository.findAllByStateless(false)
        def platformClients = [:] as ConcurrentHashMap
        def totalClients = 0
        GParsPool.withPool {
            platforms.eachParallel {
                platformClients[it.name] = adapterService.getAdapter(it).boundClientsNum
            }

            totalClients = platformClients.values().sumParallel()
        }

        [system: healthController.health, totalBoundClients: totalClients, platformClients: platformClients, mq: mqStats?.stats]
    }
}
