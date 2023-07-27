package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

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

        if (server == null) {
            return;
        }

        if (!instance.getServer().getServer(VelocityConfig.CONTROL.get(String.class)).isPresent()) {
            return;
        }

        final RegisteredServer control = instance.getServer().getServer(VelocityConfig.CONTROL.get(String.class)).get();

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())
                || PlayerCache.getAdministrator().contains(player.getUniqueId())) {

            if (server.equals(control) || instance.useLimbo) {
                return;
            }

            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }

    @Subscribe
    public void onPlayerConnected(PostLoginEvent event) {

        final Player player = event.getPlayer();

        if (player.hasPermission(VelocityConfig.RELOAD_PERMISSION.get(String.class))) {
            instance.UpdateChecker(player);
        }

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

        if (!proxyServer.isPresent()) {
            instance.getLogger().error("Fallback server was not found in your Velocity configuration or is offline, players will not be able to reconnect to the server.");

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                instance.getValue(PlayerCache.getCouples(), player).disconnect(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.FINISHSUS.color()));
            }

            if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
                instance.getKey(PlayerCache.getCouples(), player).disconnect(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.FINISHSUS.color()));
            }

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, null);

            } else if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {

                if (instance.useLimbo && PlayerCache.getNow_started_sus().contains(player.getUniqueId())) {
                    Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, null);
                    return;
                }

                Utils.punishPlayer(instance.getKey(PlayerCache.getCouples(), player).getUniqueId(), player.getUsername(), instance.getKey(PlayerCache.getCouples(), player), player);
                Utils.finishControl(player, instance.getKey(PlayerCache.getCouples(), player), null);

            }

            return;
        }

        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, proxyServer.get());

        } else if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {

            if (instance.useLimbo && PlayerCache.getNow_started_sus().contains(player.getUniqueId())) {
                Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, proxyServer.get());
                return;
            }

            Utils.punishPlayer(instance.getKey(PlayerCache.getCouples(), player).getUniqueId(), player.getUsername(), instance.getKey(PlayerCache.getCouples(), player), player);
            Utils.finishControl(player, instance.getKey(PlayerCache.getCouples(), player), proxyServer.get());

        }
    }
}
