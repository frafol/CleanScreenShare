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
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener {

    public final CleanSS instance;

    public ChatListener(CleanSS instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onChat(@NotNull ChatEvent event) {

        if (event.getMessage().startsWith("/")) {
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (player.getServer() == null) {
            return;
        }

        if (player.getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class))) {

            if (PlayerCache.getCouples().containsKey(player)) {

                player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                        .replace("%player%", player.getName())
                        .replace("%message%", event.getMessage())
                        .replace("%state%", BungeeMessages.CONTROL_CHAT_STAFF.color())));

                instance.getValue(PlayerCache.getCouples(), player).sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                        .replace("%player%", player.getName())
                        .replace("%message%", event.getMessage())
                        .replace("%state%", BungeeMessages.CONTROL_CHAT_STAFF.color())));

                return;

            }

            if (PlayerCache.getCouples().containsValue(player)) {

                player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                        .replace("%player%", player.getName())
                        .replace("%message%", event.getMessage())
                        .replace("%state%", BungeeMessages.CONTROL_CHAT_SUS.color())));

                instance.getKey(PlayerCache.getCouples(), player).sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                        .replace("%player%", player.getName())
                        .replace("%message%", event.getMessage())
                        .replace("%state%", BungeeMessages.CONTROL_CHAT_SUS.color())));


            }
        }
    }
}