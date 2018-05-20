package cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.Entity
import groovy.transform.ToString

import javax.persistence.Column
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@javax.persistence.Entity
@ToString(includeSuper = true, includeNames = true, includePackage = false)
class Device extends Entity implements IDevice {
    @Column(unique = true)
    String token

    //eager fetching since most of the time, getting a device we will also want to know the platform
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "platform_id")
    Platform platform

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user

    @Override
    void setPlatform(IPlatform platform) {
        this.platform = (Platform) platform
    }

    @Override
    void setUser(IUser user) {
        this.user = (User) user
    }
}

