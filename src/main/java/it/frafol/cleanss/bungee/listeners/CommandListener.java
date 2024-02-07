package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class CommandListener implements Listener {

    private final CleanSS instance = CleanSS.getInstance();

    @EventHandler
    public void onPlayerCommand(ChatEvent event) {

        if (!event.isCommand()) {
            return;
        }

        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        if (event.getMessage().equalsIgnoreCase("/spawn")) {
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

        for (String command : BungeeConfig.BAN_COMMANDS.getStringList()) {
            if (event.getMessage().startsWith("/" + command + " ") && event.getMessage().contains(instance.getValue(PlayerCache.getCouples(), player).getName())) {
                PlayerCache.getBan_execution().add(player.getUniqueId());
                instance.getProxy().getScheduler().schedule(instance, () -> PlayerCache.getBan_execution().remove(player.getUniqueId()), 2L, TimeUnit.SECONDS);
            }
        }
    }
}
