package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.ChatUtil;
import it.frafol.cleanss.velocity.objects.LogUtils;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import it.frafol.cleanss.velocity.objects.MessageUtil;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.concurrent.CompletableFuture;

public class ChatListener {

    public final CleanSS instance;

    public ChatListener(CleanSS instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {

        final Player player = event.getPlayer();
        boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();
        if (player.getCurrentServer().isEmpty()) return;
        if (!Utils.isInControlServer(player.getCurrentServer().get().getServer())) return;
        if (PlayerCache.getSpectators().contains(player.getUniqueId()) && !VelocityConfig.CHAT_DISABLED.get(Boolean.class)) return;
        if (PlayerCache.getSpectators().contains(player.getUniqueId())) {
            player.sendMessage(
                    LegacyComponentSerializer.legacy('§').deserialize(
                            VelocityMessages.CHAT_DISABLED.color()
                                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            if (!(player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()
                    && !instance.getUnsignedVelocityAddon())) {
                event.setResult(PlayerChatEvent.ChatResult.denied());
            }
            return;
        }

        if (!(player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()
                && !instance.getUnsignedVelocityAddon())) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
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

            String userPrefix = ChatUtil.color(user_prefix);
            String userSuffix = ChatUtil.color(user_suffix);
            if (VelocityConfig.PAPI_PROXYBRIDGE.get(Boolean.class) && instance.getPapiProxyBridge()) {
                sendAdminPAPIProxyBridge(player, event.getMessage(), userPrefix, userSuffix);
                return;
            }

            if (VelocityConfig.PAPI_NATIVE.get(Boolean.class)) {
                prepareSending(player, event.getMessage(), userPrefix, userSuffix, true);
                return;
            }

            sendMessage(player, event.getMessage(), userPrefix, userSuffix, true);
        }

        if (PlayerCache.getCouples().containsValue(player)) {

            String userPrefix = ChatUtil.color(user_prefix);
            String userSuffix = ChatUtil.color(user_suffix);

            if (VelocityConfig.PAPI_PROXYBRIDGE.get(Boolean.class) && instance.getPapiProxyBridge()) {
                sendSuspectPAPIProxyBridge(player, event.getMessage(), userPrefix, userSuffix);
            }

            if (VelocityConfig.PAPI_NATIVE.get(Boolean.class)) {
                prepareSending(player, event.getMessage(), userPrefix, userSuffix, false);
                return;
            }

            sendMessage(player, event.getMessage(), userPrefix, userSuffix, false);
        }
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().getId().equalsIgnoreCase("cleanss:chat")) return;
        byte[] data = event.getData();
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            String sub = in.readUTF();
            if (!sub.equals("CHAT")) return;
            String playerName = in.readUTF();
            boolean staff = Boolean.parseBoolean(in.readUTF());
            String msg = in.readUTF();
            if (staff) sendAdminMessage(playerName, msg);
            else sendSuspectMessage(playerName, msg);
        } catch (Exception ignored) {}
    }

    private void sendAdminMessage(String playerName, String msg) {
        Player player = instance.getServer().getPlayer(playerName).orElse(null);
        if (player == null) return;
        TextComponent component = LegacyComponentSerializer.legacySection().deserialize(msg);
        if (VelocityConfig.TAKE_CHATLOGS.get(Boolean.class)) {
            LogUtils.addLine(
                    player.getUsername(),
                    LegacyComponentSerializer.legacySection().serialize(component)
            );
        }

        player.sendMessage(component);
        instance.getValue(PlayerCache.getCouples(), player).sendMessage(component);
        instance.getServer().getAllPlayers().stream()
                .filter(p -> PlayerCache.getSpectators().contains(p.getUniqueId()))
                .forEach(p -> p.sendMessage(component));
    }

    private void sendSuspectMessage(String playerName, String msg) {
        Player player = instance.getServer().getPlayer(playerName).orElse(null);
        if (player == null) return;
        TextComponent component = LegacyComponentSerializer.legacySection().deserialize(msg);
        if (VelocityConfig.TAKE_CHATLOGS.get(Boolean.class)) {
            LogUtils.addLine(
                    instance.getKey(PlayerCache.getCouples(), player).getUsername(),
                    LegacyComponentSerializer.legacySection().serialize(component)
            );
        }

        player.sendMessage(component);
        instance.getKey(PlayerCache.getCouples(), player).sendMessage(component);
        instance.getServer().getAllPlayers().stream()
                .filter(p -> PlayerCache.getSpectators().contains(p.getUniqueId()))
                .forEach(p -> p.sendMessage(component));
    }

    private void sendAdminPAPIProxyBridge(Player player, String message, String userPrefix, String userSuffix) {
        CompletableFuture<String> stateFuture = VelocityMessages.CONTROL_CHAT_STAFF.color(player);
        CompletableFuture<String> formatFuture = VelocityMessages.CONTROL_CHAT_FORMAT.color(player);

        CompletableFuture.allOf(stateFuture, formatFuture).thenAccept(__ -> {
            String state = stateFuture.join();
            String formatString = formatFuture.join();

            String finalString = formatString
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", player.getUsername())
                    .replace("%message%", message)
                    .replace("%userprefix%", userPrefix)
                    .replace("%usersuffix%", userSuffix)
                    .replace("%state%", state);

            TextComponent component = LegacyComponentSerializer.legacy('§').deserialize(finalString);
            if (VelocityConfig.TAKE_CHATLOGS.get(Boolean.class)) {
                LogUtils.addLine(
                        player.getUsername(),
                        LegacyComponentSerializer.legacySection().serialize(component)
                );
            }

            instance.getValue(PlayerCache.getCouples(), player).sendMessage(component);
            player.sendMessage(component);
            instance.getServer().getAllPlayers().stream()
                    .filter(players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                    .forEach(players -> players.sendMessage(component));
        });
    }

    private void sendSuspectPAPIProxyBridge(Player player, String message, String userPrefix, String userSuffix) {
        CompletableFuture<String> stateFuture = VelocityMessages.CONTROL_CHAT_SUS.color(player);
        CompletableFuture<String> formatFuture = VelocityMessages.CONTROL_CHAT_FORMAT.color(player);

        CompletableFuture.allOf(stateFuture, formatFuture).thenAccept(__ -> {
            String state = stateFuture.join();
            String formatString = formatFuture.join();

            String finalString = formatString
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", player.getUsername())
                    .replace("%message%", message)
                    .replace("%userprefix%", userPrefix)
                    .replace("%usersuffix%", userSuffix)
                    .replace("%state%", state);

            TextComponent component = LegacyComponentSerializer.legacy('§').deserialize(finalString);
            if (VelocityConfig.TAKE_CHATLOGS.get(Boolean.class)) {
                LogUtils.addLine(
                        instance.getKey(PlayerCache.getCouples(), player).getUsername(),
                        LegacyComponentSerializer.legacySection().serialize(component)
                );
            }

            instance.getKey(PlayerCache.getCouples(), player).sendMessage(component);
            player.sendMessage(component);
            instance.getServer().getAllPlayers().stream()
                    .filter(players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                    .forEach(players -> players.sendMessage(component));
        });
    }

    private void prepareSending(Player player, String message, String userPrefix, String userSuffix, boolean staff) {
        String formatString = VelocityMessages.CONTROL_CHAT_FORMAT.color();
        String state = staff ? VelocityMessages.CONTROL_CHAT_STAFF.color() : VelocityMessages.CONTROL_CHAT_SUS.color();
        String finalMessage = formatString
                .replace("%prefix%", VelocityMessages.PREFIX.color())
                .replace("%player%", player.getUsername())
                .replace("%message%", message)
                .replace("%userprefix%", userPrefix)
                .replace("%usersuffix%", userSuffix)
                .replace("%state%", state);
        MessageUtil.sendChatPAPIMessage(player, finalMessage, staff);
    }

    private void sendMessage(Player player, String message, String userPrefix, String userSuffix, boolean staff) {
        String formatString = VelocityMessages.CONTROL_CHAT_FORMAT.color();
        String state = staff ? VelocityMessages.CONTROL_CHAT_STAFF.color() : VelocityMessages.CONTROL_CHAT_SUS.color();
        String finalMessage = formatString
                .replace("%prefix%", VelocityMessages.PREFIX.color())
                .replace("%player%", player.getUsername())
                .replace("%message%", message)
                .replace("%userprefix%", userPrefix)
                .replace("%usersuffix%", userSuffix)
                .replace("%state%", state);

        TextComponent component = LegacyComponentSerializer.legacy('§').deserialize(finalMessage);
        if (staff) {
            if (VelocityConfig.TAKE_CHATLOGS.get(Boolean.class)) {
                LogUtils.addLine(
                        player.getUsername(),
                        LegacyComponentSerializer.legacySection().serialize(component)
                );
            }

            instance.getValue(PlayerCache.getCouples(), player).sendMessage(component);
            player.sendMessage(component);
            instance.getServer().getAllPlayers().stream()
                    .filter(players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                    .forEach(players -> players.sendMessage(component));
            return;
        }

        if (VelocityConfig.TAKE_CHATLOGS.get(Boolean.class)) {
            LogUtils.addLine(
                    instance.getKey(PlayerCache.getCouples(), player).getUsername(),
                    LegacyComponentSerializer.legacySection().serialize(component)
            );
        }

        instance.getKey(PlayerCache.getCouples(), player).sendMessage(component);
        player.sendMessage(component);
        instance.getServer().getAllPlayers().stream()
                .filter(players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                .forEach(players -> players.sendMessage(component));
    }
}
