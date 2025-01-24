package org.pluginfile.hyperTPS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NetworkInfo {
   public static void execute(Player player) {
      Inventory loadingGui = Bukkit.createInventory((InventoryHolder)null, 9, String.valueOf(ChatColor.GOLD) + "Ağ Bilgileri Yükleniyor...");
      loadingGui.setItem(4, createSimpleItem(Material.CLOCK, String.valueOf(ChatColor.YELLOW) + "Bilgiler Alınıyor", String.valueOf(ChatColor.GRAY) + "Lütfen bekleyin."));
      player.openInventory(loadingGui);
      Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("HyperTPS"), () -> {
         List<String> networkDetails = collectNetworkInterfaceDetails();
         List<String> trafficInfo = collectDetailedNetworkTraffic();
         Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("HyperTPS"), () -> {
            showNetworkInfoGui(player, networkDetails, trafficInfo);
         });
      });
   }

   private static void showNetworkInfoGui(Player player, List<String> networkDetails, List<String> trafficInfo) {
      int inventorySize = Math.min(((networkDetails.size() + trafficInfo.size()) / 9 + 1) * 9, 54);
      inventorySize = Math.max(inventorySize, 9);
      Inventory gui = Bukkit.createInventory((InventoryHolder)null, inventorySize, String.valueOf(ChatColor.GOLD) + "Ağ ve Trafik Bilgisi");
      int slot = 0;
      Iterator var6 = networkDetails.iterator();

      String trafficDetail;
      while(var6.hasNext()) {
         trafficDetail = (String)var6.next();
         if (slot >= inventorySize) {
            Bukkit.getLogger().warning("Ağ arayüz bilgisi eklenirken slot sınırı aşıldı.");
            break;
         }

         gui.setItem(slot++, createSimpleItem(Material.PAPER, String.valueOf(ChatColor.GREEN) + "Arayüz Bilgisi", trafficDetail));
      }

      if (slot < inventorySize) {
         gui.setItem(slot++, createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(ChatColor.DARK_GRAY) + "--- Trafik Bilgisi ---", ""));
      }

      var6 = trafficInfo.iterator();

      while(var6.hasNext()) {
         trafficDetail = (String)var6.next();
         if (slot >= inventorySize) {
            Bukkit.getLogger().warning("Trafik bilgisi eklenirken slot sınırı aşıldı.");
            break;
         }

         gui.setItem(slot++, createSimpleItem(Material.COMPASS, String.valueOf(ChatColor.YELLOW) + "Trafik Bilgisi", trafficDetail));
      }

      player.openInventory(gui);
   }

   private static ItemStack createSimpleItem(Material material, String title, String description) {
      ItemStack item = new ItemStack(material, 1);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(title);
         List<String> lore = new ArrayList();
         String[] var6 = description.split("\n");
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String line = var6[var8];
            String var10001 = String.valueOf(ChatColor.GRAY);
            lore.add(var10001 + line);
         }

         meta.setLore(lore);
         item.setItemMeta(meta);
      }

      return item;
   }

   private static List<String> collectNetworkInterfaceDetails() {
      ArrayList details = new ArrayList();

      try {
         Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();

         while(true) {
            NetworkInterface network;
            do {
               if (!networkInterfaces.hasMoreElements()) {
                  return details;
               }

               network = (NetworkInterface)networkInterfaces.nextElement();
            } while(!network.isUp());

            StringBuilder detail = new StringBuilder();
            detail.append(ChatColor.YELLOW).append("Arayüz Adı: ").append(ChatColor.AQUA).append(network.getName()).append("\n");
            detail.append(ChatColor.YELLOW).append("MAC Adresi: ").append(ChatColor.AQUA).append(formatMacAddress(network.getHardwareAddress())).append("\n");
            detail.append(ChatColor.YELLOW).append("Maksimum MTU: ").append(ChatColor.AQUA).append(network.getMTU()).append("\n");
            List<String> ipv4Addresses = new ArrayList();
            List<String> ipv6Addresses = new ArrayList();
            Enumeration addresses = network.getInetAddresses();

            while(addresses.hasMoreElements()) {
               InetAddress address = (InetAddress)addresses.nextElement();
               if (address.getHostAddress().contains(":")) {
                  ipv6Addresses.add(address.getHostAddress());
               } else {
                  ipv4Addresses.add(address.getHostAddress());
               }
            }

            String ip;
            Iterator var10;
            if (!ipv4Addresses.isEmpty()) {
               detail.append(ChatColor.YELLOW).append("IPv4 Adresleri:").append("\n");
               var10 = ipv4Addresses.iterator();

               while(var10.hasNext()) {
                  ip = (String)var10.next();
                  detail.append(ChatColor.AQUA).append("  - ").append(ip).append("\n");
               }
            }

            if (!ipv6Addresses.isEmpty()) {
               detail.append(ChatColor.YELLOW).append("IPv6 Adresleri:").append("\n");
               var10 = ipv6Addresses.iterator();

               while(var10.hasNext()) {
                  ip = (String)var10.next();
                  detail.append(ChatColor.AQUA).append("  - ").append(ip).append("\n");
               }
            }

            details.add(detail.toString());
         }
      } catch (Exception var9) {
         String var10001 = String.valueOf(ChatColor.RED);
         details.add(var10001 + "Ağ bilgileri alınırken hata oluştu: " + var9.getMessage());
         return details;
      }
   }

   private static List<String> collectDetailedNetworkTraffic() {
      ArrayList trafficDetails = new ArrayList();

      try {
         Process process = Runtime.getRuntime().exec("netstat -e");
         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
         long receivedBytes = 0L;
         long sentBytes = 0L;
         int tcpConnectionCount = 0;

         String line;
         while((line = reader.readLine()) != null) {
            if (line.contains("Bytes")) {
               String[] data = line.trim().split("\\s+");
               if (data.length >= 3) {
                  receivedBytes = Long.parseLong(data[1]);
                  sentBytes = Long.parseLong(data[2]);
               }
            }
         }

         process = Runtime.getRuntime().exec("netstat -an");
         reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

         while((line = reader.readLine()) != null) {
            if (line.trim().startsWith("TCP")) {
               ++tcpConnectionCount;
            }
         }

         double receivedGb = bytesToGb(receivedBytes);
         double sentGb = bytesToGb(sentBytes);
         double totalGb = receivedGb + sentGb;
         StringBuilder trafficItemDescription = new StringBuilder();
         trafficItemDescription.append("Toplam Veri: ").append(String.format("%.2f", totalGb)).append(" GB\n").append("Alınan: ").append(String.format("%.2f", receivedGb)).append(" GB\n").append("Gönderilen: ").append(String.format("%.2f", sentGb)).append(" GB\n").append("Aktif TCP Bağlantı: ").append(tcpConnectionCount);
         trafficDetails.add(trafficItemDescription.toString());
      } catch (Exception var16) {
         String var10001 = String.valueOf(ChatColor.RED);
         trafficDetails.add(var10001 + "Trafik bilgisi alınamadı: " + var16.getMessage());
      }

      return trafficDetails;
   }

   private static double bytesToGb(long bytes) {
      return (double)bytes / 1.073741824E9D;
   }

   private static String formatMacAddress(byte[] mac) {
      if (mac == null) {
         return "Bilinmiyor";
      } else {
         StringBuilder macAddress = new StringBuilder();
         byte[] var2 = mac;
         int var3 = mac.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            macAddress.append(String.format("%02X:", b));
         }

         return macAddress.substring(0, macAddress.length() - 1);
      }
   }
}
