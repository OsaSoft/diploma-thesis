package cz.cvut.fel.hernaosc.fcm

import us.raudi.pushraven.Notification
import us.raudi.pushraven.Pushraven

class PshRvn {
	static scanner = new Scanner(System.in)
	static serverKey = "AAAAlIHdDog:APA91bG9xrVkhp5F0mnm2vq1wt4APsj4XBXV0Cb7uN4LWZygNWuKsDzpil9srgvmvZVNMNQI1cQxpLJusaUQo9EyOq2pL_3ovtp2A6k8-XmLd6oeTbD8Dp14py9_9H2q8NaGamZWxYyY"
	static deviceToken = "dLiuo9sUqjg:APA91bF54ebHPZk4K7EWYSow6fufXAk8ZkMhjO2efiX6It67T7-njLX4cxyH_EQhQgTkkmHdzG6EhEIFkaFjSWAKwcrrpHkR7lTfMHqlQWdYxAbf5dWVGb0CvQBJkgV0WXXYrvV3EmbU"

	static main(args) {
		def text = readIn("Input text to be sent:")
		Pushraven.setKey(serverKey)

		//also can use Pushraven.notification.title("title").to("")
		def raven = new Notification()
		raven.title("test1").text(text).to(deviceToken)

		Pushraven.push(raven)

		//apparently more effective than creating a new one
		raven.clear()
	}

	static readIn(message) {
		println message
		scanner.nextLine()
	}
}
