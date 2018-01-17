package cz.cvut.fel.hernaosc.dp.msgr.controller

import cz.cvut.fel.hernaosc.dp.msgr.core.service.MessagingService
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Device
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Platform
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ProofController {

	@Autowired
	MessagingService messagingService

	@RequestMapping("/test")
	boolean test(@RequestParam String title, @RequestParam String body) {
		def dev = new Device(
				token: "fS_AGuEgPQo:APA91bF_O9ElOGeJ2Vruv2CuFARuknJcjk_YazXAKIkDwGFi8v9AG2OBiobYmWquWbfZKRrMGxb6mcH6GDgZq4Zz4kQFuqhgvezNkqfctojKAaFjxHH5xo_oPTohWLnKoPP21BSiSvTL",
				platform: new Platform(name: "FCM")
		)
		messagingService.sendNotification(title, body, dev)
	}
}
