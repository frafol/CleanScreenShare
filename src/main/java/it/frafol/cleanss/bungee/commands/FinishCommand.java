package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

public class FinishCommand extends Command {

    public final CleanSS instance;

    public FinishCommand(CleanSS instance) {
        super("ssfinish","","screensharefinish","cleanssfinish","cleanscreensharefinish", "controlfinish");
        this.instance = instance;
    }

    @Override
    public void execute(@NotNull CommandSender invocation, String[] args) {

        if (!invocation.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class))) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (!(invocation instanceof ProxiedPlayer)) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.ONLY_PLAYERS.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (args.length == 0) {

            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.USAGE.color().replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;

        }

        if (args.length == 1) {

            if (instance.getProxy().getPlayers().toString().contains(args[0])) {

                final ProxiedPlayer player = instance.getProxy().getPlayer(args[0]);
                final ServerInfo proxyServer = instance.getProxy().getServerInfo(BungeeConfig.CONTROL_FALLBACK.get(String.class));

                if (player == null) {
                    invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_ONLINE.get(String.class)
                            .replace("%prefix%", BungeeMessages.PREFIX.color())));
                    return;
                }

                if (!PlayerCache.getSuspicious().contains(player.getUniqueId())) {
                    invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_CONTROL.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())));
                    return;
                }

                if (instance.getValue(PlayerCache.getCouples(), ((ProxiedPlayer) invocation)) == null || instance.getValue(PlayerCache.getCouples(), ((ProxiedPlayer) invocation)) != player) {
                    invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_CONTROL.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())));
                    return;
                }

                Utils.finishControl(player, (ProxiedPlayer) invocation, proxyServer);
                Utils.sendDiscordMessage(player, (ProxiedPlayer) invocation, BungeeMessages.DISCORD_FINISHED.get(String.class));

            }
        }
    }
}
