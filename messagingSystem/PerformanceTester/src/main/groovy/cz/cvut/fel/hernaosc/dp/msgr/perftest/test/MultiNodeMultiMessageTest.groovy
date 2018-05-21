package cz.cvut.fel.hernaosc.dp.msgr.perftest.test

import cz.cvut.fel.hernaosc.dp.msgr.javaclient.MsgrClient
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.DataMessageDto
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.util.MsgrMessageUtils
import cz.cvut.fel.hernaosc.dp.msgr.perftest.Util
import cz.cvut.fel.hernaosc.dp.msgr.websocket.common.consts.StatusCodes
import groovy.json.JsonSlurper

import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class MultiNodeMultiMessageTest implements Runnable {
    static username = "multi-node-multi-msg-test"

    List<String> urls = []
    int msgNum

    MultiNodeMultiMessageTest(String... args) {
        msgNum = Integer.parseInt(args[0])
        urls.addAll(args.drop(1))
    }

    @Override
    void run() {
        println "Running multiple node multiple messages test"
        println "Number of nodes: ${urls.size()}"
        println "Number of messages to send: $msgNum"

        CountDownLatch latch = new CountDownLatch(msgNum * urls.size())
        def startFinishTimes = new ConcurrentLinkedDeque<>()

        MsgrClient.setLoggerLevel(Level.SEVERE)

        def listener = { String msg ->
            def json = new JsonSlurper().parseText(msg)
            if (json.code == StatusCodes.WS_RECEIVED) {
                startFinishTimes << [json.payload.startTime, System.currentTimeSeconds()]
                latch.countDown()
            }
        }

        def clients = []
        urls.each {
            def deviceToken = it[-2..-1]
            def client = Util.buildBaseClient(it, true)
            client.deviceToken = "con-test-device-node-$deviceToken"
            client.userName = username
            client.wsMessageListener = listener

            client.init()
            clients << client
        }

        //wait a bit so all clients are initialized and topics are subscribed on nodes
        sleep(1000)

        println "Starting sending..."
        msgNum.times {
            def client = MsgrMessageUtils.randomElement(clients)
            def msg = new DataMessageDto(targetUsers: [client.userId], content: [msgId: UUID.randomUUID().toString()])
            msg.content.startTime = System.currentTimeSeconds()
            client.send(msg)
            sleep(50)
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

            def file = new File("multi-node-multi-msg-${urls.size()}-${msgNum}.csv").withWriter("UTF-8") {
                it.writeLine("Times;" + times.join(";"))
                it.writeLine("Avg;$average")
            }

        } else {
            println "Message delivery failed. Latches left: ${latch.count}"
            System.exit(2)
        }

        clients.each { it.disconnect() }
    }
}
