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
    def "Can create and save new instances for #clazz"() {
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

    @Unroll
    def "Params can be used to set ID when creating #clazz"() {
        given:
            def id = "test id for $clazz"

        when:
            def entity = entityService.create([id: id], clazz)
        then:
            entity.id == id

        where:
            clazz << [IDevice, IPlatform, IUser, IGroup]
    }

    def "FindOrCreate can find existing instance"() {
        given:
            def params = [id: "testId", name: "testName"]
            def user = entityService.create(params, IUser)

        expect:
            entityService.findOrCreateById(params.id, IUser).id == params.id
        and:
            entityService.findOrCreateByName(params.name, IUser).name == params.name
    }

    def "FindOrCreate can create new if existing isnt found"() {
        given:
            def id = "test id"
            def name = "test name"

        when:
            def byId = entityService.findOrCreateById(id, IUser)
        then:
            byId.id == id

        when:
            def byName = entityService.findOrCreateByName(name, IUser)
        then:
            byName.name == name
        and:
            byId != byName
    }
}
