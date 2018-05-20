package cz.cvut.fel.hernaosc.dp.msgr.core.mq

import cz.cvut.fel.hernaosc.dp.msgr.core.dto.MqStatsDto

interface IMqStats {
    MqStatsDto getStats()
}