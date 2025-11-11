package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.ChatUtil;
import it.frafol.cleanss.bungee.objects.LogUtils;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {

    public final CleanSS instance;

    public ChatListener(CleanSS instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onChat(ChatEvent event) {

        final String message = event.getMessage();
        boolean luckperms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;

        if (message.startsWith("/")) {
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (player.getServer() == null) {
            return;
        }

        if (!Utils.isInControlServer(player.getServer().getInfo())) {
            return;
        }

        if (PlayerCache.getSpectators().contains(player.getUniqueId()) && !BungeeConfig.CHAT_DISABLED.get(Boolean.class)) {
            return;
        }

        if (PlayerCache.getSpectators().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(TextComponent.fromLegacy(BungeeMessages.CHAT_DISABLED.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        String user_prefix;
        String user_suffix;

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

        } else {
            user_suffix = "";
            user_prefix = "";
        }

        if (PlayerCache.getCouples().containsKey(player)) {

            event.setCancelled(true);

            BaseComponent component = TextComponent.fromLegacy(BungeeMessages.CONTROL_CHAT_FORMAT.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                    .replace("%player%", player.getName())
                    .replace("%message%", event.getMessage())
                    .replace("%userprefix%", ChatUtil.color(user_prefix))
                    .replace("%usersuffix%", ChatUtil.color(user_suffix))
                    .replace("%state%", BungeeMessages.CONTROL_CHAT_STAFF.color()));

            if (BungeeConfig.TAKE_CHATLOGS.get(Boolean.class)) {
                LogUtils.addLine(
                        player.getName(),
                        component.toLegacyText());
            }

            player.sendMessage(component);
            instance.getValue(PlayerCache.getCouples(), player).sendMessage(component);
            instance.getProxy().getPlayers().stream().filter
                            (players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                    .forEach(players -> players.sendMessage(component));
            return;
        }

        if (PlayerCache.getCouples().containsValue(player)) {

            event.setCancelled(true);

            BaseComponent component = TextComponent.fromLegacy(BungeeMessages.CONTROL_CHAT_FORMAT.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                    .replace("%player%", player.getName())
                    .replace("%message%", event.getMessage())
                    .replace("%userprefix%", ChatUtil.color(user_prefix))
                    .replace("%usersuffix%", ChatUtil.color(user_suffix))
                    .replace("%state%", BungeeMessages.CONTROL_CHAT_SUS.color()));

            if (BungeeConfig.TAKE_CHATLOGS.get(Boolean.class)) {
                LogUtils.addLine(
                        instance.getKey(PlayerCache.getCouples(), player).getName(),
                        component.toLegacyText());
            }

            player.sendMessage(component);
            instance.getKey(PlayerCache.getCouples(), player).sendMessage(component);
            instance.getProxy().getPlayers().stream().filter
                            (players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                    .forEach(players -> players.sendMessage(component));
        }
    }
}
