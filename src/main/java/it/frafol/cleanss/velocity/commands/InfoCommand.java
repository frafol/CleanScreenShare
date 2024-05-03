package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.Placeholder;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class InfoCommand implements SimpleCommand {

    private final CleanSS instance;

    public InfoCommand(CleanSS instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {

        final CommandSource source = invocation.source();
        boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();

        if (!source.hasPermission(VelocityConfig.INFO_PERMISSION.get(String.class))) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (invocation.arguments().length > 1) {
            VelocityMessages.USAGE.sendList(source, null,
                    new Placeholder("%prefix%", VelocityMessages.PREFIX.color()));
            return;
        }

        final Optional<Player> player;
        if (invocation.arguments().length == 0) {
            player = Optional.of((Player) source);
        } else {
            player = instance.getServer().getPlayer(invocation.arguments()[0]);
        }

        if (!player.isPresent()) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", invocation.arguments()[0])));
            return;
        }

        if (VelocityConfig.MYSQL.get(Boolean.class)) {

            if (luckperms) {
                VelocityMessages.INFO_MESSAGE.sendList(source,
                        new Placeholder("player", invocation.arguments()[0]),
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                        new Placeholder("is_in_control", String.valueOf(instance.getData().getStats(player.get().getUniqueId(), "incontrol"))),
                        new Placeholder("controls_done", String.valueOf(instance.getData().getStats(player.get().getUniqueId(), "controls"))),
                        new Placeholder("playerprefix", Utils.getPrefix(player.get())),
                        new Placeholder("playersuffix", Utils.getSuffix(player.get())),
                        new Placeholder("controls_suffered", String.valueOf(instance.getData().getStats(player.get().getUniqueId(), "suffered"))),
                        new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.get().getUniqueId()) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()));
                return;
            }

            VelocityMessages.INFO_MESSAGE.sendList(source,
                    new Placeholder("player", invocation.arguments()[0]),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                    new Placeholder("is_in_control", String.valueOf(instance.getData().getStats(player.get().getUniqueId(), "incontrol"))),
                    new Placeholder("controls_done", String.valueOf(instance.getData().getStats(player.get().getUniqueId(), "controls"))),
                    new Placeholder("controls_suffered", String.valueOf(instance.getData().getStats(player.get().getUniqueId(), "suffered"))),
                    new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.get().getUniqueId()) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()));
            return;
        }

        PlayerCache.getControls().putIfAbsent(player.get().getUniqueId(), 0);
        PlayerCache.getControls_suffered().putIfAbsent(player.get().getUniqueId(), 0);

        if (luckperms) {
            VelocityMessages.INFO_MESSAGE.sendList(source,
                    new Placeholder("player", invocation.arguments()[0]),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                    new Placeholder("is_in_control", PlayerCache.getSuspicious().contains(player.get().getUniqueId()) || PlayerCache.getAdministrator().contains(player.get().getUniqueId()) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()),
                    new Placeholder("controls_done", String.valueOf(PlayerCache.getControls().get(player.get().getUniqueId()))),
                    new Placeholder("playerprefix", Utils.getPrefix(player.get())),
                    new Placeholder("playersuffix", Utils.getSuffix(player.get())),
                    new Placeholder("controls_suffered", String.valueOf(PlayerCache.getControls_suffered().get(player.get().getUniqueId()))),
                    new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.get().getUniqueId()) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()));
            return;
        }

        VelocityMessages.INFO_MESSAGE.sendList(source,
                new Placeholder("player", invocation.arguments()[0]),
                new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                new Placeholder("is_in_control", PlayerCache.getSuspicious().contains(player.get().getUniqueId()) || PlayerCache.getAdministrator().contains(player.get().getUniqueId()) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()),
                new Placeholder("controls_done", String.valueOf(PlayerCache.getControls().get(player.get().getUniqueId()))),
                new Placeholder("controls_suffered", String.valueOf(PlayerCache.getControls_suffered().get(player.get().getUniqueId()))),
                new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.get().getUniqueId()) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()));

    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        final String[] strings = invocation.arguments();

        if (Utils.isConsole(invocation.source())) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        final List<String> players = instance.getServer().getAllPlayers().stream()
                .map(Player::getUsername)
                .filter(player -> strings.length != 1 || strings[0].isEmpty()
                        || player.toLowerCase().startsWith(strings[0].toLowerCase()))
                .collect(Collectors.toList());

        return CompletableFuture.completedFuture(players);
    }
}
