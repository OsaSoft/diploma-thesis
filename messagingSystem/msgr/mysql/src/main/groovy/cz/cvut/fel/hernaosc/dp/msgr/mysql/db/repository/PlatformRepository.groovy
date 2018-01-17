package cz.cvut.fel.hernaosc.dp.msgr.mysql.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IPlatformRepository
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Platform
import org.springframework.data.repository.CrudRepository

interface PlatformRepository extends CrudRepository<Platform, String>, IPlatformRepository {

}