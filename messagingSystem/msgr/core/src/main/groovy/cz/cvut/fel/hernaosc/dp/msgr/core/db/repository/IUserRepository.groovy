package cz.cvut.fel.hernaosc.dp.msgr.core.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface IUserRepository extends IBaseRepository<IUser> {
    IUser findByName(String name)

    Page<IUser> findAllByGroups_Id(String groupId, Pageable pageable)

    long countByGroups_Id(String groupId)
}