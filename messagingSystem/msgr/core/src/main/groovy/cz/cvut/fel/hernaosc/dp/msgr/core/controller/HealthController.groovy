package cz.cvut.fel.hernaosc.dp.msgr.core.controller

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.sys.SysUtils
import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
@Slf4j
class HealthController {
    @RequestMapping(method = RequestMethod.GET)
    def getHealth() {
        def load
        try {
            load = (SysUtils.processCpuLoad / 100).round(3)
        } catch (Exception ex) {
            log.error "Error getting system load", ex
        }

        //if load couldnt be taken, assume full load
        [load: load ?: 1, memory: SysUtils.memoryStatus.percent]
    }
}
