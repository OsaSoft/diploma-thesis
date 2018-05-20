package cz.cvut.fel.hernaosc.dp.msgr.perftest

import cz.cvut.fel.hernaosc.dp.msgr.javaclient.MsgrClient

class Util {
    static MsgrClient buildBaseClient(String address, boolean isWebsocket = false) {
        def client = new MsgrClient()
        client.with {
            url = address
            websocket = isWebsocket
            if (!isWebsocket) platformName = "test"
            serverRefresh = 100000
        }

        client
    }
}
