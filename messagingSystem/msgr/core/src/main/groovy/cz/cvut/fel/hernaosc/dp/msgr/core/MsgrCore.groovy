package cz.cvut.fel.hernaosc.dp.msgr.core

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource

@EnableAutoConfiguration
@PropertySource(["classpath*:application.properties", "classpath*:application.yml"])
class MsgrCore {
}

@EnableAutoConfiguration
@Profile("test")
class MsgrCoreTest {
}
