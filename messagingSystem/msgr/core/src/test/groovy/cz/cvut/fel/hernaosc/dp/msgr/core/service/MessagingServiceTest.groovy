package cz.cvut.fel.hernaosc.dp.msgr.core.service

import spock.lang.Specification
import spock.lang.Unroll

class MessagingServiceTest extends Specification {
    MessagingService messagingService = new MessagingService()

    void setup() {
    }

    void cleanup() {
    }

    @Unroll
    def "splits into number of pages correctly"() {
        given:
            messagingService.pageSize = 20

        expect:
            messagingService.numPages(count) == expected

        where:
            count | expected
            5     | 1
            10    | 1
            19    | 1
            20    | 1
            21    | 2
            27    | 2
            30    | 2
            40    | 2
            45    | 3
            60    | 3
    }
}
