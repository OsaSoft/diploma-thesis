package cz.cvut.fel.hernaosc.dp.msgr.core.db.entities

import cz.cvut.fel.hernaosc.dp.msgr.core.db.IEntity

interface IDevice extends IEntity {
    String getToken()

    void setToken(String token)

    IPlatform getPlatform()

    void setPlatform(IPlatform platform)

    IUser getUser()

    void setUser(IUser user)
}
