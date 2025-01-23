package org.pluginfile.hyperTPS;

import java.io.File;
import java.lang.management.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

public class SystemInfo {

    private static final String SYMBOL_GOOD = "✅";
    private static final String SYMBOL_WARNING = "⚠️";
    private static final String SYMBOL_CRITICAL = "❌";

    public String getMemoryInfo() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        return String.format(
                "%s Heap Bellek Kullanımı: %d MB, Maks: %d MB, Taahhüt Edilen: %d MB\n" +
                        "%s Non-Heap Bellek Kullanımı: %d MB, Maks: %d MB, Taahhüt Edilen: %d MB",
                SYMBOL_GOOD, heapMemoryUsage.getUsed() / (1024 * 1024), heapMemoryUsage.getMax() / (1024 * 1024), heapMemoryUsage.getCommitted() / (1024 * 1024),
                SYMBOL_GOOD, nonHeapMemoryUsage.getUsed() / (1024 * 1024), nonHeapMemoryUsage.getMax() / (1024 * 1024), nonHeapMemoryUsage.getCommitted() / (1024 * 1024)
        );
    }

    public String getCpuInfo() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        double cpuLoad = osBean.getSystemCpuLoad();
        double systemLoadAverage = osBean.getSystemLoadAverage();

        return String.format(
                "%s İşlemciler: %d Çekirdek\n%s Anlık CPU Yükü: %.2f%%\n%s Ortalama Sistem Yükü: %s",
                SYMBOL_GOOD, osBean.getAvailableProcessors(),
                (cpuLoad >= 0) ? SYMBOL_GOOD : SYMBOL_WARNING, (cpuLoad >= 0 ? cpuLoad * 100 : 0),
                (systemLoadAverage >= 0) ? SYMBOL_GOOD : SYMBOL_WARNING, (systemLoadAverage >= 0 ? String.format("%.2f", systemLoadAverage) : "Desteklenmiyor")
        );
    }

    public String getOperatingSystemInfo() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");

        return String.format(
                "%s İşletim Sistemi: %s (Sürüm: %s, Mimarisi: %s)",
                SYMBOL_GOOD, osName, osVersion, osArch
        );
    }

    public String getDiskSpaceInfo() {
        File root = new File("/");
        long totalSpace = root.getTotalSpace() / (1024 * 1024);
        long usableSpace = root.getUsableSpace() / (1024 * 1024);

        return String.format(
                "%s Toplam Disk Alanı: %d MB\n%s Kullanılabilir Disk Alanı: %d MB",
                SYMBOL_GOOD, totalSpace,
                SYMBOL_GOOD, usableSpace
        );
    }

    public String getJavaInfo() {
        Properties properties = System.getProperties();
        String javaVersion = properties.getProperty("java.version");
        String javaVendor = properties.getProperty("java.vendor");
        String javaHome = properties.getProperty("java.home");

        return String.format(
                "%s Java Versiyonu: %s\n%s Java Sağlayıcısı: %s\n%s Java Kurulum Dizini: %s",
                SYMBOL_GOOD, javaVersion,
                SYMBOL_GOOD, javaVendor,
                SYMBOL_GOOD, javaHome
        );
    }

    public String getThreadInfo() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadMXBean.getThreadCount();
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();

        return String.format(
                "%s Aktif Thread Sayısı: %d\n%s Zirve Thread Sayısı: %d\n%s Toplam Başlatılan Thread Sayısı: %d",
                SYMBOL_GOOD, threadCount,
                SYMBOL_GOOD, peakThreadCount,
                SYMBOL_GOOD, totalStartedThreadCount
        );
    }

    public String getJvmArguments() {
        List<String> jvmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

        StringBuilder arguments = new StringBuilder();
        arguments.append(SYMBOL_GOOD).append(" JVM Argümanları:\n");
        for (String arg : jvmArguments) {
            arguments.append("  - ").append(arg).append("\n");
        }
        return arguments.toString();
    }

    public String getMinecraftInfo() {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        String version = Bukkit.getBukkitVersion();
        String serverName = Bukkit.getServer().getName();

        return String.format(
                "%s Sunucu İsmi: %s\n%s Sürüm: %s\n%s Çevrimiçi Oyuncu Sayısı: %d/%d",
                SYMBOL_GOOD, serverName,
                SYMBOL_GOOD, version,
                SYMBOL_GOOD, onlinePlayers, maxPlayers
        );
    }

    public String getUptime() {
        long uptimeInMillis = ManagementFactory.getRuntimeMXBean().getUptime();

        long days = TimeUnit.MILLISECONDS.toDays(uptimeInMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeInMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeInMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeInMillis) % 60;

        return String.format(
                "%s Çalışma Süresi: %d gün, %d saat, %d dakika, %d saniye",
                SYMBOL_GOOD, days, hours, minutes, seconds
        );
    }

    public String getNetworkInfo() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return String.format(
                    "%s Sunucu IP: %s\n%s Sunucu Hostname: %s",
                    SYMBOL_GOOD, ip.getHostAddress(),
                    SYMBOL_GOOD, ip.getHostName()
            );
        } catch (UnknownHostException e) {
            return SYMBOL_CRITICAL + " Ağ Bilgisi Alınamadı: " + e.getMessage();
        }
    }

    public String getSystemInfo() {
        StringBuilder systemInfo = new StringBuilder();

        systemInfo.append("=== [HyperTPS] Sistem Bilgileri ===").append("\n");
        systemInfo.append(getOperatingSystemInfo()).append("\n");
        systemInfo.append(getCpuInfo()).append("\n");
        systemInfo.append(getMemoryInfo()).append("\n");
        systemInfo.append(getDiskSpaceInfo()).append("\n");
        systemInfo.append(getJavaInfo()).append("\n");
        systemInfo.append(getThreadInfo()).append("\n");
        systemInfo.append(getJvmArguments()).append("\n");
        systemInfo.append(getMinecraftInfo()).append("\n");
        systemInfo.append(getUptime()).append("\n");
        systemInfo.append(getNetworkInfo()).append("\n");
        systemInfo.append("========================").append("\n");

        System.out.println("HyperTPS - Sistem Detay Bilgileri:\n" + systemInfo);

        return systemInfo.toString();
    }
}