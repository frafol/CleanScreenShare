package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
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
    public void onPlayerJoin(@NotNull PostLoginEvent event) {

        final ProxiedPlayer player = event.getPlayer();

        if (player.hasPermission(BungeeConfig.RELOAD_PERMISSION.get(String.class))) {
                instance.UpdateChecker(player);
        }

        if (instance.getData() != null) {
            instance.getData().setupPlayer(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerChange(@NotNull ServerConnectEvent event) {

        final ProxiedPlayer player = event.getPlayer();
        final ServerInfo server = event.getTarget();

        if (PlayerCache.getAdministrator().contains(player.getUniqueId()) ||
                PlayerCache.getSuspicious().contains(player.getUniqueId())) {

            if (server.getName().equals(BungeeConfig.CONTROL.get(String.class))) {
                return;
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(@NotNull PlayerDisconnectEvent event) {

        final ServerInfo proxyServer = instance.getProxy().getServersCopy().get(BungeeConfig.CONTROL_FALLBACK.get(String.class));
        final ProxiedPlayer player = event.getPlayer();

        if (proxyServer == null) {
            instance.getLogger().severe("Fallback server was not found in your BungeeCord configuration or is offline, players will not be able to reconnect to the server.");

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                instance.getValue(PlayerCache.getCouples(), player).disconnect(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color()));
            }

            if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
                instance.getKey(PlayerCache.getCouples(), player).disconnect(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color()));
            }
        }

        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, proxyServer);
        }

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            Utils.punishPlayer(instance.getKey(PlayerCache.getCouples(), player).getUniqueId(), player.getName(), instance.getKey(PlayerCache.getCouples(), player), player);
            Utils.finishControl(player, instance.getKey(PlayerCache.getCouples(), player), proxyServer);
        }
    }
}
