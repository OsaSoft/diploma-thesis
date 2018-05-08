package cz.cvut.fel.hernaosc.dp.msgr.core.controller

import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
@Slf4j
class HealthController {

    /**
     * Requires Python and PSUtil to be installed on machine https://pypi.org/project/psutil/
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    def getHealth() {
        def cmd = ["python", "-c", "import psutil;print(psutil.cpu_times_percent(interval=0.5).system)"].execute()
        cmd.waitForOrKill(1000)
        def load
        try {
            load = (Float.parseFloat(cmd.text) / 100).round(3)
        } catch (Exception ex) {
            log.error "Error parsing system load '$cmd.text'", ex
        }

        //if load couldnt be taken, assume full load
        [load: load ?: 1]
    }
}
