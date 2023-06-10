package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

        if (Utils.isConsole(source)) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ONLY_PLAYERS.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (!source.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class))) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (invocation.arguments().length == 0) {

            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.USAGE.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;

        }

        if (invocation.arguments().length == 1) {

            if (instance.getServer().getAllPlayers().toString().contains(invocation.arguments()[0])) {

                final Optional<Player> player = instance.getServer().getPlayer(invocation.arguments()[0]);
                final Optional<RegisteredServer> proxyServer = instance.getServer().getServer(VelocityConfig.CONTROL_FALLBACK.get(String.class));
                final Player sender = (Player) invocation.source();

                if (!player.isPresent()) {
                    source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.get(String.class)
                            .replace("%prefix%", VelocityMessages.PREFIX.color())));
                    return;
                }

                if (!PlayerCache.getSuspicious().contains(player.get().getUniqueId())) {
                    source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_CONTROL.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
                    return;
                }

                if (instance.getValue(PlayerCache.getCouples(), sender) == null || instance.getValue(PlayerCache.getCouples(), sender) != player.get()) {
                    source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_CONTROL.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
                    return;
                }

                if (!proxyServer.isPresent()) {
                    return;
                }

                Utils.finishControl(player.get(), sender, proxyServer.get());
                Utils.sendDiscordMessage(player.get(), sender, VelocityMessages.DISCORD_FINISHED.get(String.class));

            } else {

                source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", invocation.arguments()[0])));

            }
        }
    }
}
