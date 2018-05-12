package cz.cvut.fel.hernaosc.dp.msgr.mysql

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource

@EnableAutoConfiguration
@PropertySource(["classpath*:application.properties", "classpath*:application.yml"])
class MsgrMysql {
}

@EnableAutoConfiguration
@ComponentScan(basePackages = "cz.cvut.fel.hernaosc.dp.msgr.mysql")
@Profile("test")
class MsgrMysqlTest {
}
