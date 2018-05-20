package cz.cvut.fel.hernaosc.dp.msgr.perftest.test

import cz.cvut.fel.hernaosc.dp.msgr.javaclient.MsgrClient

class LagTest implements Runnable {
    String url

    MsgrClient client1
    MsgrClient client2

    LagTest(String url) {
        this.url = url
    }

    @Override
    void run() {

    }
}
