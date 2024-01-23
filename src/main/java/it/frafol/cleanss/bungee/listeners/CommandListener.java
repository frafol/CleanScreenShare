package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class CommandListener implements Listener {

    @EventHandler
    public void onPlayerCommand(ChatEvent event) {

        if (!event.getMessage().startsWith("/")) {
            return;
        }

        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.COMMAND_BLOCKED.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (PlayerCache.getIn_control().get(player.getUniqueId()) == null) {
            return;
        }

        if (!PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            return;
        }

        for (String command : CleanSS.getInstance().getConfigTextFile().getStringList("settings.slog.ban_commands")) {
            if (event.getMessage().startsWith("/" + command + " ") && event.getMessage().contains(CleanSS.getInstance().getValue(PlayerCache.getCouples(), player).getName())) {
                PlayerCache.getBan_execution().add(player.getUniqueId());
                CleanSS.getInstance().getProxy().getScheduler().schedule(CleanSS.getInstance(), () -> PlayerCache.getBan_execution().remove(player.getUniqueId()), 2L, TimeUnit.SECONDS);
            }
        }
    }
}
