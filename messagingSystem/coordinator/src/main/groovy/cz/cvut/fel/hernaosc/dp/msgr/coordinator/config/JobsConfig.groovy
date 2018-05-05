package cz.cvut.fel.hernaosc.dp.msgr.coordinator.config

import cz.cvut.fel.hernaosc.dp.msgr.coordinator.jobs.HealthCheckJob
import groovy.util.logging.Slf4j
import org.quartz.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@Slf4j
class JobsConfig {
    @Value('${coordinator.healthcheck.cron:#{null}}')
    private String cronExpression
    @Value('${coordinator.healthcheck.seconds:#{null}}')
    private Integer secondsPeriod

    @Bean
    JobDetail healthCheckJobDetail() {
        JobBuilder.newJob(HealthCheckJob.class).withIdentity("healthCheckJob").storeDurably().build()
    }

    @Bean
    Trigger healthCheckJobTrigger() {
        log.debug "Building trigger for HealthCheckJob using cron '$cronExpression' or seconds '$secondsPeriod'"

        def scheduleBuilder

        if (cronExpression) {
            try {
                scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression)
            } catch (RuntimeException ex) {
                log.error "Error building cron schedule '$cronExpression'", ex
            }
        }

        if (!scheduleBuilder) {
            // not checking the secondsPeriod for null is fine, we want startup to fail if no healthcheck period is set up
            scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(secondsPeriod).repeatForever()
        }

        TriggerBuilder.newTrigger().forJob(healthCheckJobDetail()).withIdentity("healthCheckTrigger").withSchedule(scheduleBuilder).build()
    }
}
