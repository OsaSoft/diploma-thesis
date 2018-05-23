package cz.cvut.fel.hernaosc.dp.msgr.core.db

interface IEntity {
	String getId()
	void setId(String id)

	Date getDateCreated()
}
