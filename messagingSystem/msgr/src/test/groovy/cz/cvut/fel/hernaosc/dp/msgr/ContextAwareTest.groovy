package cz.cvut.fel.hernaosc.dp.msgr

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("test")
/**
 * Abstract test class that uses "test" profile and has full Spring context
 *
 */
abstract class ContextAwareTest extends Specification {
}
