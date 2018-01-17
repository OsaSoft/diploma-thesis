package cz.cvut.fel.hernaosc.dp.msgr.mysql

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest(classes = [MsgrMysqlTest.class])
@ActiveProfiles("test")
/**
 * Abstract test class that uses "test" profile and has full Spring context
 *
 */
abstract class ContextAwareTest extends Specification {
}
