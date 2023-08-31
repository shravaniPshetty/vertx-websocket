package org.acme;

import java.time.Instant;

public class RuntimeMetrics {
    private final double memoryUsed;
    private final long openFileDescriptors;
    private final String processCpuUsage;
    private final String systemCpuUsage;
    private final long timestamp = Instant.now().toEpochMilli();

    public RuntimeMetrics(double memoryUsed, long openFileDescriptors, String processCpuUsage, String systemCpuUsage) {
        this.memoryUsed = memoryUsed;
        this.openFileDescriptors = openFileDescriptors;
        this.processCpuUsage = processCpuUsage;
        this.systemCpuUsage = systemCpuUsage;
    }

    public double getMemoryUsed() {
        return memoryUsed;
    }

    public long getOpenFileDescriptors() {
        return openFileDescriptors;
    }

    public String getProcessCpuUsage() {
        return processCpuUsage;
    }

    public String getSystemCpuUsage() {
        return systemCpuUsage;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
