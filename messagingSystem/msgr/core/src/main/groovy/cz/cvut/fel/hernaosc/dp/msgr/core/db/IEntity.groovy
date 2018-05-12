package cz.cvut.fel.hernaosc.dp.msgr.core.db

abstract interface IEntity {
	String getId()
	void setId(String id)

	Date getDateCreated()
}
