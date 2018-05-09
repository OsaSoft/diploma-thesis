package cz.cvut.fel.hernaosc.dp.msgr.coordinator.common.sys

import javax.management.Attribute
import javax.management.AttributeList
import javax.management.MBeanServer
import javax.management.ObjectName
import java.lang.management.ManagementFactory

class SysUtils {
    static double getProcessCpuLoad() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer()
        ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem")
        AttributeList list = mbs.getAttributes(name, ["SystemCpuLoad"] as String[])

        if (list.isEmpty()) return Double.NaN

        Attribute att = (Attribute) list.get(0)
        Double value = (Double) att.getValue()

        // returns a percentage value with 1 decimal point precision
        return ((int) (value * 1000) / 10.0)
    }

    static SysMemory getMemoryStatus() {
        new SysMemory(total: Runtime.getRuntime().totalMemory(), free: Runtime.getRuntime().freeMemory())
    }

    /**
     * Requires Python and PSUtil to be installed on machine https://pypi.org/project/psutil/
     */
//    static getProcessCpuLoadPython() {
//        def cmd = ["python", "-c", "import psutil;print(psutil.cpu_times_percent(interval=0.5).system)"].execute()
//        cmd.waitForOrKill(1000)
//        def load
//        try {
//            load = (Float.parseFloat(cmd.text) / 100).round(3)
//        } catch (Exception ex) {
//            log.error "Error parsing system load '$cmd.text'", ex
//        }
//
//        load
//    }
}
