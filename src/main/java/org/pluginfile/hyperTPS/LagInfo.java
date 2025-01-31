package org.pluginfile.hyperTPS;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Arrays;

public class LagInfo implements Listener {

    private final HyperTPS plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private long lastTickTime = System.currentTimeMillis();
    private double tps = 20.0;
    private double mspt = 0.0;

    public LagInfo(HyperTPS plugin) {
        this.plugin = plugin;
        startMeasuring();
    }

    private void startMeasuring() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long elapsed = now - lastTickTime;
                lastTickTime = now;

                double instantTps = 1000.0 / Math.max(1, elapsed);
                tps = (tps * 0.9) + (instantTps * 0.1);
                mspt = elapsed;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void openLagInfoGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "Sunucu Performansı");

        setupLagInfo(gui);

        gui.setItem(22, createInfoItem(Material.EMERALD, ChatColor.GOLD + "Yenile", ChatColor.GRAY + "Bilgileri anında yenilemek için tıklayın."));

        player.openInventory(gui);
    }

    private void setupLagInfo(Inventory gui) {
        fillWithPlaceholders(gui);
        updateLagInfo(gui);
    }

    private void fillWithPlaceholders(Inventory gui) {
        ItemStack filler = createInfoItem(Material.GRAY_STAINED_GLASS_PANE, " ", "");
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null || gui.getItem(i).getType() == Material.AIR) {
                gui.setItem(i, filler);
            }
        }
    }

    private void updateLagInfo(Inventory gui) {
        gui.setItem(10, createInfoItem(Material.CLOCK, ChatColor.AQUA + "TPS Bilgisi", getDetailedTPSInfo()));
        gui.setItem(12, createInfoItem(Material.PAPER, ChatColor.GREEN + "MSPT Bilgisi", getDetailedMSPTInfo()));
        gui.setItem(14, createInfoItem(Material.REDSTONE, ChatColor.RED + "Genel Performans", getGeneralPerformanceInfo()));
        gui.setItem(16, createInfoItem(Material.HOPPER, ChatColor.YELLOW + "Chunk Yükleme", getChunkLoadInfo()));
    }

    private ItemStack createInfoItem(Material material, String name, String... description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(description));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getDetailedTPSInfo() {
        ChatColor color = getTPSColor(tps);
        return color + "Anlık TPS: " + df.format(tps) + "\n" +
                ChatColor.GRAY + "20.0 TPS ideal bir hızdır.";
    }

    private String getDetailedMSPTInfo() {
        ChatColor color = mspt <= 50 ? ChatColor.GREEN : (mspt <= 75 ? ChatColor.YELLOW : ChatColor.RED);
        return color + "Ortalama MSPT: " + df.format(mspt) + " ms\n" +
                ChatColor.GRAY + "Tik Başına Milisaniye:\n" +
                ChatColor.GRAY + "< 50ms: Mükemmel\n" +
                ChatColor.GRAY + "50-75ms: İyi\n" +
                ChatColor.GRAY + "> 75ms: Lag Olabilir";
    }

    private String getGeneralPerformanceInfo() {
        if (tps >= 19.5) {
            return ChatColor.GREEN + "Durum: Mükemmel\n" + ChatColor.GRAY + "Sunucu optimal performansta çalışıyor.";
        } else if (tps >= 18) {
            return ChatColor.GREEN + "Durum: Çok İyi\n" + ChatColor.GRAY + "Sunucu stabil çalışıyor.";
        } else if (tps >= 15) {
            return ChatColor.YELLOW + "Durum: Orta\n" + ChatColor.GRAY + "Hafif gecikmeler olabilir.";
        } else {
            return ChatColor.RED + "Durum: Kötü\n" + ChatColor.GRAY + "Sunucu ciddi gecikmeler yaşıyor.";
        }
    }

    private String getChunkLoadInfo() {
        int loadedChunks = Bukkit.getWorlds().stream().mapToInt(world -> world.getLoadedChunks().length).sum();
        return ChatColor.YELLOW + "Yüklü Chunk Sayısı: " + loadedChunks + "\n" +
                ChatColor.GRAY + "Yüksek sayılar performansı etkileyebilir.";
    }

    private ChatColor getTPSColor(double tps) {
        if (tps >= 18.0) return ChatColor.GREEN;
        if (tps >= 15.0) return ChatColor.YELLOW;
        return ChatColor.RED;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_AQUA + "Sunucu Performansı")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.EMERALD) {
                Player player = (Player) event.getWhoClicked();
                player.sendMessage(ChatColor.GRAY + "Performans bilgileri güncelleniyor...");
                Bukkit.getScheduler().runTask(plugin, () -> openLagInfoGUI(player));
            }
        }
    }
}