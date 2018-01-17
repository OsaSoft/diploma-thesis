package cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IDevice
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.Entity
import groovy.transform.ToString

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.OneToMany

@javax.persistence.Entity
@ToString(includeNames = true, includePackage = false, includeSuper = true)
class Platform extends Entity implements IPlatform {
	/**
	 * IPlatform name
	 */
	@Column(unique = true)
	String name
	//TODO: String adapter class name? Use convention instead, ie, try to find a "NameAdaptor"?

	@OneToMany(mappedBy = "platform", cascade = CascadeType.ALL, orphanRemoval = true)
	List<Device> devices = []

	@Override
	void setDevices(List<IDevice> devices) {
		this.devices = devices.collect { (Device) it }
	}

	@Override
	void addDevice(IDevice device) {
		devices << (Device) device
	}
}
