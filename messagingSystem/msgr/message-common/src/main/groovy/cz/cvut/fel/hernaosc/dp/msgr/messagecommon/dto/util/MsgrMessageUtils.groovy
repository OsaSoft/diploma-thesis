package cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.util

class MsgrMessageUtils {
    static <E> E randomElement(List<E> elements) {
        elements[new Random().nextInt(elements.size())]
    }
}
