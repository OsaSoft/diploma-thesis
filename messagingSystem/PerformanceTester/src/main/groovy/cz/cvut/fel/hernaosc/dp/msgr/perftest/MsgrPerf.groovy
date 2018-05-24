package cz.cvut.fel.hernaosc.dp.msgr.perftest

import cz.cvut.fel.hernaosc.dp.msgr.perftest.test.CommunicationTest
import cz.cvut.fel.hernaosc.dp.msgr.perftest.test.ConnectionTest
import cz.cvut.fel.hernaosc.dp.msgr.perftest.test.MultiNodeMultiMessageTest
import cz.cvut.fel.hernaosc.dp.msgr.perftest.test.SingeNodeMultiMessageTest

class MsgrPerf {
    private static cmdList = [
            "testConnection",
            "multiMessage",
            "multiNodeMessage",
            "communication",
            "help"
    ]

    private static man = [
            "testConnection"  : "<url> <target> \n Valid targets: 'device', 'user'",
            "multiMessage"    : "<url> <target> <numMessages> \n Valid targets: 'device', 'user'",
            "multiNodeMessage": "<numMessages> <list of node urls>",
            "communication"   : "<url1> <url2> <numMessages> <delay between messages>",
            "help"            : "<cmd>"
    ]

    static void main(String... args) {
        if (!valid(args)) {
            println "Invalid arguments. Usage: MsgrPerf <cmd> <args>"
            println "Available commands:"
            println "For details on each command run MsgrPerf help <cmd>"
            cmdList.each { println it }
            System.exit(1)
        }

        switch (cmdList.indexOf(args[0])) {
            case 0:
                new ConnectionTest(args[1..2] as String[]).run()
                break
            case 1:
                new SingeNodeMultiMessageTest(args[1..3] as String[]).run()
                break
            case 2:
                new MultiNodeMultiMessageTest(args.drop(1)).run()
                break
            case 3:
                new CommunicationTest(args[1..4] as String[]).run()
                break
            case 4:
                println "Usage: ${args[1]} ${man[args[1]]}"
                System.exit(1)
                break
        }
    }

    static boolean valid(String... args) {
        args.size() >= 2 && args[0] in cmdList
    }
}
