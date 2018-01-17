package cz.cvut.fel.hernaosc.dp.msgr.fcm.adapter.cz.cvut.fel.hernaosc.dp.msgr.fcm

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.PropertySource

@EnableAutoConfiguration
@PropertySource(["classpath*:application.properties", "classpath*:application.yml"])
class Fcm {
}
