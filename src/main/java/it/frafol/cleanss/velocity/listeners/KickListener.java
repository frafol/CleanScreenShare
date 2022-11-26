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
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class KickListener {

    public CleanSS instance;

    public KickListener(CleanSS instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPlayerChange(@NotNull ServerPreConnectEvent event) {

        assert event.getPreviousServer() != null;
        if (event.getPreviousServer().getServerInfo().getName().equals(VelocityConfig.CONTROL.get(String.class)) &&
                PlayerCache.getSuspicious().contains(event.getPlayer().getUniqueId())) {

            event.setResult(ServerPreConnectEvent.ServerResult.denied());

        }

        if (event.getPreviousServer().getServerInfo().getName().equals(VelocityConfig.CONTROL.get(String.class)) &&
                PlayerCache.getCouples().get(event.getPlayer()) != null) {

            event.setResult(ServerPreConnectEvent.ServerResult.denied());

        }
    }

    @Subscribe
    public void onPlayerDisconnect(@NotNull DisconnectEvent event) {

        final Optional<RegisteredServer> proxyServer = instance.getServer().getServer(VelocityConfig.CONTROL_FALLBACK.get(String.class));

        for (Map.Entry<Player, Player> entry : PlayerCache.getCouples().entrySet()) {

            if (event.getPlayer() == entry.getKey()) {
                PlayerCache.getSuspicious().remove(entry.getValue().getUniqueId());

                assert proxyServer.isPresent();
                entry.getValue().createConnectionRequest(proxyServer.get()).fireAndForget();

                entry.getValue().sendMessage(Component.text(VelocityMessages.FINISHSUS.color().replace("%prefix%", VelocityMessages.PREFIX.color())));

                return;
            }

            if (event.getPlayer() == entry.getValue()) {
                PlayerCache.getSuspicious().remove(entry.getValue().getUniqueId());

                assert proxyServer.isPresent();
                entry.getKey().createConnectionRequest(proxyServer.get()).fireAndForget();

                if (VelocityConfig.DISCONNECT_COMMAND_TOGGLE.get(Boolean.class)) {
                    instance.getServer().getCommandManager().executeImmediatelyAsync(
                            instance.getServer().getConsoleCommandSource(),
                            VelocityConfig.DISCONNECT_COMMAND.get(String.class)
                                    .replace("%player%", entry.getValue().getUsername()));
                }

                entry.getKey().sendMessage(Component.text(VelocityMessages.LEAVESUS.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", entry.getValue().getUsername())));

            }
        }
    }
}
