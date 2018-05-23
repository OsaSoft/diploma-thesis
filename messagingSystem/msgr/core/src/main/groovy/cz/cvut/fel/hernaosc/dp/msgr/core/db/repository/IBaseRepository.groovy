package cz.cvut.fel.hernaosc.dp.msgr.core.db.repository

interface IBaseRepository<E> {
    E save(E entity)

    Optional<E> findById(String id)

    List<E> findAllById(List<String> ids)
}