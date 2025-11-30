package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.*;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ChatListener implements Listener  {

    public final CleanSS instance;

    public ChatListener(CleanSS instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        final String message = event.getMessage();
        boolean luckperms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;
        if (message.startsWith("/")) return;
        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        if (player.getServer() == null) return;
        if (!Utils.isInControlServer(player.getServer().getInfo())) return;
        if (PlayerCache.getSpectators().contains(player.getUniqueId()) && !BungeeConfig.CHAT_DISABLED.get(Boolean.class)) return;
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
            if (user == null) return;
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
            String userPrefix = ChatUtil.color(user_prefix);
            String userSuffix = ChatUtil.color(user_suffix);
            if (BungeeConfig.PAPI_PROXYBRIDGE.get(Boolean.class) && instance.getPAPIProxyBridge()) {
                sendAdminPAPIProxyBridge(player, event.getMessage(), userPrefix, userSuffix);
                return;
            }

            if (BungeeConfig.PAPI_NATIVE.get(Boolean.class)) {
                prepareSending(player, event.getMessage(), userPrefix, userSuffix, true);
            }

            sendMessage(player, event.getMessage(), userPrefix, userSuffix, true);
            return;
        }

        if (PlayerCache.getCouples().containsValue(player)) {

            event.setCancelled(true);
            String userPrefix = ChatUtil.color(user_prefix);
            String userSuffix = ChatUtil.color(user_suffix);
            if (BungeeConfig.PAPI_PROXYBRIDGE.get(Boolean.class) && instance.getPAPIProxyBridge()) {
                sendSuspectPAPIProxyBridge(player, event.getMessage(), userPrefix, userSuffix);
                return;
            }

            if (BungeeConfig.PAPI_NATIVE.get(Boolean.class)) {
                prepareSending(player, event.getMessage(), userPrefix, userSuffix, false);
                return;
            }

            sendMessage(player, event.getMessage(), userPrefix, userSuffix, false);
        }
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase("cleanss:chat")) return;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()))) {
            String sub = in.readUTF();
            if (!sub.equals("CHAT")) return;
            String player = in.readUTF();
            boolean staff = Boolean.parseBoolean(in.readUTF());
            String msg = in.readUTF();
            if (staff) sendAdminMessage(player, msg);
            else sendSuspectMessage(player, msg);
        } catch (Exception ignored) {}
    }

    private void sendAdminMessage(String playerName, String msg) {
        ProxiedPlayer player = instance.getProxy().getPlayer(playerName);
        if (player == null || !player.isConnected()) return;
        BaseComponent component = TextComponent.fromLegacy(msg);
        if (BungeeConfig.TAKE_CHATLOGS.get(Boolean.class)) {
            LogUtils.addLine(
                    player.getName(),
                    BaseComponent.toLegacyText(component));
        }
        player.sendMessage(component);
        instance.getValue(PlayerCache.getCouples(), player).sendMessage(component);
        instance.getProxy().getPlayers().stream()
                .filter(players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                .forEach(players -> players.sendMessage(component));
    }

    private void sendSuspectMessage(String playerName, String msg) {
        ProxiedPlayer player = instance.getProxy().getPlayer(playerName);
        if (player == null || !player.isConnected()) return;
        BaseComponent component = TextComponent.fromLegacy(msg);
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

    private void sendAdminPAPIProxyBridge(ProxiedPlayer player, String message, String userPrefix, String userSuffix) {
        CompletableFuture<String> stateFuture = BungeeMessages.CONTROL_CHAT_STAFF.color(player.getUniqueId());
        CompletableFuture<String> formatFuture = BungeeMessages.CONTROL_CHAT_FORMAT.color(player.getUniqueId());
        CompletableFuture.allOf(formatFuture, stateFuture).thenAccept(__ -> {
            String formatString = formatFuture.getNow(BungeeMessages.CONTROL_CHAT_FORMAT.color());
            String state = stateFuture.getNow(BungeeMessages.CONTROL_CHAT_STAFF.color());
            String finalString = formatString
                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                    .replace("%player%", player.getName())
                    .replace("%message%", message)
                    .replace("%userprefix%", userPrefix)
                    .replace("%usersuffix%", userSuffix)
                    .replace("%state%", state);

            BaseComponent component = TextComponent.fromLegacy(finalString);
            if (BungeeConfig.TAKE_CHATLOGS.get(Boolean.class)) {
                LogUtils.addLine(
                        player.getName(),
                        BaseComponent.toLegacyText(component));
            }

            player.sendMessage(component);
            instance.getValue(PlayerCache.getCouples(), player).sendMessage(component);
            instance.getProxy().getPlayers().stream()
                    .filter(players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                    .forEach(players -> players.sendMessage(component));
        });
    }

    private void sendSuspectPAPIProxyBridge(ProxiedPlayer player, String message, String userPrefix, String userSuffix) {
        CompletableFuture<String> stateFuture = BungeeMessages.CONTROL_CHAT_SUS.color(player.getUniqueId());
        CompletableFuture<String> formatFuture = BungeeMessages.CONTROL_CHAT_FORMAT.color(player.getUniqueId());
        CompletableFuture.allOf(formatFuture, stateFuture).thenAccept(__ -> {
            String formatString = formatFuture.getNow(BungeeMessages.CONTROL_CHAT_FORMAT.color());
            String state = stateFuture.getNow(BungeeMessages.CONTROL_CHAT_SUS.color());
            String finalString = formatString
                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                    .replace("%player%", player.getName())
                    .replace("%message%", message)
                    .replace("%userprefix%", userPrefix)
                    .replace("%usersuffix%", userSuffix)
                    .replace("%state%", state);

            BaseComponent component = TextComponent.fromLegacy(finalString);
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
        });
    }

    private void prepareSending(ProxiedPlayer player, String message, String userPrefix, String userSuffix, boolean staff) {
        String formatString = BungeeMessages.CONTROL_CHAT_FORMAT.color();
        String state;
        if (staff) state = BungeeMessages.CONTROL_CHAT_STAFF.color();
        else state = BungeeMessages.CONTROL_CHAT_SUS.color();
        String finalMessage = formatString
                .replace("%prefix%", BungeeMessages.PREFIX.color())
                .replace("%player%", player.getName())
                .replace("%message%", message)
                .replace("%userprefix%", userPrefix)
                .replace("%usersuffix%", userSuffix)
                .replace("%state%", state);
        MessageUtil.sendChatPAPIMessage(player, finalMessage, staff);
    }

    private void sendMessage(ProxiedPlayer player, String message, String userPrefix, String userSuffix, boolean staff) {
        String formatString = BungeeMessages.CONTROL_CHAT_FORMAT.color();
        String state;
        if (staff) state = BungeeMessages.CONTROL_CHAT_STAFF.color();
        else state = BungeeMessages.CONTROL_CHAT_SUS.color();
        String finalMessage = formatString
                .replace("%prefix%", BungeeMessages.PREFIX.color())
                .replace("%player%", player.getName())
                .replace("%message%", message)
                .replace("%userprefix%", userPrefix)
                .replace("%usersuffix%", userSuffix)
                .replace("%state%", state);

        BaseComponent component = TextComponent.fromLegacy(finalMessage);
        if (staff) {
            if (BungeeConfig.TAKE_CHATLOGS.get(Boolean.class)) {
                LogUtils.addLine(
                        player.getName(),
                        BaseComponent.toLegacyText(component));
            }

            player.sendMessage(component);
            instance.getValue(PlayerCache.getCouples(), player).sendMessage(component);
            instance.getProxy().getPlayers().stream()
                    .filter(players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                    .forEach(players -> players.sendMessage(component));
            return;
        }

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
