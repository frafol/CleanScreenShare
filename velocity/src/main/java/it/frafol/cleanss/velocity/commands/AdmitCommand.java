package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AdmitCommand implements SimpleCommand {

    private final CleanSS instance;

    public AdmitCommand(CleanSS instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {

        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ONLY_PLAYERS.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (!PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_CONTROL.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (!PlayerCache.getCouples().containsValue(player)) {
            player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_CONTROL.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        Player administrator = null;
        for (Player admin : PlayerCache.getCouples().keySet()) {
            if (PlayerCache.getCouples().get(admin).equals(player)) {
                administrator = admin;
            }
        }

        if (administrator == null) {
            player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_CONTROL.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        Utils.sendAdmit(player, administrator);
    }
}
