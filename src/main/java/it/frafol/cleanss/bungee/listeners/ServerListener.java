package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ServerListener implements Listener {

    @EventHandler
    public void onJoin(@NotNull ServerConnectedEvent event) {

        final ProxiedPlayer player = event.getPlayer();

        CleanSS.getInstance().getProxy().getScheduler().schedule(CleanSS.getInstance(), () -> {

            if (!player.getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class))) {
                return;
            }

            if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {

                Utils.sendChannelMessage(player, "SUSPECT");

            }

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {

                Utils.sendChannelMessage(player, "ADMIN");

            }

        }, 1L, TimeUnit.SECONDS);
    }
}
