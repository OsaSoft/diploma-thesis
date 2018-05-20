package cz.cvut.fel.hernaosc.dp.msgr.perftest

import cz.cvut.fel.hernaosc.dp.msgr.perftest.test.ConnectionTest
import cz.cvut.fel.hernaosc.dp.msgr.perftest.test.LagTest

class MsgrPerf {
    private static cmdList = [
            "testConnection",
            "perf"
    ]

    static void main(String... args) {
        if (!valid(args)) {
            println "Invalid arguments. Usage: MsgrPerf <cmd> <server address> <cmd args>"
            println "Available commands:"
            cmdList.each { println it }
            System.exit(1)
        }

        switch (cmdList.indexOf(args[0])) {
            case 0:
                new ConnectionTest(args[1..2] as String[]).run()
                break
            case 1:
                new LagTest(args[1]).run()
                break
        }
    }

    static boolean valid(String... args) {
        args.size() >= 2 && args[0] in cmdList
    }
}
