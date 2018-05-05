package cz.cvut.fel.hernaosc.dp.msgr.fcm.adapter

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import spock.lang.Specification
import us.raudi.pushraven.Message
import us.raudi.pushraven.Notification
import us.raudi.pushraven.Pushraven

class FirebaseCloudMessagingAdapterTest extends Specification {

    FirebaseCloudMessagingAdapter adapter = new FirebaseCloudMessagingAdapter()

    void setup() {
    }

    void cleanup() {
    }

    def "When sending notification, it is pushed to PushRaven"() {
        given:
            GroovySpy(Pushraven, global: true)
            GroovySpy(Message, global: true)
            GroovySpy(Notification, global: true)
        and:
            def notification = Mock(Notification)
            def message = Mock(Message)
            def device = Mock(IDevice)
            device.token = "test-token"
        and:
            def title = "test-title"
            def body = "test-body"

        when: "a notification is sent through FCM adapter"
            def result = adapter.sendNotification(title, body, device)
        then: "a Pushraven notification is built"
            1 * new Notification() >> notification
            1 * notification.title(title) >> notification
            1 * notification.body(body) >> notification
        and: "a Pushraven message is built"
            1 * new Message() >> message
            1 * message.name(_) >> message
            1 * message.notification(notification) >> message
            1 * message.token(device.token) >> message
            1 * Pushraven.push(message) >> null
        and:
            result
    }

    def "When sending message, it is pushed to PushRaven"() {
        given:
            GroovySpy(Pushraven, global: true)
            GroovySpy(Message, global: true)
        and:
            def notification = Mock(Notification)
            def message = Mock(Message)
            def device = Mock(IDevice)
            device.token = "test-token"
        and:
            def title = "test-title"
            def body = "test-body"

        when: "a notification is sent through FCM adapter"
            def result = adapter.sendNotification(title, body, device)
        then: "a Pushraven notification is built"
            1 * new Notification() >> notification
            1 * notification.title(title) >> notification
            1 * notification.body(body) >> notification
        and: "a Pushraven message is built"
            1 * new Message() >> message
            1 * message.name(_) >> message
            1 * message.notification(notification) >> message
            1 * message.token(device.token) >> message
            1 * Pushraven.push(message) >> null
        and:
            result
    }
}
