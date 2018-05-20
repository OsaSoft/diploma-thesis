package cz.cvut.fel.hernaosc.dp.msgr.perftest.test

import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto
import cz.cvut.fel.hernaosc.dp.msgr.perftest.Util
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.consts.StatusCodes
import groovy.json.JsonSlurper

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ConnectionTest implements Runnable {
    static username = "connection-test"

    String url
    String target

    ConnectionTest(String... args) {
        (url, target) = args
    }

    @Override
    void run() {
        println "Running basic connection test"

        CountDownLatch latch = new CountDownLatch(1)
        long finishTime = -1

        def client = Util.buildBaseClient(url, true)

        client.deviceToken = "con-test-device"
        client.userName = username
        client.wsMessageListener = { String msg ->
            finishTime = System.currentTimeSeconds()

            def json = new JsonSlurper().parseText(msg)
            if (json.code == StatusCodes.WS_RECEIVED) {
                latch.countDown()
            }
        }

        client.init()
        println "Sending message...."

        def start = System.currentTimeSeconds()
        def msg = new NotificationDto(title: "test", body: "this is a test")

        switch (target) {
            case "device":
                msg.targetDevices = [client.deviceId]
                break
            case "user":
                msg.targetUsers = [client.userId]
                break
            default:
                println "Invalid target arg. Available targets: 'device', 'user'"
                System.exit(1)
                break
        }

        client.send(msg)

        def result = latch.await(10, TimeUnit.SECONDS)
        if (result) {
            println "Message delivered successfully after ${finishTime - start}ms"
        } else {
            println "Message delivery failed."
            System.exit(2)
        }

        client.disconnect()
        //keeps hanging here for some reason...
        System.exit(0)
    }
}
