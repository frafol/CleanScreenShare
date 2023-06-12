package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class KickListener {

    public CleanSS instance;

    public KickListener(CleanSS instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPlayerConnect(@NotNull ServerPreConnectEvent event) {

        final Player player = event.getPlayer();
        final RegisteredServer server = event.getOriginalServer();

        if (server == null) {

            if (player.hasPermission(VelocityConfig.RELOAD_PERMISSION.get(String.class))) {
                instance.UpdateChecker(player);
            }

            instance.UpdateJDA();
            instance.getData().setupPlayer(player.getUniqueId());

            PlayerCache.getControls().putIfAbsent(player.getUniqueId(), 0);
            PlayerCache.getControls_suffered().putIfAbsent(player.getUniqueId(), 0);

            return;
        }

        if (!server.getServerInfo().getName().equals(VelocityConfig.CONTROL.get(String.class))
                && (PlayerCache.getSuspicious().contains(player.getUniqueId()) || PlayerCache.getAdministrator().contains(player.getUniqueId()))) {

            event.setResult(ServerPreConnectEvent.ServerResult.denied());

        }
    }

    @Subscribe
    public void onPlayerDisconnect(@NotNull DisconnectEvent event) {

        final Optional<RegisteredServer> proxyServer = instance.getServer().getServer(VelocityConfig.CONTROL_FALLBACK.get(String.class));
        final Player player = event.getPlayer();

        instance.UpdateJDA();

        if (!proxyServer.isPresent()) {
            return;
        }

        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {

            Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, proxyServer.get());

        } else if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {

            Utils.finishControl(player, instance.getKey(PlayerCache.getCouples(), player), proxyServer.get());
            Utils.sendDiscordMessage(player, instance.getKey(PlayerCache.getCouples(), player), VelocityMessages.DISCORD_QUIT.get(String.class));

        }

        Utils.punishPlayer(player.getUniqueId(), player.getRemoteAddress().getAddress().getHostAddress());
    }
}
