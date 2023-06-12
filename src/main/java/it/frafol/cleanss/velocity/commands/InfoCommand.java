package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class InfoCommand implements SimpleCommand {

    private final CleanSS instance;

    public InfoCommand(CleanSS instance) {
        this.instance = instance;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {

        final CommandSource source = invocation.source();

        if (!source.hasPermission(VelocityConfig.INFO_PERMISSION.get(String.class))) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (invocation.arguments().length == 0) {

            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.USAGE.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;

        }

        if (invocation.arguments().length == 1) {

            if (instance.getServer().getAllPlayers().toString().contains(invocation.arguments()[0])) {

                final Optional<Player> player = instance.getServer().getPlayer(invocation.arguments()[0]);

                if (!player.isPresent()) {
                    source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("%player%", invocation.arguments()[0])));
                    return;
                }

                if (VelocityConfig.MYSQL.get(Boolean.class)) {
                    VelocityMessages.INFO_MESSAGE.sendList(source, player.get(),
                            new Placeholder("player", invocation.arguments()[0]),
                            new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                            new Placeholder("is_in_control", String.valueOf(instance.getData().getStats(player.get().getUniqueId(), "incontrol"))),
                            new Placeholder("controls_done", String.valueOf(instance.getData().getStats(player.get().getUniqueId(), "controls"))),
                            new Placeholder("controls_suffered", String.valueOf(instance.getData().getStats(player.get().getUniqueId(), "suffered"))));
                    return;
                }

                PlayerCache.getControls().putIfAbsent(player.get().getUniqueId(), 0);
                PlayerCache.getControls_suffered().putIfAbsent(player.get().getUniqueId(), 0);

                VelocityMessages.INFO_MESSAGE.sendList(source, player.get(),
                        new Placeholder("player", invocation.arguments()[0]),
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                        new Placeholder("is_in_control", String.valueOf(PlayerCache.getSuspicious().contains(player.get().getUniqueId()))),
                        new Placeholder("controls_done", String.valueOf(PlayerCache.getControls().get(player.get().getUniqueId()))),
                        new Placeholder("controls_suffered", String.valueOf(PlayerCache.getControls_suffered().get(player.get().getUniqueId()))));

            } else {
                source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", invocation.arguments()[0])));
            }
        }
    }
}
