package it.frafol.cleanss.velocity.commands.rediscommands;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.Placeholder;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.redisbungee.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class InfoCommand implements SimpleCommand {

    private final CleanSS instance;
    private final RedisBungeeAPI redisBungeeAPI;

    public InfoCommand(CleanSS instance, RedisBungeeAPI redisBungeeAPI) {
        this.instance = instance;
        this.redisBungeeAPI = redisBungeeAPI;
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

        final UUID player;
        if (invocation.arguments().length == 0) {
            player = ((Player) source).getUniqueId();
        } else {
            player = redisBungeeAPI.getUuidFromName(invocation.arguments()[0]);
        }

        if (!redisBungeeAPI.isPlayerOnline(player)) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", invocation.arguments()[0])));
            return;
        }

        if (VelocityConfig.MYSQL.get(Boolean.class)) {

            if (luckperms) {
                VelocityMessages.INFO_MESSAGE.sendList(source,
                        new Placeholder("player", redisBungeeAPI.getNameFromUuid(player)),
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                        new Placeholder("is_in_control", String.valueOf(instance.getData().getStats(player, "incontrol"))),
                        new Placeholder("controls_done", String.valueOf(instance.getData().getStats(player, "controls"))),
                        new Placeholder("playerprefix", Utils.getPrefix(player)),
                        new Placeholder("playersuffix", Utils.getSuffix(player)),
                        new Placeholder("controls_suffered", String.valueOf(instance.getData().getStats(player, "suffered"))),
                        new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()));
                return;
            }

            VelocityMessages.INFO_MESSAGE.sendList(source,
                    new Placeholder("player", redisBungeeAPI.getNameFromUuid(player)),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                    new Placeholder("is_in_control", String.valueOf(instance.getData().getStats(player, "incontrol"))),
                    new Placeholder("controls_done", String.valueOf(instance.getData().getStats(player, "controls"))),
                    new Placeholder("controls_suffered", String.valueOf(instance.getData().getStats(player, "suffered"))),
                    new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()));
            return;
        }

        PlayerCache.getControls().putIfAbsent(player, 0);
        PlayerCache.getControls_suffered().putIfAbsent(player, 0);

        if (luckperms) {
            VelocityMessages.INFO_MESSAGE.sendList(source,
                    new Placeholder("player", redisBungeeAPI.getNameFromUuid(player)),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                    new Placeholder("is_in_control", PlayerCache.getSuspicious().contains(player) || PlayerCache.getAdministrator().contains(player) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()),
                    new Placeholder("controls_done", String.valueOf(PlayerCache.getControls().get(player))),
                    new Placeholder("playerprefix", Utils.getPrefix(player)),
                    new Placeholder("playersuffix", Utils.getSuffix(player)),
                    new Placeholder("controls_suffered", String.valueOf(PlayerCache.getControls_suffered().get(player))),
                    new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()));
            return;
        }

        VelocityMessages.INFO_MESSAGE.sendList(source,
                new Placeholder("player", redisBungeeAPI.getNameFromUuid(player)),
                new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                new Placeholder("is_in_control", PlayerCache.getSuspicious().contains(player) || PlayerCache.getAdministrator().contains(player) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()),
                new Placeholder("controls_done", String.valueOf(PlayerCache.getControls().get(player))),
                new Placeholder("controls_suffered", String.valueOf(PlayerCache.getControls_suffered().get(player))),
                new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player) ? VelocityMessages.INFO_TRUE.color() : VelocityMessages.INFO_FALSE.color()));

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
