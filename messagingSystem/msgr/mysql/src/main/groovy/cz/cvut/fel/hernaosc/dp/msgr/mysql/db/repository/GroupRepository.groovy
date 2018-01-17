package cz.cvut.fel.hernaosc.dp.msgr.mysql.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IGroupRepository
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.Group
import org.springframework.data.repository.CrudRepository

interface GroupRepository extends CrudRepository<Group, String>, IGroupRepository {

}