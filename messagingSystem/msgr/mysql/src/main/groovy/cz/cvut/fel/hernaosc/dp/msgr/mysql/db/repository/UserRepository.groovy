package cz.cvut.fel.hernaosc.dp.msgr.mysql.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.repository.IUserRepository
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities.User
import org.springframework.data.repository.CrudRepository

interface UserRepository extends CrudRepository<User, String>, IUserRepository {

}