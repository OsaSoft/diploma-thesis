package cz.cvut.fel.hernaosc.dp.msgr.mysql

import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IGroupRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IPlatformRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IUserRepository
import org.springframework.beans.factory.annotation.Autowired
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
    @Autowired
    IDeviceRepository deviceRepository
    @Autowired
    IGroupRepository groupRepository
    @Autowired
    IPlatformRepository platformRepository
    @Autowired
    IUserRepository userRepository

    void cleanup() {
        ["device", "group", "platform", "user"].each { repoName ->
            this."${repoName}Repository".deleteAll()
        }
    }
}
