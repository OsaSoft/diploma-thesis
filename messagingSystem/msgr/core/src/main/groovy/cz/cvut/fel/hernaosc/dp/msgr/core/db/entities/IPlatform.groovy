package cz.cvut.fel.hernaosc.dp.msgr.core.db.entities

import cz.cvut.fel.hernaosc.dp.msgr.core.db.IEntity

interface IPlatform extends IEntity {
    String getName()

    void setName(String name)

    boolean isStateless()

    void setStateless(boolean stateless)

    List<IDevice> getDevices()

    void setDevices(List<IDevice> devices)

    void addDevice(IDevice device)
}
