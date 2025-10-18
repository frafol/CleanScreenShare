package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.Placeholder;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import it.frafol.cleanss.bungee.objects.handlers.DataHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InfoCommand extends Command implements TabExecutor {

    public final CleanSS instance;

    public InfoCommand(CleanSS instance) {
        super(BungeeCommandsConfig.SS_INFO.getStringList().get(0),"", BungeeCommandsConfig.SS_INFO.getStringList().toArray(new String[0]));
        this.instance = instance;
    }

    @Override
    public void execute(CommandSender invocation, String[] args) {

        if (!invocation.hasPermission(BungeeConfig.INFO_PERMISSION.get(String.class))) {
            invocation.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (args.length > 1) {
            BungeeMessages.USAGE.sendList(invocation,
					new Placeholder("%prefix%", BungeeMessages.PREFIX.color()));
            return;
        }

        if (args.length == 0 && !(invocation instanceof ProxiedPlayer)) {
            BungeeMessages.USAGE.sendList(invocation,
                    new Placeholder("%prefix%", BungeeMessages.PREFIX.color()));
            return;
        }

        ProxiedPlayer player;
        if (args.length == 0) {
            player = instance.getProxy().getPlayer(invocation.getName());
        } else {
            player = instance.getProxy().getPlayer(args[0]);
        }

        if (player == null || !player.isConnected()) {
            invocation.sendMessage(TextComponent.fromLegacy(BungeeMessages.NOT_ONLINE.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (BungeeConfig.MYSQL.get(Boolean.class)) {

            if (Utils.isLuckPerms) {
                BungeeMessages.INFO_MESSAGE.sendList(invocation,
                        new Placeholder("player", player.getName()),
                        new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                        new Placeholder("is_in_control", String.valueOf(instance.getData().getStats(player.getUniqueId(), "incontrol"))),
                        new Placeholder("controls_done", String.valueOf(instance.getData().getStats(player.getUniqueId(), "controls"))),
                        new Placeholder("playerprefix", Utils.getPrefix(player)),
                        new Placeholder("playersuffix", Utils.getSuffix(player)),
                        new Placeholder("controls_suffered", String.valueOf(instance.getData().getStats(player.getUniqueId(), "suffered"))),
                        new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.getUniqueId()) ? BungeeMessages.INFO_TRUE.color() : BungeeMessages.INFO_FALSE.color()));
                return;
            }

            BungeeMessages.INFO_MESSAGE.sendList(invocation,
                    new Placeholder("player", player.getName()),
                    new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                    new Placeholder("is_in_control", String.valueOf(instance.getData().getStats(player.getUniqueId(), "incontrol"))),
                    new Placeholder("controls_done", String.valueOf(instance.getData().getStats(player.getUniqueId(), "controls"))),
                    new Placeholder("controls_suffered", String.valueOf(instance.getData().getStats(player.getUniqueId(), "suffered"))),
                    new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.getUniqueId()) ? BungeeMessages.INFO_TRUE.color() : BungeeMessages.INFO_FALSE.color()));
            return;
        }

        PlayerCache.getControls().put(player.getUniqueId(), DataHandler.getStat(player.getUniqueId(), "done"));
        PlayerCache.getControls_suffered().put(player.getUniqueId(), DataHandler.getStat(player.getUniqueId(), "suffered"));

        if (Utils.isLuckPerms) {
            BungeeMessages.INFO_MESSAGE.sendList(invocation,
                    new Placeholder("player", player.getName()),
                    new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                    new Placeholder("is_in_control", PlayerCache.getSuspicious().contains(player.getUniqueId()) || PlayerCache.getAdministrator().contains(player.getUniqueId()) ? BungeeMessages.INFO_TRUE.color() : BungeeMessages.INFO_FALSE.color()),
                    new Placeholder("controls_done", String.valueOf(PlayerCache.getControls().get(player.getUniqueId()))),
                    new Placeholder("playerprefix", Utils.getPrefix(player)),
                    new Placeholder("playersuffix", Utils.getSuffix(player)),
                    new Placeholder("controls_suffered", String.valueOf(PlayerCache.getControls_suffered().get(player.getUniqueId()))),
                    new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.getUniqueId()) ? BungeeMessages.INFO_TRUE.color() : BungeeMessages.INFO_FALSE.color()));
            return;
        }

        BungeeMessages.INFO_MESSAGE.sendList(invocation,
                new Placeholder("player", player.getName()),
                new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                new Placeholder("is_in_control", PlayerCache.getSuspicious().contains(player.getUniqueId()) || PlayerCache.getAdministrator().contains(player.getUniqueId()) ? BungeeMessages.INFO_TRUE.color() : BungeeMessages.INFO_FALSE.color()),
                new Placeholder("controls_done", String.valueOf(PlayerCache.getControls().get(player.getUniqueId()))),
                new Placeholder("controls_suffered", String.valueOf(PlayerCache.getControls_suffered().get(player.getUniqueId()))),
                new Placeholder("is_spectating", PlayerCache.getSpectators().contains(player.getUniqueId()) ? BungeeMessages.INFO_TRUE.color() : BungeeMessages.INFO_FALSE.color()));

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String [] args) {

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
