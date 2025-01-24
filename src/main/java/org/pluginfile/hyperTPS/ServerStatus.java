package org.pluginfile.hyperTPS;

import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ServerStatus {
   public static void execute(CommandSender sender) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(String.valueOf(ChatColor.RED) + "Bu komut yalnızca oyuncular tarafından kullanılabilir!");
      } else {
         Player player = (Player)sender;
         int totalChunks = 0;
         int totalBlocks = 0;
         int totalMobs = 0;
         int totalPlayers = Bukkit.getOnlinePlayers().size();
         long totalChunkLoadTime = 0L;
         Iterator var8 = Bukkit.getWorlds().iterator();

         while(var8.hasNext()) {
            World world = (World)var8.next();
            Chunk[] chunks = world.getLoadedChunks();
            totalChunks += chunks.length;
            Chunk[] var11 = chunks;
            int var12 = chunks.length;

            for(int var13 = 0; var13 < var12; ++var13) {
               Chunk chunk = var11[var13];
               long startTime = System.nanoTime();
               long endTime = System.nanoTime();
               totalChunkLoadTime += endTime - startTime;
               totalBlocks += 256 * world.getMaxHeight();
               totalMobs += countAllMobs(chunk);
            }
         }

         double averageChunkLoadTimeMs = totalChunks > 0 ? (double)(totalChunkLoadTime / (long)totalChunks) / 1000000.0D : 0.0D;
         double mobsPerPlayer = totalPlayers > 0 ? (double)totalMobs / (double)totalPlayers : 0.0D;
         double mobsPerChunk = totalChunks > 0 ? (double)totalMobs / (double)totalChunks : 0.0D;
         sendInventory(player, totalChunks, totalBlocks, totalMobs, totalPlayers, averageChunkLoadTimeMs, mobsPerPlayer, mobsPerChunk);
      }
   }

   private static void sendInventory(Player player, int totalChunks, int totalBlocks, int totalMobs, int totalPlayers, double averageChunkLoadTime, double mobsPerPlayer, double mobsPerChunk) {
      Inventory statusInventory = Bukkit.createInventory((InventoryHolder)null, 27, String.valueOf(ChatColor.GOLD) + "Sunucu Durumu");
      Material var10002 = Material.GRASS_BLOCK;
      String var10003 = String.valueOf(ChatColor.GREEN) + "Toplam Chunk Sayısı";
      String var10004 = String.valueOf(ChatColor.GRAY) + "Yüklenen toplam chunk sayısı:";
      String var10005 = String.valueOf(ChatColor.AQUA);
      statusInventory.setItem(10, createItem(var10002, var10003, List.of(var10004, var10005 + String.valueOf(totalChunks))));
      var10002 = Material.STONE;
      var10003 = String.valueOf(ChatColor.GREEN) + "Toplam Blok Sayısı";
      var10004 = String.valueOf(ChatColor.GRAY) + "Yüklenen toplam blok sayısı:";
      var10005 = String.valueOf(ChatColor.AQUA);
      statusInventory.setItem(11, createItem(var10002, var10003, List.of(var10004, var10005 + String.valueOf(totalBlocks))));
      var10002 = Material.ZOMBIE_HEAD;
      var10003 = String.valueOf(ChatColor.GREEN) + "Toplam Mob Sayısı";
      var10004 = String.valueOf(ChatColor.GRAY) + "Sunucudaki toplam mob sayısı:";
      var10005 = String.valueOf(ChatColor.AQUA);
      statusInventory.setItem(12, createItem(var10002, var10003, List.of(var10004, var10005 + String.valueOf(totalMobs))));
      var10002 = Material.PLAYER_HEAD;
      var10003 = String.valueOf(ChatColor.GREEN) + "Oyuncu Sayısı";
      var10004 = String.valueOf(ChatColor.GRAY) + "Bağlı oyuncu sayısı:";
      var10005 = String.valueOf(ChatColor.AQUA);
      statusInventory.setItem(14, createItem(var10002, var10003, List.of(var10004, var10005 + String.valueOf(totalPlayers))));
      var10002 = Material.CLOCK;
      var10003 = String.valueOf(ChatColor.GREEN) + "Ortalama Chunk Yükleme Süresi";
      var10004 = String.valueOf(ChatColor.GRAY) + "Chunk yükleme hızları (strateji):";
      var10005 = String.valueOf(ChatColor.AQUA);
      statusInventory.setItem(15, createItem(var10002, var10003, List.of(var10004, var10005 + String.format("%.2f ms", averageChunkLoadTime))));
      var10002 = Material.IRON_SWORD;
      var10003 = String.valueOf(ChatColor.GREEN) + "Kişi Başına Mob Sayısı";
      var10004 = String.valueOf(ChatColor.GRAY) + "Her oyuncuya düşen mob sayısı:";
      var10005 = String.valueOf(ChatColor.AQUA);
      statusInventory.setItem(16, createItem(var10002, var10003, List.of(var10004, var10005 + String.format("%.2f", mobsPerPlayer))));
      var10002 = Material.SPAWNER;
      var10003 = String.valueOf(ChatColor.GREEN) + "Chunk Başına Mob Sayısı";
      var10004 = String.valueOf(ChatColor.GRAY) + "Chunk başına düşen mob sayısı:";
      var10005 = String.valueOf(ChatColor.AQUA);
      statusInventory.setItem(22, createItem(var10002, var10003, List.of(var10004, var10005 + String.format("%.2f", mobsPerChunk))));
      player.openInventory(statusInventory);
   }

   private static int countAllMobs(Chunk chunk) {
      int count = 0;
      Entity[] var2 = chunk.getEntities();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Entity entity = var2[var4];
         if (entity instanceof LivingEntity && !(entity instanceof Player)) {
            ++count;
         }
      }

      return count;
   }

   private static ItemStack createItem(Material material, String name, List<String> lore) {
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name);
         meta.setLore(lore);
         item.setItemMeta(meta);
      }

      return item;
   }
}
