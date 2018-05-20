package cz.cvut.fel.hernaosc.dp.msgr.core.db.entities

import cz.cvut.fel.hernaosc.dp.msgr.core.db.IEntity

interface IGroup extends IEntity {
    String getName()

    void setName(String name)

    List<IUser> getUsers()

    void setUsers(List<IUser> users)

    void addUser(IUser user)
}
