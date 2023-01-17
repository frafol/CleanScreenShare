package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class KickListener implements Listener {

    public CleanSS instance;

    public KickListener(CleanSS instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerChange(@NotNull ServerSwitchEvent event) {

        if (event.getFrom() == null) {
            return;
        }

        if (!(event.getPlayer().getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class))) &&
                PlayerCache.getSuspicious().contains(event.getPlayer().getUniqueId())) {

            event.getPlayer().connect(event.getFrom());

        }

        if (event.getFrom().getName().equals(BungeeConfig.CONTROL.get(String.class)) &&
                PlayerCache.getCouples().get(event.getPlayer()) != null) {

            event.getPlayer().connect(event.getFrom());

        }
    }

    @EventHandler
    public void onPlayerDisconnect(@NotNull PlayerDisconnectEvent event) {

        final ServerInfo proxyServer = instance.getProxy().getServers().get(BungeeConfig.CONTROL_FALLBACK.get(String.class));

        for (Map.Entry<ProxiedPlayer, ProxiedPlayer> entry : PlayerCache.getCouples().entrySet()) {

            if (event.getPlayer() == entry.getKey()) {

                PlayerCache.getSuspicious().remove(entry.getValue().getUniqueId());

                entry.getValue().connect(proxyServer);

                entry.getValue().sendMessage(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color().replace("%prefix%", BungeeMessages.PREFIX.color())));

                return;
            }

            if (event.getPlayer() == entry.getValue()) {
                PlayerCache.getSuspicious().remove(entry.getValue().getUniqueId());

                entry.getKey().connect(proxyServer);

                entry.getKey().sendMessage(TextComponent.fromLegacyText(BungeeMessages.LEAVESUS.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                        .replace("%player%", entry.getValue().getName())));

            }
        }
    }
}
