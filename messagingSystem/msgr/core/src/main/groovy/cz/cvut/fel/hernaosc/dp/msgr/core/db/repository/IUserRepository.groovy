package cz.cvut.fel.hernaosc.dp.msgr.core.db.repository

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IUser

interface IUserRepository extends IBaseRepository<IUser> {
    IUser findByName(String name)

    /**
     * Not supported out of the box by Spring JPA. Modules implementing the DB functionality
     * need to implement this method for the specific platform
     * @param groupId
     * @return List of user IDs
     */
    List<String> getUserIdsByGroup(String groupId)
}