package cz.cvut.fel.hernaosc.dp.msgr.core.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IGroup

interface IGroupRepository extends IBaseRepository<IGroup> {
    IGroup findByName(String name)
}