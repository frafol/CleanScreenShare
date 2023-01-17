package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class KickListener implements Listener {

    public CleanSS instance;

    public KickListener(CleanSS instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerChange(@NotNull ServerConnectEvent event) {

        final ProxiedPlayer player = event.getPlayer();

        if (event.getPlayer().getServer() == null) {
            return;
        }

        if (event.getPlayer().getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class)) &&
                PlayerCache.getCouples().containsValue(player)
                || PlayerCache.getCouples().containsKey(player)
                || PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerDisconnect(@NotNull PlayerDisconnectEvent event) {

        final ServerInfo proxyServer = instance.getProxy().getServers().get(BungeeConfig.CONTROL_FALLBACK.get(String.class));
        final ProxiedPlayer player = event.getPlayer();

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {

                PlayerCache.getSuspicious().remove(instance.getValue(PlayerCache.getCouples(), player).getUniqueId());
                PlayerCache.getCouples().get(player).connect(proxyServer);

                PlayerCache.getCouples().get(player).sendMessage(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())));

                return;

            }

            if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {

                PlayerCache.getSuspicious().remove(player.getUniqueId());

                instance.getKey(PlayerCache.getCouples(), player).connect(proxyServer);

                instance.getKey(PlayerCache.getCouples(), player).sendMessage(TextComponent.fromLegacyText(BungeeMessages.LEAVESUS.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                        .replace("%player%", player.getName())));

        }
    }
}
