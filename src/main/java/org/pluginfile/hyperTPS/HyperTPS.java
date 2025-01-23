package org.pluginfile.hyperTPS;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Entity;
import java.net.InetSocketAddress;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class HyperTPS extends JavaPlugin implements Listener {

    private final SystemInfo systemInfo = new SystemInfo();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("🔵 Gelistirici: HyperAlpha71");
        getLogger().info("✅ Plugin Aktive Edildi.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("sunucu")) {
            if (sender instanceof Player player) {
                openAsyncServerInfo(player);
            } else {
                sender.sendMessage(ChatColor.RED + "Bu komut yalnızca oyuncular tarafından kullanılabilir!");
            }
            return true;
        }


        if (command.getName().equalsIgnoreCase("gecikme")) {
            String lagInfo = measureLag();
            sender.sendMessage(ChatColor.AQUA + "[HyperTPS] Gecikme Durumu:");
            sender.sendMessage(lagInfo);
            return true;
        }

        if (command.getName().equalsIgnoreCase("durum")) {
            if (sender instanceof Player) {
                calculateServerStats(sender);
            } else {
                sender.sendMessage(ChatColor.RED + "Bu komut yalnızca oyuncular içindir!");
            }
        }


        if (command.getName().equalsIgnoreCase("oyuncubilgi")) {
            if (args.length == 1) {
                String playerName = args[0];
                String playerData = getPlayerData(playerName);
                sender.sendMessage(ChatColor.DARK_GREEN + "[HyperTPS] Oyuncu Bilgileri:");
                sender.sendMessage(playerData);
            } else {
                sender.sendMessage(ChatColor.RED + "Kullanım: /oyuncubilgi <oyuncu_adi>");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("oyuncuip")) {
            if (!sender.hasPermission("hypertps.oyuncuip")) {
                sender.sendMessage(ChatColor.RED + "Bu komutu kullanma yetkiniz yok!");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Lütfen bir oyuncu kullanıcı adı giriniz! Kullanım: /oyuncuip <oyuncuAdı>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(ChatColor.RED + "Oyuncu bulunamadı veya çevrimdışı.");
                return true;
            }

            // Oyuncu adresi ve IP'sini al
            InetSocketAddress address = target.getAddress();
            if (address == null) {
                sender.sendMessage(ChatColor.RED + "Oyuncunun IP adresi alınamadı.");
                return true;
            }

            String ip = address.getAddress().getHostAddress();
            int port = address.getPort(); // Oyuncunun bağlantı portu

            sendPlayerInfo(sender, target.getName(), ip, port);

            return true;
        }

        sender.sendMessage(ChatColor.RED + "Bilinmeyen komut. Mevcut komutlar: /sunucu, /gecikme, /durum, /oyuncubilgi, /geoip");
        return false;
    }

    private void openAsyncServerInfo(Player player) {
        // Önce oyuncuya yükleniyor ekranını göster.
        Inventory loadingGui = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Sunucu Bilgileri");
        loadingGui.setItem(4, createSimpleItem(Material.CLOCK, ChatColor.YELLOW + "Bilgiler yükleniyor...", "Lütfen bekleyiniz."));
        player.openInventory(loadingGui);

        // Verileri asenkron olarak hesapla
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                // Ağır işlemler bu kısımda gerçekleşiyor
                String osInfo = systemInfo.getOperatingSystemInfo();
                String memoryInfo = systemInfo.getMemoryInfo();
                String cpuInfo = systemInfo.getCpuInfo();
                String diskInfo = systemInfo.getDiskSpaceInfo();
                String javaInfo = systemInfo.getJavaInfo();
                String uptime = systemInfo.getUptime();
                String networkInfo = systemInfo.getNetworkInfo();
                String threadInfo = systemInfo.getThreadInfo();
                String jvmArgs = systemInfo.getJvmArguments();
                String minecraftInfo = systemInfo.getMinecraftInfo();

                // Ana iş parçacığında GUI'yi oluşturup oyuncuya göster
                Bukkit.getScheduler().runTask(this, () -> {
                    Inventory serverInfoGui = Bukkit.createInventory(null, 18, ChatColor.GOLD + "Sunucu Bilgileri");

                    serverInfoGui.setItem(0, createSimpleItem(Material.REDSTONE_TORCH, "İşletim Sistemi", osInfo));
                    serverInfoGui.setItem(1, createSimpleItem(Material.CHEST, "Bellek Kullanımı", memoryInfo));
                    serverInfoGui.setItem(2, createSimpleItem(Material.REDSTONE, "CPU Kullanımı", cpuInfo));
                    serverInfoGui.setItem(3, createSimpleItem(Material.HOPPER, "Disk Alanı", diskInfo));
                    serverInfoGui.setItem(4, createSimpleItem(Material.BOOK, "Java Sürümü", javaInfo));
                    serverInfoGui.setItem(5, createSimpleItem(Material.CLOCK, "Çalışma Süresi", uptime));
                    serverInfoGui.setItem(6, createSimpleItem(Material.ENDER_PEARL, "Ağ Bilgileri", networkInfo));
                    serverInfoGui.setItem(7, createSimpleItem(Material.IRON_BLOCK, "Thread Kullanımı", threadInfo));
                    serverInfoGui.setItem(8, createSimpleItem(Material.WRITABLE_BOOK, "JVM Argümanları", jvmArgs));
                    serverInfoGui.setItem(9, createSimpleItem(Material.GRASS_BLOCK, "Minecraft Durumu", minecraftInfo));

                    player.openInventory(serverInfoGui); // GUI'yi oyuncuya aç
                });
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Sunucu bilgileri alınırken hata oluştu.");
                getLogger().severe("Sunucu bilgilerini toplarken hata oluştu: " + e.getMessage());
            }
        });
    }

    private ItemStack createSimpleItem(Material material, String title, String description) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + title);
            List<String> lore = new ArrayList<>();
            for (String line : description.split("\n")) {
                lore.add(ChatColor.GRAY + line);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String measureLag() {
        double tps = 20.0; // Placeholder for now.
        double mspt = 1000.0 / tps;
        return ChatColor.GREEN + String.format("TPS: %.2f", tps) +
                ChatColor.YELLOW + ", MSPT: " + ChatColor.AQUA + String.format("%.2f ms", mspt);
    }

    private void calculateServerStats(CommandSender sender) {
        int totalChunks = 0; // Sunucudaki toplam chunk sayısı
        int totalBlocks = 0; // Toplam yüklenen block sayısı
        int totalMobs = 0; // Tüm dünyalardaki toplam mob sayısı
        int totalPlayers = Bukkit.getOnlinePlayers().size(); // Toplam aktif oyuncu sayısı
        long totalChunkLoadTime = 0; // Chunk'ların yüklenme süresi toplamı (nanoTime)

        for (World world : Bukkit.getWorlds()) {
            Chunk[] chunks = world.getLoadedChunks(); // Dünyadaki yüklü chunk'lar
            totalChunks += chunks.length;

            for (Chunk chunk : chunks) {
                long startTime = System.nanoTime(); // Chunk yüklenme hızı ölçümü başlangıcı
                if (!chunk.isLoaded()) {
                    chunk.load(true); // Chunk'i yüklet
                }
                long endTime = System.nanoTime(); // Chunk yüklenme hızı ölçümü bitişi
                totalChunkLoadTime += (endTime - startTime);

                totalBlocks += 16 * 16 * world.getMaxHeight(); // Chunk'taki toplam block sayısı
                totalMobs += countAllMobs(chunk); // Tüm mobları say
            }
        }

        // Chunk başına yüklenme süresi (nanoTime -> ms dönüştürülür)
        double averageChunkLoadTimeMs = totalChunks > 0 ? (totalChunkLoadTime / totalChunks) / 1_000_000.0 : 0.0;

        // Kişi başına düşen mob sayısı
        double mobsPerPlayer = totalPlayers > 0 ? (double) totalMobs / totalPlayers : 0.0;

        // Chunk başına düşen mob sayısı
        double mobsPerChunk = totalChunks > 0 ? (double) totalMobs / totalChunks : 0.0;

        // Bilgileri oyuncuya gönder
        sender.sendMessage(ChatColor.GOLD + "[HyperTPS] Sunucu Durumu:");
        sender.sendMessage(ChatColor.GREEN + "Toplam Chunk Sayısı: " + ChatColor.AQUA + totalChunks);
        sender.sendMessage(ChatColor.GREEN + "Toplam Yüklenen Block Sayısı: " + ChatColor.AQUA + totalBlocks);
        sender.sendMessage(ChatColor.GREEN + "Toplam Mob Sayısı: " + ChatColor.AQUA + totalMobs);
        sender.sendMessage(ChatColor.GREEN + "Kişi Başına Düşen Mob Sayısı: " + ChatColor.AQUA + String.format("%.2f", mobsPerPlayer));
        sender.sendMessage(ChatColor.GREEN + "Chunk Başına Düşen Mob Sayısı: " + ChatColor.AQUA + String.format("%.2f", mobsPerChunk));
        sender.sendMessage(ChatColor.GREEN + "Chunk'ların Ortalama Yüklenme Hızı: " + ChatColor.AQUA + String.format("%.2f ms/chunk", averageChunkLoadTimeMs));
    }

    private void sendPlayerInfo(CommandSender sender, String playerName, String ip, int port) {
        // Şu anki tarih ve saat
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // Asenkron iş parçacığında API isteğini gönder
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                // API bağlantısını ayarla
                String apiUrl = "http://ip-api.com/json/" + ip;
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                // Yanıtı kontrol edip işleme al
                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();

                    // API yanıtlarını metin formatında işleme al
                    String jsonResponse = response.toString();
                    String city = extractValue(jsonResponse, "city");
                    String country = extractValue(jsonResponse, "country");
                    String isp = extractValue(jsonResponse, "isp");

                    // Oyuncuya bilgileri gönder
                    sender.sendMessage(ChatColor.GOLD + "===== " + ChatColor.YELLOW + playerName + " Bilgileri =====");
                    sender.sendMessage(ChatColor.GREEN + "Tarih: " + ChatColor.AQUA + currentDate);
                    sender.sendMessage(ChatColor.GREEN + "IP Adresi: " + ChatColor.AQUA + ip);
                    sender.sendMessage(ChatColor.GREEN + "Port: " + ChatColor.AQUA + port);
                    sender.sendMessage(ChatColor.GREEN + "Şehir: " + ChatColor.AQUA + city);
                    sender.sendMessage(ChatColor.GREEN + "Ülke: " + ChatColor.AQUA + country);
                    sender.sendMessage(ChatColor.GREEN + "ISP: " + ChatColor.AQUA + isp);
                    sender.sendMessage(ChatColor.GOLD + "=======================================");
                } else {
                    sender.sendMessage(ChatColor.RED + "API isteği başarısız oldu. Kod: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "API isteği sırasında bir hata oluştu: " + e.getMessage());
            }
        });
    }

    private String extractValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return "Bilinmiyor";
            startIndex += searchKey.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex == -1) return "Bilinmiyor";
            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return "Bilinmiyor";
        }
    }

private int countAllMobs(Chunk chunk) {
        int count = 0;

        for (Entity entity : chunk.getEntities()) {
            // Mob türü kontrolü: Entity türü hem canlı hem de oyuncu dışı olacak
            if (entity instanceof org.bukkit.entity.LivingEntity &&
                    !(entity instanceof org.bukkit.entity.Player)) {
                count++;
            }
        }

        return count;
    }

    private String getPlayerData(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            return ChatColor.GREEN + "Oyuncu: " + player.getName() + "\n"
                    + "UUID: " + player.getUniqueId();
        }
        return ChatColor.RED + "❌ " + playerName + " isimli oyuncu bulunamadı.";
    }

    private String getPlayerIp(String playerName) {
        Player player = getServer().getPlayer(playerName);
        if (player != null && player.getAddress() != null) {
            return player.getAddress().getAddress().getHostAddress();
        }
        return ChatColor.RED + "❌ Oyuncunun IP adresi alınamadı.";
    }

    private String getGeolocationData(String ip) {
        if (!isValidIP(ip)) {
            return ChatColor.RED + "❌ Geçersiz IP adresi: " + ip;
        }

        try {
            String apiUrl = "http://ip-api.com/json/" + ip + "?fields=status,message,country,regionName,city,zip,lat,lon,isp";
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return parseGeoResponse(response.toString());
        } catch (Exception e) {
            return ChatColor.RED + "❌ Lokasyon bilgisi alınamadı: " + e.getMessage();
        }
    }

    private String parseGeoResponse(String response) {
        try {
            StringBuilder result = new StringBuilder();

            // JSON cevabını düz metin olarak işle
            for (String line : response.split(",")) {
                if (line.contains(":")) {
                    String[] keyValue = line.split(":", 2);
                    String key = keyValue[0].replace("\"", "").trim();
                    String value = keyValue[1].replace("\"", "").trim();
                    if (!key.equalsIgnoreCase("status") && !key.equalsIgnoreCase("message")) {
                        result.append(ChatColor.GREEN).append(key).append(": ")
                                .append(ChatColor.YELLOW).append(value).append("\n");
                    }
                }
            }

            return result.toString();
        } catch (Exception e) {
            return ChatColor.RED + "❌ İşlenirken hata oluştu: " + e.getMessage();
        }
    }

    private boolean isValidIP(String ip) {
        String ipRegex = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip != null && ip.matches(ipRegex);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("GeoPanel") || event.getView().getTitle().equals(ChatColor.GOLD + "Sunucu Bilgileri")) {
            event.setCancelled(true);
        }
    }
}