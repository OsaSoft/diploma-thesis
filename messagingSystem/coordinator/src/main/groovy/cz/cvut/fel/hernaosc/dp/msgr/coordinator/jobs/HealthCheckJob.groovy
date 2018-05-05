package cz.cvut.fel.hernaosc.dp.msgr.coordinator.jobs

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.service.CoordinatorService
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean

@Slf4j
class HealthCheckJob extends QuartzJobBean {
    @Autowired
    private CoordinatorService coordinatorService

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.trace "Health check job triggered"
        coordinatorService.doHealthCheck()
    }
}
