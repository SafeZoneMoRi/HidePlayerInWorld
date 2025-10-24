package io.moonlightnovamc.plugin.palyerhideinworld;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Palyerhideinworld extends JavaPlugin implements Listener {

    private List<String> hiddenWorlds;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadHiddenWorlds();

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("hideworldreload").setExecutor((sender, command, label, args) -> {
            reloadConfig();
            loadHiddenWorlds();
            sender.sendMessage("§a[HidePlayersInWorld] Config reloaded!");
            hideForExistingPlayers();
            return true;
        });

        getLogger().info("HidePlayersInWorld enabled!");
        hideForExistingPlayers();
    }

    @Override
    public void onDisable() {
        // แสดงผู้เล่นกลับทั้งหมดตอนปิดปลั๊กอิน
        for (Player p1 : Bukkit.getOnlinePlayers()) {
            for (Player p2 : Bukkit.getOnlinePlayers()) {
                if (!p1.equals(p2)) p1.showPlayer(this, p2);
            }
        }
        getLogger().info("HidePlayersInWorld disabled!");
    }

    private void loadHiddenWorlds() {
        hiddenWorlds = getConfig().getStringList("hidden-worlds");
        getLogger().info("Loaded hidden worlds: " + hiddenWorlds);
    }

    // เมื่อผู้เล่นเข้ามา
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(this, () -> updateVisibility(e.getPlayer()), 5L);
    }

    // เมื่อเปลี่ยนโลก
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Bukkit.getScheduler().runTaskLater(this, () -> updateVisibility(e.getPlayer()), 5L);
    }

    // เมื่อรีสปอว์น
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Bukkit.getScheduler().runTaskLater(this, () -> updateVisibility(e.getPlayer()), 5L);
    }

    // เมื่อออกเกม
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!p.equals(other)) {
                other.showPlayer(this, p);
                p.showPlayer(this, other);
            }
        }
    }

    // ฟังก์ชันหลัก: อัปเดตการมองเห็น
    private void updateVisibility(Player p) {
        World playerWorld = p.getWorld();
        boolean shouldHide = hiddenWorlds.contains(playerWorld.getName());

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (p.equals(other)) continue;
            boolean otherHidden = hiddenWorlds.contains(other.getWorld().getName());

            // ซ่อนเฉพาะเมื่อทั้งคู่ในโลกที่ต้องซ่อน
            if (shouldHide && otherHidden) {
                p.hidePlayer(this, other);
                other.hidePlayer(this, p);
            } else {
                p.showPlayer(this, other);
                other.showPlayer(this, p);
            }
        }
    }

    // ใช้ตอนเปิด plugin เพื่อซ่อนผู้เล่นที่อยู่ในโลกที่กำหนดอยู่แล้ว
    private void hideForExistingPlayers() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateVisibility(p);
            }
        }, 10L);
    }
}
