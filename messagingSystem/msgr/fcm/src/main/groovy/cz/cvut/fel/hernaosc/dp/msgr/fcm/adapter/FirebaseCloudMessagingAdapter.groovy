package cz.cvut.fel.hernaosc.dp.msgr.fcm.adapter

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.IPlatformAdapter
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.PlatformAdapter
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import us.raudi.pushraven.FcmResponse
import us.raudi.pushraven.Message
import us.raudi.pushraven.Notification
import us.raudi.pushraven.Pushraven

import javax.annotation.PostConstruct

@Component
@PlatformAdapter("FCM")
@Slf4j
class FirebaseCloudMessagingAdapter implements IPlatformAdapter {

	@Value('${msgr.adapter.fcm.projectId:#{null}}')
	private String projectId

	@Value('${msgr.adapter.fcm.serviceAccountFilename:#{null}}')
	private String serviceAccountFilename

	@PostConstruct
	void init() {
		if (!projectId || !serviceAccountFilename) {
			log.error "FCM not configured"
			return
		}

		try {
			Pushraven.accountFile = new ClassPathResource(serviceAccountFilename).getFile()
			Pushraven.projectId = projectId
		} catch (IOException ex) {
			log.error("Could dont find FCM service account file called '$serviceAccountFilename'", ex)
		}
	}

	@Override
	boolean sendNotification(String title, String body, IDevice device) {
		if (!device) {
			log.error("Attempting to send notification to null device")
			return false
		}

		Notification notification =
				new Notification()
						.title(title)
						.body(body)

		Message msg =
				new Message()
						.name("TODO")
						.notification(notification)
						.token(device.token)

		FcmResponse response = Pushraven.push(msg)
		println response
		true
	}

	@Override
	boolean sendMessage(Map payload, IDevice device) {
		if (!device) {
			log.error("Attempting to send notification to null device")
			return false
		}

		Message msg = new Message()
				.name("TODO")
				.data(payload)
				.token(device.token)

		FcmResponse response = Pushraven.push(msg)
		println response
		true
	}
}
