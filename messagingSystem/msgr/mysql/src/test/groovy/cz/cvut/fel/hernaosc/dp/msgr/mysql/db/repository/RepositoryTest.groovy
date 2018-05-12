package cz.cvut.fel.hernaosc.dp.msgr.mysql.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IDeviceRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IGroupRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IPlatformRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IUserRepository
import cz.cvut.fel.hernaosc.dp.msgr.core.service.IEntityService
import cz.cvut.fel.hernaosc.dp.msgr.mysql.ContextAwareTest
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Device
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Group
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Platform
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.User
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

class RepositoryTest extends ContextAwareTest {
	def setup() {}

	def cleanup() {}

	@Autowired
	IDeviceRepository deviceRepository
	@Autowired
	IGroupRepository groupRepository
	@Autowired
	IPlatformRepository platformRepository
	@Autowired
	IUserRepository userRepository

	@Unroll
	def "#repoName Repository is properly injected by Spring container"() {
		given:
			def repo = this."${repoName}Repository"

		when:
			repo.save(instance)
		then:
			repo.findById(instance.id).present

		where:
			repoName   | instance
			"device"   | new Device(token: "token123")
			"group"    | new Group(name: "testGroup")
			"platform" | new Platform(name: "testPlatform")
			"user"     | new User(name: "testUser")
	}
}
