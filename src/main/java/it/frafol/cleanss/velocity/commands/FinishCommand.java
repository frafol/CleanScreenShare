package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FinishCommand implements SimpleCommand {

    public final CleanSS instance;

    public FinishCommand(CleanSS instance) {
        this.instance = instance;
    }

    @Override
    public void execute(SimpleCommand.@NotNull Invocation invocation) {

        final CommandSource source = invocation.source();

        if (!source.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class))) {
            source.sendMessage(Component.text(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (invocation.arguments().length == 0) {

            source.sendMessage(Component.text(VelocityMessages.USAGE.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;

        }

        if (invocation.arguments().length == 1) {

            if (instance.getServer().getAllPlayers().toString().contains(invocation.arguments()[0])) {

                final Optional<Player> player = instance.getServer().getPlayer(invocation.arguments()[0]);
                final Optional<RegisteredServer> proxyServer = instance.getServer().getServer(VelocityConfig.CONTROL_FALLBACK.get(String.class));
                final Player sender = (Player) invocation.source();

                if (!player.isPresent()) {
                    source.sendMessage(Component.text(VelocityMessages.NOT_ONLINE.get(String.class)
                            .replace("%prefix%", VelocityMessages.PREFIX.color())));
                    return;
                }

                if (!PlayerCache.getSuspicious().contains(player.get().getUniqueId())) {
                    source.sendMessage(Component.text(VelocityMessages.NOT_CONTROL.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
                    return;
                }

                PlayerCache.getSuspicious().remove(player.get().getUniqueId());
                PlayerCache.getCouples().remove(sender, player.get());

                if (!player.get().getCurrentServer().isPresent()) {
                    return;
                }

                if (player.get().getCurrentServer().get().getServer().getServerInfo().getName().equals(VelocityConfig.CONTROL.get(String.class))) {

                    if (!proxyServer.isPresent()) {
                        return;
                    }

                    player.get().createConnectionRequest(proxyServer.get()).fireAndForget();

                    player.get().sendMessage(Component.text(VelocityMessages.FINISHSUS.color().replace("%prefix%", VelocityMessages.PREFIX.color())));

                    if (!sender.getCurrentServer().isPresent()) {
                        return;
                    }

                    if (sender.getCurrentServer().get().getServer().getServerInfo().getName().equals(VelocityConfig.CONTROL.get(String.class))) {
                        sender.createConnectionRequest(proxyServer.get()).fireAndForget();
                    }
                }
            }
        }
    }
}
