package org.pluginfile.hyperTPS;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
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
      return String.format("%s Heap Bellek Kullanımı: %d MB, Maks: %d MB, Taahhüt Edilen: %d MB\n%s Non-Heap Bellek Kullanımı: %d MB, Maks: %d MB, Taahhüt Edilen: %d MB", "✅", heapMemoryUsage.getUsed() / 1048576L, heapMemoryUsage.getMax() / 1048576L, heapMemoryUsage.getCommitted() / 1048576L, "✅", nonHeapMemoryUsage.getUsed() / 1048576L, nonHeapMemoryUsage.getMax() / 1048576L, nonHeapMemoryUsage.getCommitted() / 1048576L);
   }

   public String getCpuInfo() {
      OperatingSystemMXBean osBean = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
      double cpuLoad = osBean.getSystemCpuLoad();
      double systemLoadAverage = osBean.getSystemLoadAverage();
      return String.format("%s İşlemciler: %d Çekirdek\n%s Anlık CPU Yükü: %.2f%%\n%s Ortalama Sistem Yükü: %s", "✅", osBean.getAvailableProcessors(), cpuLoad >= 0.0D ? "✅" : "⚠️", cpuLoad >= 0.0D ? cpuLoad * 100.0D : 0.0D, systemLoadAverage >= 0.0D ? "✅" : "⚠️", systemLoadAverage >= 0.0D ? String.format("%.2f", systemLoadAverage) : "Desteklenmiyor");
   }

   public String getOperatingSystemInfo() {
      String osName = System.getProperty("os.name");
      String osVersion = System.getProperty("os.version");
      String osArch = System.getProperty("os.arch");
      return String.format("%s İşletim Sistemi: %s (Sürüm: %s, Mimarisi: %s)", "✅", osName, osVersion, osArch);
   }

   public String getDiskSpaceInfo() {
      File root = new File("/");
      long totalSpace = root.getTotalSpace() / 1048576L;
      long usableSpace = root.getUsableSpace() / 1048576L;
      return String.format("%s Toplam Disk Alanı: %d MB\n%s Kullanılabilir Disk Alanı: %d MB", "✅", totalSpace, "✅", usableSpace);
   }

   public String getJavaInfo() {
      Properties properties = System.getProperties();
      String javaVersion = properties.getProperty("java.version");
      String javaVendor = properties.getProperty("java.vendor");
      String javaHome = properties.getProperty("java.home");
      return String.format("%s Java Versiyonu: %s\n%s Java Sağlayıcısı: %s\n%s Java Kurulum Dizini: %s", "✅", javaVersion, "✅", javaVendor, "✅", javaHome);
   }

   public String getThreadInfo() {
      ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
      int threadCount = threadMXBean.getThreadCount();
      int peakThreadCount = threadMXBean.getPeakThreadCount();
      long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
      return String.format("%s Aktif Thread Sayısı: %d\n%s Zirve Thread Sayısı: %d\n%s Toplam Başlatılan Thread Sayısı: %d", "✅", threadCount, "✅", peakThreadCount, "✅", totalStartedThreadCount);
   }

   public String getJvmArguments() {
      List<String> jvmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
      StringBuilder arguments = new StringBuilder();
      arguments.append("✅").append(" JVM Argümanları:\n");
      Iterator var3 = jvmArguments.iterator();

      while(var3.hasNext()) {
         String arg = (String)var3.next();
         arguments.append("  - ").append(arg).append("\n");
      }

      return arguments.toString();
   }

   public String getMinecraftInfo() {
      int onlinePlayers = Bukkit.getOnlinePlayers().size();
      int maxPlayers = Bukkit.getMaxPlayers();
      String version = Bukkit.getBukkitVersion();
      String serverName = Bukkit.getServer().getName();
      return String.format("%s Sunucu İsmi: %s\n%s Sürüm: %s\n%s Çevrimiçi Oyuncu Sayısı: %d/%d", "✅", serverName, "✅", version, "✅", onlinePlayers, maxPlayers);
   }

   public String getUptime() {
      long uptimeInMillis = ManagementFactory.getRuntimeMXBean().getUptime();
      long days = TimeUnit.MILLISECONDS.toDays(uptimeInMillis);
      long hours = TimeUnit.MILLISECONDS.toHours(uptimeInMillis) % 24L;
      long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeInMillis) % 60L;
      long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeInMillis) % 60L;
      return String.format("%s Çalışma Süresi: %d gün, %d saat, %d dakika, %d saniye", "✅", days, hours, minutes, seconds);
   }

   public String getNetworkInfo() {
      try {
         InetAddress ip = InetAddress.getLocalHost();
         return String.format("%s Sunucu IP: %s\n%s Sunucu Hostname: %s", "✅", ip.getHostAddress(), "✅", ip.getHostName());
      } catch (UnknownHostException var2) {
         return "❌ Ağ Bilgisi Alınamadı: " + var2.getMessage();
      }
   }

   public String getSystemInfo() {
      StringBuilder systemInfo = new StringBuilder();
      systemInfo.append("=== [HyperTPS] Sistem Bilgileri ===").append("\n");
      systemInfo.append(this.getOperatingSystemInfo()).append("\n");
      systemInfo.append(this.getCpuInfo()).append("\n");
      systemInfo.append(this.getMemoryInfo()).append("\n");
      systemInfo.append(this.getDiskSpaceInfo()).append("\n");
      systemInfo.append(this.getJavaInfo()).append("\n");
      systemInfo.append(this.getThreadInfo()).append("\n");
      systemInfo.append(this.getJvmArguments()).append("\n");
      systemInfo.append(this.getMinecraftInfo()).append("\n");
      systemInfo.append(this.getUptime()).append("\n");
      systemInfo.append(this.getNetworkInfo()).append("\n");
      systemInfo.append("========================").append("\n");
      System.out.println("HyperTPS - Sistem Detay Bilgileri:\n" + String.valueOf(systemInfo));
      return systemInfo.toString();
   }
}
