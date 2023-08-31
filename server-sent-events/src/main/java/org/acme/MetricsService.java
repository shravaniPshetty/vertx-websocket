package org.acme;

import jakarta.inject.Singleton;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Set;

@Singleton
public class MetricsService {
    private static final String OPERATING_SYSTEM_MBEAN = "java.lang:type=OperatingSystem";

    public RuntimeMetrics getRuntimeMetrics() {
        return new RuntimeMetrics(
                getMemoryUsed(),
                getOpenFileDescriptors(),
                getProcessCpuUsage(),
                getSystemCpuUsage()
        );
    }

    double getMemoryUsed() {
        double free = getMBeanAttribute("java.lang:type=OperatingSystem", "FreePhysicalMemorySize", Long.class);
        double total = getMBeanAttribute("java.lang:type=OperatingSystem", "TotalPhysicalMemorySize", Long.class);
        return (total - free) / (1024 * 1024);
    }

    long getOpenFileDescriptors() {
        return getMBeanAttribute("java.lang:type=OperatingSystem", "OpenFileDescriptorCount", Long.class);
    }

    String getProcessCpuUsage() {
        double value = getMBeanAttribute("java.lang:type=OperatingSystem", "ProcessCpuLoad", Double.class);
        return String.format("%.2f", value * 100);
    }

    String getSystemCpuUsage() {
        double value = getMBeanAttribute(OPERATING_SYSTEM_MBEAN, "SystemCpuLoad", Double.class);
        return String.format("%.2f", value * 100);
    }

    private MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }

    private ObjectInstance getMBean(String name) {
        try {
            ObjectName objectName = new ObjectName(name);
            Set<ObjectInstance> mbeans = getMBeanServer().queryMBeans(objectName, null);
            Iterator<ObjectInstance> iterator = mbeans.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
            return null;
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T getMBeanAttribute(String objectName, String attribute, Class<T> returnType) {
        try {
            return returnType.cast(getMBeanServer().getAttribute(getMBean(objectName).getObjectName(), attribute));
        } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException e) {
            throw new RuntimeException(e);
        }
    }
}
