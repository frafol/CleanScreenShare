package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class AdmitCommand extends Command {

    public final CleanSS instance;

    public AdmitCommand(CleanSS instance) {
        super(BungeeCommandsConfig.SS_ADMIT.getStringList().get(0),"", BungeeCommandsConfig.SS_ADMIT.getStringList().toArray(new String[0]));
        this.instance = instance;
    }

    @Override
    public void execute(CommandSender invocation, String[] args) {

        if (!(invocation instanceof ProxiedPlayer)) {
            invocation.sendMessage(TextComponent.fromLegacy(BungeeMessages.ONLY_PLAYERS.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) invocation;
        if (!PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            invocation.sendMessage(TextComponent.fromLegacy(BungeeMessages.NOT_CONTROL.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (!PlayerCache.getCouples().containsValue(player)) {
            invocation.sendMessage(TextComponent.fromLegacy(BungeeMessages.NOT_CONTROL.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        ProxiedPlayer administrator = null;
        for (ProxiedPlayer admin : PlayerCache.getCouples().keySet()) {
            if (PlayerCache.getCouples().get(admin).equals(player)) {
                administrator = admin;
            }
        }

        if (administrator == null) {
            invocation.sendMessage(TextComponent.fromLegacy(BungeeMessages.NOT_CONTROL.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        Utils.sendAdmit(player, administrator);
    }
}
