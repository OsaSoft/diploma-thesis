package cz.cvut.fel.hernaosc.dp.msgr.core.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform

interface IPlatformRepository extends IBaseRepository<IPlatform> {
    IPlatform findByName(String name)

    List<IPlatform> findAllByStateless(boolean stateless)
}