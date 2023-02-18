package it.frafol.cleanss.bukkit.commands;

import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;

public class MainCommand implements Listener {

    @EventHandler
    public void onCommand(@NotNull PlayerCommandPreprocessEvent event) throws IOException {

        final Player player = event.getPlayer();
        final String message = event.getMessage();
        final YamlFile cache = CleanSS.getInstance().getCacheTextFile().getConfig();

        if (!player.hasPermission(SpigotConfig.ADMIN_PERMISSION.get(String.class))) {
            return;
        }

        if (message.equalsIgnoreCase("/setadminspawn")) {

            event.setCancelled(true);

            player.sendMessage(SpigotConfig.SPAWN_SET.color());
            cache.set("spawns.admin", PlayerCache.LocationToString(player.getLocation()));
            cache.save();

        }

        if (message.equalsIgnoreCase("/setsuspectspawn")) {

            event.setCancelled(true);

            player.sendMessage(SpigotConfig.SPAWN_SET.color());
            cache.set("spawns.suspect", PlayerCache.LocationToString(player.getLocation()));
            cache.save();

        }
    }
}
