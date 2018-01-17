package cz.cvut.fel.hernaosc.dp.msgr.mysql.db

import cz.cvut.fel.hernaosc.dp.msgr.core.db.IEntity
import groovy.transform.ToString
import org.hibernate.annotations.GenericGenerator

import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType

@javax.persistence.Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@ToString(includeNames = true, includePackage = false)
abstract class Entity implements IEntity {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	String id
	Date dateCreated = new Date()

	String getId() {
		return id
	}

	Date getDateCreated() {
		return dateCreated
	}
}

