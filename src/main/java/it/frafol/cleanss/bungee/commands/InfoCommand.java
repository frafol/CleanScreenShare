package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.Placeholder;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

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

        if (args.length == 0) {

            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.USAGE.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;

        }

        if (args.length == 1) {

            if (instance.getProxy().getPlayers().toString().contains(args[0])) {

                final ProxiedPlayer player = instance.getProxy().getPlayer(args[0]);

                if (player == null) {
                    invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_ONLINE.get(String.class)
                            .replace("%prefix%", BungeeMessages.PREFIX.color())));
                    return;
                }

                if (BungeeConfig.MYSQL.get(Boolean.class)) {
                    BungeeMessages.INFO_MESSAGE.sendList(invocation, player,
                            new Placeholder("player", args[0]),
                            new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                            new Placeholder("is_in_control", String.valueOf(instance.getData().getStats(player.getUniqueId(), "incontrol"))),
                            new Placeholder("controls_done", String.valueOf(instance.getData().getStats(player.getUniqueId(), "controls"))),
                            new Placeholder("controls_suffered", String.valueOf(instance.getData().getStats(player.getUniqueId(), "suffered"))));
                    return;
                }

                PlayerCache.getControls().putIfAbsent(player.getUniqueId(), 0);
                PlayerCache.getControls_suffered().putIfAbsent(player.getUniqueId(), 0);

                BungeeMessages.INFO_MESSAGE.sendList(invocation, player,
                        new Placeholder("player", args[0]),
                        new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                        new Placeholder("is_in_control", String.valueOf(PlayerCache.getSuspicious().contains(player.getUniqueId()))),
                        new Placeholder("controls_done", String.valueOf(PlayerCache.getControls().get(player.getUniqueId()))),
                        new Placeholder("controls_suffered", String.valueOf(PlayerCache.getControls_suffered().get(player.getUniqueId()))));

            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String @NotNull [] args) {
        Set<String> list = new HashSet<>();

        if (args.length == 1) {
            for (ProxiedPlayer players : instance.getProxy().getPlayers()) {
                list.add(players.getName());
            }
        }
        return list;
    }
}
