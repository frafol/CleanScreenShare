package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.Placeholder;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InfoCommand extends Command implements TabExecutor {

    public final CleanSS instance;

    public InfoCommand(CleanSS instance) {
        super("ssinfo","","screenshareinfo","cleanssinfo","cleanscreenshareinfo", "controlinfo");
        this.instance = instance;
    }

    @Override
    public void execute(@NotNull CommandSender invocation, String[] args) {

        if (!invocation.hasPermission(BungeeConfig.INFO_PERMISSION.get(String.class))) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (args.length != 1) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.USAGE.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (!instance.getProxy().getPlayers().toString().contains(args[0])) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_ONLINE.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        final ProxiedPlayer player = instance.getProxy().getPlayer(args[0]);

        if (player == null) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_ONLINE.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (BungeeConfig.MYSQL.get(Boolean.class)) {

            if (Utils.isLuckPerms) {
                BungeeMessages.INFO_MESSAGE.sendList(invocation, player,
                        new Placeholder("player", args[0]),
                        new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                        new Placeholder("is_in_control", String.valueOf(instance.getData().getStats(player.getUniqueId(), "incontrol"))),
                        new Placeholder("controls_done", String.valueOf(instance.getData().getStats(player.getUniqueId(), "controls"))),
                        new Placeholder("playerprefix", Utils.getPrefix(player)),
                        new Placeholder("playersuffix", Utils.getSuffix(player)),
                        new Placeholder("controls_suffered", String.valueOf(instance.getData().getStats(player.getUniqueId(), "suffered"))),
                        new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.getUniqueId()) ? "true" : "false"));
                return;
            }

            BungeeMessages.INFO_MESSAGE.sendList(invocation, player,
                    new Placeholder("player", args[0]),
                    new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                    new Placeholder("is_in_control", String.valueOf(instance.getData().getStats(player.getUniqueId(), "incontrol"))),
                    new Placeholder("controls_done", String.valueOf(instance.getData().getStats(player.getUniqueId(), "controls"))),
                    new Placeholder("controls_suffered", String.valueOf(instance.getData().getStats(player.getUniqueId(), "suffered"))),
                    new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.getUniqueId()) ? "true" : "false"));
            return;
        }

        PlayerCache.getControls().putIfAbsent(player.getUniqueId(), 0);
        PlayerCache.getControls_suffered().putIfAbsent(player.getUniqueId(), 0);

        if (Utils.isLuckPerms) {
            BungeeMessages.INFO_MESSAGE.sendList(invocation, player,
                    new Placeholder("player", args[0]),
                    new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                    new Placeholder("is_in_control", String.valueOf(PlayerCache.getSuspicious().contains(player.getUniqueId()) || PlayerCache.getAdministrator().contains(player.getUniqueId()))),
                    new Placeholder("controls_done", String.valueOf(PlayerCache.getControls().get(player.getUniqueId()))),
                    new Placeholder("playerprefix", Utils.getPrefix(player)),
                    new Placeholder("playersuffix", Utils.getSuffix(player)),
                    new Placeholder("controls_suffered", String.valueOf(PlayerCache.getControls_suffered().get(player.getUniqueId()))),
                    new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.getUniqueId()) ? "true" : "false"));
            return;
        }

        BungeeMessages.INFO_MESSAGE.sendList(invocation, player,
                new Placeholder("player", args[0]),
                new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                new Placeholder("is_in_control", String.valueOf(PlayerCache.getSuspicious().contains(player.getUniqueId()))),
                new Placeholder("controls_done", String.valueOf(PlayerCache.getControls().get(player.getUniqueId()))),
                new Placeholder("controls_suffered", String.valueOf(PlayerCache.getControls_suffered().get(player.getUniqueId()))),
                new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.getUniqueId()) ? "true" : "false"));

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String @NotNull [] args) {

        if (args.length != 1) {
            return Collections.emptyList();
        }

        String partialName = args[0].toLowerCase();

        List<String> completions = new ArrayList<>();
        for (ProxiedPlayer player : instance.getProxy().getPlayers()) {
            if (player.getName().toLowerCase().startsWith(partialName)) {
                completions.add(player.getName());
            }
        }

        return completions;
    }
}
