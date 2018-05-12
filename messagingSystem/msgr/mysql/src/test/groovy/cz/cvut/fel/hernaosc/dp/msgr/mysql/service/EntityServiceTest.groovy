package cz.cvut.fel.hernaosc.dp.msgr.mysql.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IGroup
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IGroupRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IPlatformRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IUserRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IEntityService
import cz.cvut.fel.hernaosc.dp.msgr.mysql.ContextAwareTest
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

class EntityServiceTest extends ContextAwareTest {

    @Autowired
    IDeviceRepository deviceRepository
    @Autowired
    IGroupRepository groupRepository
    @Autowired
    IPlatformRepository platformRepository
    @Autowired
    IUserRepository userRepository

    @Autowired
    IEntityService entityService

    void setup() {
    }

    void cleanup() {
    }

    @Unroll
    def "can create and save new instances for #clazz"() {
        given:
            def entity = entityService.create(params, clazz)

        when:
            def result = this."${repoName}Repository".findById(entity.id)
        then:
            result
            result.get()."${params.keySet()[0]}" == params["${params.keySet()[0]}"]

        where:
            clazz     | params                 | repoName
            IPlatform | [name: "testPlatform"] | "platform"
            IDevice   | [token: "testDevice"]  | "device"
            IUser     | [name: "testUser"]     | "user"
            IGroup    | [name: "testGroup"]    | "group"
    }
}
