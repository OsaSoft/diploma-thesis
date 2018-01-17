package cz.cvut.fel.hernaosc.dp.msgr.mysql.db.entities

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IGroup
import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import cz.cvut.fel.hernaosc.dp.msgr.mysql.db.Entity
import groovy.transform.ToString

import javax.persistence.*

@javax.persistence.Entity
//group is a protected word, cant be table name
@Table(name = "msgr_group")
@ToString(includeNames = true, includePackage = false, includeSuper = true)
class Group extends Entity implements IGroup {
	@Column(unique = true)
	String name

	@ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
	@JoinTable(name = "group_user",
			joinColumns = [
					@JoinColumn(name = "group_id", referencedColumnName = "id")
			],
			inverseJoinColumns = [
					@JoinColumn(name = "user_id", referencedColumnName = "id")
			]
	)
	List<User> users = []

	@Override
	void setUsers(List<IUser> users) {
		this.users = users.collect { (User) it }
	}

	@Override
	void addUser(IUser user) {
		users << (User) user
	}
}
