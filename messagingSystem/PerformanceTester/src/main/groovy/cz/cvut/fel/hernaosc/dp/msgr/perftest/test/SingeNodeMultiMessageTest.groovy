package cz.cvut.fel.hernaosc.dp.msgr.perftest.test

import cz.cvut.fel.hernaosc.dp.msgr.javaclient.MsgrClient
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.perftest.Util
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.consts.StatusCodes
import groovy.json.JsonSlurper

import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class SingeNodeMultiMessageTest implements Runnable {
    static username = "single-node-multi-msg-test"

    String url
    String target
    int msgNum

    SingeNodeMultiMessageTest(String... args) {
        (url, target) = args[0..1]
        msgNum = Integer.parseInt(args[2])
    }

    @Override
    void run() {
        println "Running single node multiple messages test"
        println "Number of messages to send: $msgNum"

        CountDownLatch latch = new CountDownLatch(msgNum)
        def startFinishTimes = new ConcurrentLinkedDeque<>()

        def client = Util.buildBaseClient(url, true)
        MsgrClient.setLoggerLevel(Level.SEVERE)

        client.deviceToken = "con-test-device"
        client.userName = username
        client.wsMessageListener = { String msg ->
            def json = new JsonSlurper().parseText(msg)
            if (json.code == StatusCodes.WS_RECEIVED) {
                startFinishTimes << [json.payload.startTime, System.currentTimeSeconds()]
                latch.countDown()
            }
        }

        client.init()

        def msg = new DataMessageDto()

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

        println "Starting sending..."
        msgNum.times {
            msg.content = [startTime: System.currentTimeSeconds()]
            client.send(msg)
        }

        def result = latch.await(20, TimeUnit.SECONDS)
        if (result) {
            println "All messages delivered successfully"

            def times = []
            startFinishTimes.each {
                times << it[1] - it[0]
            }
            def average = times.sum() / times.size()

            println "Delivery times: "
            println times.collect { it + "ms" }

            println "Average delivery time: ${average}ms"

            def file = new File("single-node-multi-msg-$target-${msgNum}.csv").withWriter("UTF-8") {
                it.writeLine("Times;" + times.join(";"))
                it.writeLine("Avg;$average")
            }

        } else {
            println "Message delivery failed."
            System.exit(2)
        }

        client.disconnect()
    }
}
