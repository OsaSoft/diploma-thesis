package cz.cvut.fel.hernaosc.dp.msgr

import cz.cvut.fel.hernaosc.dp.msgr.core.service.AdapterService
import org.springframework.beans.factory.annotation.Autowired

class FirebaseCloudMessagingAdapterIntegrationTest extends ContextAwareTest {
	void setup() {
	}

	void cleanup() {
	}

	@Autowired
	AdapterService adapterService

	def "Adapter is found and registered by AdapterService"() {
		expect:
			adapterService.adapters["FCM"]
	}
}
