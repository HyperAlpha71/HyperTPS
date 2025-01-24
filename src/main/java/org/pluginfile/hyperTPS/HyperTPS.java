package org.pluginfile.hyperTPS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class HyperTPS extends JavaPlugin implements Listener {
   private final SystemInfo systemInfo = new SystemInfo();

   public void onEnable() {
      Bukkit.getPluginManager().registerEvents(this, this);
      this.getLogger().info("\ud83d\udd35 Gelistirici: HyperAlpha71");
      this.getLogger().info("✅ Plugin Aktif Edildi.");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      Player target;
      if (command.getName().equalsIgnoreCase("sunucu")) {
         if (sender instanceof Player) {
            target = (Player)sender;
            this.openAsyncServerInfo(target);
         } else {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "Bu komut yalnızca oyuncular tarafından kullanılabilir!");
         }

         return true;
      } else if (command.getName().equalsIgnoreCase("agbilgisi") && sender instanceof Player) {
         target = (Player)sender;
         NetworkInfo.execute(target);
         return true;
      } else {
         String playerName;
         if (command.getName().equalsIgnoreCase("gecikme")) {
            playerName = this.measureLag();
            sender.sendMessage(String.valueOf(ChatColor.AQUA) + "[HyperTPS] Gecikme Durumu:");
            sender.sendMessage(playerName);
            return true;
         } else if (command.getName().equalsIgnoreCase("durum")) {
            if (sender instanceof Player) {
               ServerStatus.execute(sender);
            } else {
               sender.sendMessage(String.valueOf(ChatColor.RED) + "Bu komut yalnızca oyuncular içindir!");
            }

            return true;
         } else if (command.getName().equalsIgnoreCase("oyuncubilgi")) {
            if (args.length == 1) {
               playerName = args[0];
               String playerData = this.getPlayerData(playerName);
               sender.sendMessage(String.valueOf(ChatColor.DARK_GREEN) + "[HyperTPS] Oyuncu Bilgileri:");
               sender.sendMessage(playerData);
            } else {
               sender.sendMessage(String.valueOf(ChatColor.RED) + "Kullanım: /oyuncubilgi <oyuncu_adi>");
            }

            return true;
         } else if (command.getName().equalsIgnoreCase("oyuncuip")) {
            if (!sender.hasPermission("hypertps.oyuncuip")) {
               sender.sendMessage(String.valueOf(ChatColor.RED) + "Bu komutu kullanma yetkiniz yok!");
               return true;
            } else if (args.length == 0) {
               sender.sendMessage(String.valueOf(ChatColor.RED) + "Lütfen bir oyuncu kullanıcı adı giriniz! Kullanım: /oyuncuip <oyuncuAdı>");
               return true;
            } else {
               target = Bukkit.getPlayer(args[0]);
               if (target != null && target.isOnline()) {
                  InetSocketAddress address = target.getAddress();
                  if (address == null) {
                     sender.sendMessage(String.valueOf(ChatColor.RED) + "Oyuncunun IP adresi alınamadı.");
                     return true;
                  } else {
                     String ip = address.getAddress().getHostAddress();
                     int port = address.getPort();
                     this.sendPlayerInfo(sender, target.getName(), ip, port);
                     return true;
                  }
               } else {
                  sender.sendMessage(String.valueOf(ChatColor.RED) + "Oyuncu bulunamadı veya çevrimdışı.");
                  return true;
               }
            }
         } else {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "Bilinmeyen komut. Mevcut komutlar: /sunucu, /gecikme, /durum, /oyuncubilgi, /geoip");
            return false;
         }
      }
   }

   private void openAsyncServerInfo(Player player) {
      Inventory loadingGui = Bukkit.createInventory((InventoryHolder)null, 9, String.valueOf(ChatColor.GOLD) + "Sunucu Bilgileri");
      loadingGui.setItem(4, this.createSimpleItem(Material.CLOCK, String.valueOf(ChatColor.YELLOW) + "Bilgiler yükleniyor...", "Lütfen bekleyiniz."));
      player.openInventory(loadingGui);
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
         try {
            String osInfo = this.systemInfo.getOperatingSystemInfo();
            String memoryInfo = this.systemInfo.getMemoryInfo();
            String cpuInfo = this.systemInfo.getCpuInfo();
            String diskInfo = this.systemInfo.getDiskSpaceInfo();
            String javaInfo = this.systemInfo.getJavaInfo();
            String uptime = this.systemInfo.getUptime();
            String networkInfo = this.systemInfo.getNetworkInfo();
            String threadInfo = this.systemInfo.getThreadInfo();
            String jvmArgs = this.systemInfo.getJvmArguments();
            String minecraftInfo = this.systemInfo.getMinecraftInfo();
            Bukkit.getScheduler().runTask(this, () -> {
               Inventory serverInfoGui = Bukkit.createInventory((InventoryHolder)null, 18, String.valueOf(ChatColor.GOLD) + "Sunucu Bilgileri");
               serverInfoGui.setItem(0, this.createSimpleItem(Material.REDSTONE_TORCH, "İşletim Sistemi", osInfo));
               serverInfoGui.setItem(1, this.createSimpleItem(Material.CHEST, "Bellek Kullanımı", memoryInfo));
               serverInfoGui.setItem(2, this.createSimpleItem(Material.REDSTONE, "CPU Kullanımı", cpuInfo));
               serverInfoGui.setItem(3, this.createSimpleItem(Material.HOPPER, "Disk Alanı", diskInfo));
               serverInfoGui.setItem(4, this.createSimpleItem(Material.BOOK, "Java Sürümü", javaInfo));
               serverInfoGui.setItem(5, this.createSimpleItem(Material.CLOCK, "Çalışma Süresi", uptime));
               serverInfoGui.setItem(6, this.createSimpleItem(Material.ENDER_PEARL, "Ağ Bilgileri", networkInfo));
               serverInfoGui.setItem(7, this.createSimpleItem(Material.IRON_BLOCK, "Thread Kullanımı", threadInfo));
               serverInfoGui.setItem(8, this.createSimpleItem(Material.WRITABLE_BOOK, "JVM Argümanları", jvmArgs));
               serverInfoGui.setItem(9, this.createSimpleItem(Material.GRASS_BLOCK, "Minecraft Durumu", minecraftInfo));
               player.openInventory(serverInfoGui);
            });
         } catch (Exception var12) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Sunucu bilgileri alınırken hata oluştu.");
            this.getLogger().severe("Sunucu bilgilerini toplarken hata oluştu: " + var12.getMessage());
         }

      });
   }

   private ItemStack createSimpleItem(Material material, String title, String description) {
      ItemStack item = new ItemStack(material, 1);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         String var10001 = String.valueOf(ChatColor.YELLOW);
         meta.setDisplayName(var10001 + title);
         List<String> lore = new ArrayList();
         String[] var7 = description.split("\n");
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            String line = var7[var9];
            var10001 = String.valueOf(ChatColor.GRAY);
            lore.add(var10001 + line);
         }

         meta.setLore(lore);
         item.setItemMeta(meta);
      }

      return item;
   }

   private String measureLag() {
      double tps = 20.0D;
      double mspt = 1000.0D / tps;
      String var10000 = String.valueOf(ChatColor.GREEN);
      return var10000 + String.format("TPS: %.2f", tps) + String.valueOf(ChatColor.YELLOW) + ", MSPT: " + String.valueOf(ChatColor.AQUA) + String.format("%.2f ms", mspt);
   }

   private void sendPlayerInfo(CommandSender sender, String playerName, String ip, int port) {
      String currentDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
         String var10001;
         try {
            String apiUrl = "http://ip-api.com/json/" + ip;
            HttpURLConnection connection = (HttpURLConnection)(new URL(apiUrl)).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (connection.getResponseCode() == 200) {
               BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
               StringBuilder response = new StringBuilder();

               String line;
               while((line = reader.readLine()) != null) {
                  response.append(line);
               }

               reader.close();
               String jsonResponse = response.toString();
               String city = this.extractValue(jsonResponse, "city");
               String country = this.extractValue(jsonResponse, "country");
               String isp = this.extractValue(jsonResponse, "isp");
               var10001 = String.valueOf(ChatColor.GOLD);
               sender.sendMessage(var10001 + "===== " + String.valueOf(ChatColor.YELLOW) + playerName + " Bilgileri =====");
               var10001 = String.valueOf(ChatColor.GREEN);
               sender.sendMessage(var10001 + "Tarih: " + String.valueOf(ChatColor.AQUA) + currentDate);
               var10001 = String.valueOf(ChatColor.GREEN);
               sender.sendMessage(var10001 + "IP Adresi: " + String.valueOf(ChatColor.AQUA) + ip);
               var10001 = String.valueOf(ChatColor.GREEN);
               sender.sendMessage(var10001 + "Port: " + String.valueOf(ChatColor.AQUA) + port);
               var10001 = String.valueOf(ChatColor.GREEN);
               sender.sendMessage(var10001 + "Şehir: " + String.valueOf(ChatColor.AQUA) + city);
               var10001 = String.valueOf(ChatColor.GREEN);
               sender.sendMessage(var10001 + "Ülke: " + String.valueOf(ChatColor.AQUA) + country);
               var10001 = String.valueOf(ChatColor.GREEN);
               sender.sendMessage(var10001 + "ISP: " + String.valueOf(ChatColor.AQUA) + isp);
               sender.sendMessage(String.valueOf(ChatColor.GOLD) + "=======================================");
            } else {
               var10001 = String.valueOf(ChatColor.RED);
               sender.sendMessage(var10001 + "API isteği başarısız oldu. Kod: " + connection.getResponseCode());
            }
         } catch (Exception var15) {
            var10001 = String.valueOf(ChatColor.RED);
            sender.sendMessage(var10001 + "API isteği sırasında bir hata oluştu: " + var15.getMessage());
         }

      });
   }

   private String extractValue(String json, String key) {
      try {
         String searchKey = "\"" + key + "\":\"";
         int startIndex = json.indexOf(searchKey);
         if (startIndex == -1) {
            return "Bilinmiyor";
         } else {
            startIndex += searchKey.length();
            int endIndex = json.indexOf("\"", startIndex);
            return endIndex == -1 ? "Bilinmiyor" : json.substring(startIndex, endIndex);
         }
      } catch (Exception var6) {
         return "Bilinmiyor";
      }
   }

   private String getPlayerData(String playerName) {
      Player player = Bukkit.getPlayer(playerName);
      String var10000;
      if (player != null) {
         var10000 = String.valueOf(ChatColor.GREEN);
         return var10000 + "Oyuncu: " + player.getName() + "\nUUID: " + String.valueOf(player.getUniqueId());
      } else {
         var10000 = String.valueOf(ChatColor.RED);
         return var10000 + "❌ " + playerName + " isimli oyuncu bulunamadı.";
      }
   }

   private String parseGeoResponse(String response) {
      try {
         StringBuilder result = new StringBuilder();
         String[] var3 = response.split(",");
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String line = var3[var5];
            if (line.contains(":")) {
               String[] keyValue = line.split(":", 2);
               String key = keyValue[0].replace("\"", "").trim();
               String value = keyValue[1].replace("\"", "").trim();
               if (!key.equalsIgnoreCase("status") && !key.equalsIgnoreCase("message")) {
                  result.append(ChatColor.GREEN).append(key).append(": ").append(ChatColor.YELLOW).append(value).append("\n");
               }
            }
         }

         return result.toString();
      } catch (Exception var10) {
         String var10000 = String.valueOf(ChatColor.RED);
         return var10000 + "❌ İşlenirken hata oluştu: " + var10.getMessage();
      }
   }

   private boolean isValidIP(String ip) {
      String ipRegex = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
      return ip != null && ip.matches(ipRegex);
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getView().getTitle().contains("GeoPanel") || event.getView().getTitle().equals(String.valueOf(ChatColor.GOLD) + "Sunucu Bilgileri")) {
         event.setCancelled(true);
      }

   }
}
