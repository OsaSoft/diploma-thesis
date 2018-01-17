package cz.cvut.fel.hernaosc.dp.msgr.core.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform

interface IPlatformRepository {
	IPlatform findByName(String name)
}