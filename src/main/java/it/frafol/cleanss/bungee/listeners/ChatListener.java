package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
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

        final String message = event.getMessage();
        boolean luckperms = instance.getProxy().getPluginManager().getPlugin("LuckPermsBungee") != null;

        if (message.startsWith("/")) {
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (player.getServer() == null) {
            return;
        }

        if (player.getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class))) {

            String user_prefix = "";
            String user_suffix = "";

            if (luckperms) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(player.getUniqueId());

                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                user_prefix = prefix == null ? "" : prefix;
                user_suffix = suffix == null ? "" : suffix;

            }

            if (PlayerCache.getCouples().containsKey(player)) {

                event.setCancelled(true);

                player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                        .replace("%player%", player.getName())
                        .replace("%message%", event.getMessage())
                        .replace("%userprefix%", user_prefix)
                        .replace("%usersuffix%", user_suffix)
                        .replace("%state%", BungeeMessages.CONTROL_CHAT_STAFF.color())));

                instance.getValue(PlayerCache.getCouples(), player).sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                        .replace("%player%", player.getName())
                        .replace("%message%", event.getMessage())
                        .replace("%userprefix%", user_prefix)
                        .replace("%usersuffix%", user_suffix)
                        .replace("%state%", BungeeMessages.CONTROL_CHAT_STAFF.color())));

                return;

            }

            if (PlayerCache.getCouples().containsValue(player)) {

                event.setCancelled(true);

                player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                        .replace("%player%", player.getName())
                        .replace("%message%", event.getMessage())
                        .replace("%userprefix%", user_prefix)
                        .replace("%usersuffix%", user_suffix)
                        .replace("%state%", BungeeMessages.CONTROL_CHAT_SUS.color())));

                instance.getKey(PlayerCache.getCouples(), player).sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                        .replace("%player%", player.getName())
                        .replace("%message%", event.getMessage())
                        .replace("%userprefix%", user_prefix)
                        .replace("%usersuffix%", user_suffix)
                        .replace("%state%", BungeeMessages.CONTROL_CHAT_SUS.color())));

            }
        }
    }
}
