package cz.cvut.fel.hernaosc.dp.msgr.core.service

import cz.cvut.fel.hernaosc.dp.msgr.core.db.entities.IPlatform
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.IPlatformAdapter
import cz.cvut.fel.hernaosc.dp.msgr.core.platform.PlatformAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Service
class AdapterService implements IAdapterService {
    @Autowired
    private ApplicationContext applicationContext

    private adapters = [:]

    @PostConstruct
    void init() {
        applicationContext.getBeansWithAnnotation(PlatformAdapter).each { name, bean ->
            IPlatformAdapter adapter = (IPlatformAdapter) bean
            String adapterName = AnnotationUtils.findAnnotation(adapter.class, PlatformAdapter).value()
            adapters[adapterName] = adapter
        }
    }

    IPlatformAdapter getAdapter(IPlatform platform) {
        adapters[platform.name]
    }
}
