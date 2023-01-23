package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

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

        final ProxiedPlayer sender = (ProxiedPlayer) invocation;

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
                    invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_CONTROL.color().replace("%prefix%", BungeeMessages.PREFIX.color())));
                    return;
                }

                if (instance.getValue(PlayerCache.getCouples(), ((ProxiedPlayer) invocation)) == null || instance.getValue(PlayerCache.getCouples(), ((ProxiedPlayer) invocation)) != player) {
                    invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_CONTROL.color().replace("%prefix%", BungeeMessages.PREFIX.color())));
                    return;
                }

                PlayerCache.getAdministrator().remove(((ProxiedPlayer) invocation).getUniqueId());
                PlayerCache.getSuspicious().remove(player.getUniqueId());
                PlayerCache.getCouples().remove(((ProxiedPlayer) invocation), player);

                if (player.getServer() == null) {
                    return;
                }

                if (player.getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class))) {

                    if (proxyServer == null) {
                        return;
                    }

                    player.connect(proxyServer);

                    if (BungeeMessages.CONTROLFINISH_USETITLE.get(Boolean.class)) {

                        final Title title = ProxyServer.getInstance().createTitle();

                        title.fadeIn(BungeeMessages.CONTROLFINISH_FADEIN.get(Integer.class) * 20);
                        title.stay(BungeeMessages.CONTROLFINISH_STAY.get(Integer.class) * 20);
                        title.fadeOut(BungeeMessages.CONTROLFINISH_FADEOUT.get(Integer.class) * 20);

                        title.title(new TextComponent(BungeeMessages.CONTROLFINISH_TITLE.color()));
                        title.subTitle(new TextComponent(BungeeMessages.CONTROLFINISH_SUBTITLE.color()));

                        ProxyServer.getInstance().getScheduler().schedule(instance, () ->
                                title.send(player), BungeeMessages.CONTROLFINISH_DELAY.get(Integer.class), TimeUnit.SECONDS);
                    }

                    player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color().replace("%prefix%", BungeeMessages.PREFIX.color())));

                    if (sender.getServer() == null) {
                        return;
                    }

                    if (sender.getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class))) {
                        sender.connect(proxyServer);
                    }
                }
            }
        }
    }
}
