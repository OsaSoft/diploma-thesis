package cz.cvut.fel.hernaosc.dp.msgr.core.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.IEntity

interface IEntityService {
    IEntity findOrCreateById(String id, Class clazz)

    IEntity findOrCreateById(String id, Class clazz, Map params)

    IEntity findOrCreateByName(String name, Class clazz)

    IEntity findOrCreateByName(String name, Class clazz, Map params)

    IEntity create(Map params, Class clazz)
}
