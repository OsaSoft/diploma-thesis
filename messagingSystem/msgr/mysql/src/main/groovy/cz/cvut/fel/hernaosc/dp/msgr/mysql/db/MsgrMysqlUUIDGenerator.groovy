package cz.cvut.fel.hernaosc.dp.msgr.mysql.db

import org.hibernate.HibernateException
import org.hibernate.MappingException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.UUIDGenerator
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.Type

class MsgrMysqlUUIDGenerator extends UUIDGenerator {
    private String entityName

    @Override
    void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        entityName = params[ENTITY_NAME]
        super.configure(type, params, serviceRegistry)
    }

    @Override
    Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Serializable id = session.getEntityPersister(entityName, object).getIdentifier(object, session)

        id ?: super.generate(session, object)
    }
}
