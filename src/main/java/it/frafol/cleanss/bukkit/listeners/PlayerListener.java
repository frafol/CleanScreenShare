package it.frafol.cleanss.bukkit.listeners;

import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if (!SpigotConfig.CHAT_ENABLE.get(Boolean.class)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
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

    @EventHandler
    public void onPlayerDamage(@NotNull EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) {
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

    @EventHandler
    public void onHunger(@NotNull FoodLevelChangeEvent event) {

        if (!(event.getEntity() instanceof Player)) {
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

        if (SpigotConfig.SPAWN.get(Boolean.class)) {
            player.teleport(player.getWorld().getSpawnLocation());
        }

        if (SpigotConfig.GAMEMODE.get(Boolean.class)) {
            player.setGameMode(GameMode.ADVENTURE);
        }
    }
}
