package it.frafol.cleanss.bukkit.listeners;

import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if (SpigotConfig.PREVENT_CHAT.get(Boolean.class)) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(@NotNull PlayerCommandPreprocessEvent event) {

        final Player player = event.getPlayer();

        if (player.hasPermission(SpigotConfig.STAFF_PERMISSION.get(String.class))) {
            return;
        }

        if (SpigotConfig.BLOCK_COMMANDS.get(Boolean.class)) {
            if (event.getMessage().startsWith("/")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPvP(EntityDamageByEntityEvent event) {

        if (SpigotConfig.PVP.get(Boolean.class)) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(@NotNull EntityDamageEvent event) {

        final Entity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();

        if (SpigotConfig.INVINCIBLE.get(Boolean.class)) {
            event.setCancelled(true);
        }

        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID) && SpigotConfig.VOID.get(Boolean.class)) {
            event.setCancelled(true);
            player.teleport(player.getWorld().getSpawnLocation());
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHunger(@NotNull FoodLevelChangeEvent event) {

        final HumanEntity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();

        if (SpigotConfig.HUNGER.get(Boolean.class)) {
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {

        final Player player = event.getPlayer();

        if (SpigotConfig.GAMEMODE.get(Boolean.class)) {
            player.setGameMode(GameMode.ADVENTURE);
        }

        if (SpigotConfig.HUNGER.get(Boolean.class)) {
            player.setFoodLevel(20);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {

        final Player player = event.getPlayer();

        if (player.hasPermission(SpigotConfig.STAFF_PERMISSION.get(String.class))) {
            return;
        }

        if (SpigotConfig.MOVE.get(Boolean.class)) {
            event.setCancelled(true);
        }

    }
}
