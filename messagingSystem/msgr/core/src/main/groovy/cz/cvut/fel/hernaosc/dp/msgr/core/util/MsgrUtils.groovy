package cz.cvut.fel.hernaosc.dp.msgr.core.util

import java.util.function.Function

class MsgrUtils {
    static <T> T fromOptional(Optional<T> optional) {
        optional.present ? optional.get() : null
    }

    static <K, V> Map<K, ?> flattenMap(Map<K, ?> map, String separator = '_', Function<?, V> valueProcessor = {
        it.toString()
    }) {
        map.collectEntries { k, v ->
            switch (v) {
                case Map:
                    flattenMap(v, separator, valueProcessor).collectEntries { q, r ->
                        [(k + separator + q): r]
                    }
                    break
                case Collection:
                    int i = 0
                    flattenCollection(v.flatten(), valueProcessor).collectEntries {
                        [(k + separator + i++): it]
                    }
                    break
                default:
                    [(k): valueProcessor.apply(v)]
                    break
            }
        } as Map<K, V>
    }

    static <T, V> Collection flattenCollection(Collection<T> col, Function<?, V> valueProcessor = { it.toString() }) {
        col.collect { elem ->
            switch (elem) {
                case Collection:
                    flattenCollection(elem, valueProcessor)
                    break
                default:
                    valueProcessor.apply(elem)
                    break
            }
        }
    }
}
