package cz.cvut.fel.hernaosc.dp.msgr.core.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.IEntity

interface IEntityService {
    def <E extends IEntity> E findOrCreateById(String id, Class<E> clazz)

    def <E extends IEntity> E findOrCreateById(String id, Class<E> clazz, Map params)

    def <E extends IEntity> E findOrCreateByName(String name, Class<E> clazz)

    def <E extends IEntity> E findOrCreateByName(String name, Class<E> clazz, Map params)

    def <E extends IEntity> E create(Map params, Class<E> clazz)
}
