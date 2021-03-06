package cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IGroup
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.Entity

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.ManyToMany
import javax.persistence.OneToMany

@javax.persistence.Entity
//Cannot use Groovy @ToString, because that attempts to load relations
class User extends Entity implements IUser {
    @Column(unique = true)
    String name

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Device> devices = []

    @ManyToMany(mappedBy = "users", cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    List<Group> groups

    @Override
    void setDevices(List<IDevice> devices) {
        this.devices = devices.collect { (Device) it }
    }

    @Override
    void addDevice(IDevice device) {
        devices << (Device) device
    }

    @Override
    void setGroups(List<IGroup> groups) {
        this.groups = groups.collect { (Group) it }
    }

    @Override
    String toString() {
        return "User{" +
                "name='" + name + '\'' +
                "id=" + id +
                "dateCreated=" + dateCreated +
                '}'
    }
}
