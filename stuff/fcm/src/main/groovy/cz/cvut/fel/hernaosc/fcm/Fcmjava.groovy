package cz.cvut.fel.hernaosc.fcm

import de.bytefish.fcmjava.client.FcmClient
import de.bytefish.fcmjava.constants.Constants
import de.bytefish.fcmjava.http.options.IFcmClientSettings
import de.bytefish.fcmjava.model.options.FcmMessageOptions
import de.bytefish.fcmjava.requests.data.DataUnicastMessage
import de.bytefish.fcmjava.requests.notification.NotificationPayload
import de.bytefish.fcmjava.responses.FcmMessageResponse

import java.time.Duration

class Main {
	static scanner = new Scanner(System.in)

	static main(args) {
		def deviceToken = "dLiuo9sUqjg:APA91bF54ebHPZk4K7EWYSow6fufXAk8ZkMhjO2efiX6It67T7-njLX4cxyH_EQhQgTkkmHdzG6EhEIFkaFjSWAKwcrrpHkR7lTfMHqlQWdYxAbf5dWVGb0CvQBJkgV0WXXYrvV3EmbU"
		def fcmClient = new FcmClient(new CustomFcmClientSettings())

		def opts = FcmMessageOptions.builder()
				.setTimeToLive(Duration.ofHours(1))
				.build()

		def notification = NotificationPayload.builder()
				.setTitle("test")
				.build()
		def msg = new DataUnicastMessage(opts, deviceToken, "hello world", notification)
		FcmMessageResponse response = fcmClient.send(msg)
		println response
	}

	static readIn(message) {
		println "$message:"
		scanner.next()
	}
}

class CustomFcmClientSettings implements IFcmClientSettings {
	@Override
	String getApiKey() {
		"AAAAlIHdDog:APA91bG9xrVkhp5F0mnm2vq1wt4APsj4XBXV0Cb7uN4LWZygNWuKsDzpil9srgvmvZVNMNQI1cQxpLJusaUQo9EyOq2pL_3ovtp2A6k8-XmLd6oeTbD8Dp14py9_9H2q8NaGamZWxYyY"
	}

	@Override
	String getFcmUrl() {
		Constants.FCM_URL
	}
}
