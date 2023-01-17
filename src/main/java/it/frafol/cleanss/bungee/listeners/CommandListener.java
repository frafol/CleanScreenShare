package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.objects.PlayerCache;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class CommandListener implements Listener {

    @EventHandler
    public void onPlayerCommand(@NotNull ChatEvent event) {

        if (!event.getMessage().startsWith("/")) {
            return;
        }

        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }

    }
}
