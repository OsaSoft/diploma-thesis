package cz.cvut.fel.hernaosc.dp.msgr.mq.kafka

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.PropertySource

@EnableAutoConfiguration
@PropertySource(["classpath*:application.properties", "classpath*:application.yml"])
class KafkaApplication {
}
