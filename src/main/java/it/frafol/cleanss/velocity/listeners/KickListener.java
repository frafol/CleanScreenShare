package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;

import java.util.Optional;

public class KickListener {

    public CleanSS instance;

    public KickListener(CleanSS instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPlayerConnect(ServerPreConnectEvent event) {

        final Player player = event.getPlayer();
        final RegisteredServer server = event.getOriginalServer();

        if (!server.getServerInfo().getName().equals(VelocityConfig.CONTROL.get(String.class))
                && (PlayerCache.getSuspicious().contains(player.getUniqueId()) || PlayerCache.getAdministrator().contains(player.getUniqueId()))) {

            event.setResult(ServerPreConnectEvent.ServerResult.denied());

        }
    }

    @Subscribe
    public void onPlayerConnected(PostLoginEvent event) {

        final Player player = event.getPlayer();

        if (player.hasPermission(VelocityConfig.RELOAD_PERMISSION.get(String.class))) {
            instance.UpdateChecker(player);
        }

        instance.UpdateJDA();

        if (instance.getData() != null) {
            instance.getData().setupPlayer(player.getUniqueId());
        }

        PlayerCache.getControls().putIfAbsent(player.getUniqueId(), 0);
        PlayerCache.getControls_suffered().putIfAbsent(player.getUniqueId(), 0);

    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {

        final Optional<RegisteredServer> proxyServer = instance.getServer().getServer(VelocityConfig.CONTROL_FALLBACK.get(String.class));
        final Player player = event.getPlayer();

        instance.UpdateJDA();

        if (!proxyServer.isPresent()) {
            return;
        }

        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, proxyServer.get());

        } else if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            Utils.punishPlayer(instance.getKey(PlayerCache.getCouples(), player).getUniqueId(), player.getUsername(), instance.getKey(PlayerCache.getCouples(), player), player);
            Utils.finishControl(player, instance.getKey(PlayerCache.getCouples(), player), proxyServer.get());

        }
    }
}
