package cz.cvut.fel.hernaosc.dp.msgr.core.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.IPlatformAdapter

interface IAdapterService {
    IPlatformAdapter getAdapter(IPlatform platform)
}
