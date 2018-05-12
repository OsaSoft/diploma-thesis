package cz.cvut.fel.hernaosc.dp.msgr.core.util

class MsgrUtils {
    static <T> T fromOptional(Optional<T> optional) {
        optional.present ? optional.get() : null
    }
}
