package cz.cvut.fel.hernaosc.dp.msgr.core.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser

interface IUserRepository extends IBaseRepository<IUser> {
    IUser findByName(String name)
}