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

class CommunicationTest implements Runnable {

    static final username = { id -> "communication-test-u$id" }

    int msgNum
    int delay
    String host1
    String host2

    CommunicationTest(String... args) {
        msgNum = Integer.parseInt(args[2])
        delay = Integer.parseInt(args[3])
        (host1, host2) = args.take(2)
    }

    @Override
    void run() {
        println "Running communication simulation test"
        println "Hosts are: $host1 and $host2}"

        CountDownLatch latch = new CountDownLatch(msgNum)
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
        [host1, host2].eachWithIndex { host, id ->
            def client = Util.buildBaseClient(host, true)
            client.deviceToken = "communication-test-dev$id"
            client.userName = username(id)
            client.wsMessageListener = listener

            client.init()
            clients << client
        }

        //wait a bit so all clients are initialized and topics are subscribed on nodes
        sleep(500)

        println "Starting sending..."
        msgNum.times {
            def client = MsgrMessageUtils.randomElement(clients)
            // flips the user id. If 0 => abs(-1) = 1. If 1 => abs(0) = 0
            def target = clients[(Integer.parseInt(client.userName[-1]) - 1).abs()].userId
            def msg = new DataMessageDto(targetUsers: [target], content: [msgId: UUID.randomUUID().toString()])
            msg.content.startTime = System.currentTimeSeconds()
            client.send(msg)
            sleep(delay)
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

            def file = new File("communication-test-${msgNum}-${delay}.csv").withWriter("UTF-8") {
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
