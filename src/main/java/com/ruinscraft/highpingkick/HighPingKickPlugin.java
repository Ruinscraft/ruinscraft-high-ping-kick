package com.ruinscraft.highpingkick;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class HighPingKickPlugin extends JavaPlugin implements Runnable, Listener {

    private static final int HIGH_PING = 250;
    private static final Map<UUID, Long> JOINED_AT = new HashMap<>();
    private static final Set<UUID> MOVED_USERS = new HashSet<>();

    public static int getPing(Player player) throws Exception {
        Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
        return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        // run every 20 ticks or ~2 seconds
        getServer().getScheduler().runTaskTimer(this, this, 40L, 40L);
        getCommand("showping").setExecutor(new ShowPingCommand());
    }

    // check task
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int ping;
            try {
                ping = getPing(player);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }


            int score = 0;

            if (ping > HIGH_PING) {
                score++;
            }

            if (!player.hasPlayedBefore()) {
                score++;
            }

            // if joined more than 15 seconds ago
            if (JOINED_AT.get(player.getUniqueId()) != null) {
                if (JOINED_AT.get(player.getUniqueId()) + TimeUnit.SECONDS.toMillis(15) < System.currentTimeMillis()) {
                    // if hasn't moved yet
                    if (!MOVED_USERS.contains(player.getUniqueId())) {
                        score++;
                    }
                }
            }

            if (score > 1) {
                player.kickPlayer("You have been kicked for an unusually poor connection. Please try reconnecting.");
                getLogger().info(player.getName() + " " + player.getAddress().getHostName() + " has been kicked for a poor connection");
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        MOVED_USERS.add(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        JOINED_AT.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        MOVED_USERS.remove(event.getPlayer().getUniqueId());
        JOINED_AT.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        // cancel chat if first time online and has not moved
        if (!event.getPlayer().hasPlayedBefore()) {
            if (!MOVED_USERS.contains(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPreCommand(PlayerCommandPreprocessEvent event) {
        // cancel command if first time online and has not moved
        if (!event.getPlayer().hasPlayedBefore()) {
            if (!MOVED_USERS.contains(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

}
