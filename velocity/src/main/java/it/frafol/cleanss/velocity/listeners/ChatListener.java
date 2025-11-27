package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

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

        if (player.getCurrentServer().isEmpty()) {
            return;
        }

        if (!Utils.isInControlServer(player.getCurrentServer().get().getServer())) {
            return;
        }

        if (PlayerCache.getSpectators().contains(player.getUniqueId()) && !VelocityConfig.CHAT_DISABLED.get(Boolean.class)) {
            return;
        }

        if (PlayerCache.getSpectators().contains(player.getUniqueId())) {
            player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CHAT_DISABLED.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));

            if (!(player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol() && !instance.getUnsignedVelocityAddon())) {
                event.setResult(PlayerChatEvent.ChatResult.denied());
            }

            return;
        }

        if (!(player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol() && !instance.getUnsignedVelocityAddon())) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
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

            String prefix = VelocityMessages.PREFIX.color();
            String userPrefix = ChatUtil.color(user_prefix);
            String userSuffix = ChatUtil.color(user_suffix);

            CompletableFuture<String> stateFuture = VelocityMessages.CONTROL_CHAT_STAFF.color(player);
            CompletableFuture<String> formatFuture = VelocityMessages.CONTROL_CHAT_FORMAT.color(player);
            CompletableFuture.allOf(stateFuture, formatFuture).thenAccept(__ -> {
                String state = stateFuture.join();
                String formatString = formatFuture.join();
                String finalString = formatString
                        .replace("%prefix%", prefix)
                        .replace("%player%", player.getUsername())
                        .replace("%message%", event.getMessage())
                        .replace("%userprefix%", userPrefix)
                        .replace("%usersuffix%", userSuffix)
                        .replace("%state%", state);
                TextComponent messageComponent = LegacyComponentSerializer.legacy('§')
                        .deserialize(finalString);

                if (VelocityConfig.TAKE_CHATLOGS.get(Boolean.class)) {
                    LogUtils.addLine(
                            player.getUsername(),
                            LegacyComponentSerializer.legacySection().serialize(messageComponent));
                }

                instance.getValue(PlayerCache.getCouples(), player).sendMessage(messageComponent);
                player.sendMessage(messageComponent);
                instance.getServer().getAllPlayers().stream().filter
                                (players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                        .forEach(players -> players.sendMessage(messageComponent));
            });
        }

        if (PlayerCache.getCouples().containsValue(player)) {

            String prefix = VelocityMessages.PREFIX.color();
            String userPrefix = ChatUtil.color(user_prefix);
            String userSuffix = ChatUtil.color(user_suffix);

            CompletableFuture<String> stateFuture = VelocityMessages.CONTROL_CHAT_SUS.color(player);
            CompletableFuture<String> formatFuture = VelocityMessages.CONTROL_CHAT_FORMAT.color(player);
            CompletableFuture.allOf(stateFuture, formatFuture).thenAccept(__ -> {
                String state = stateFuture.join();
                String formatString = formatFuture.join();
                String finalString = formatString
                        .replace("%prefix%", prefix)
                        .replace("%player%", player.getUsername())
                        .replace("%message%", event.getMessage())
                        .replace("%userprefix%", userPrefix)
                        .replace("%usersuffix%", userSuffix)
                        .replace("%state%", state);
                TextComponent messageComponent = LegacyComponentSerializer.legacy('§')
                        .deserialize(finalString);

                if (VelocityConfig.TAKE_CHATLOGS.get(Boolean.class)) {
                    LogUtils.addLine(
                            instance.getKey(PlayerCache.getCouples(), player).getUsername(),
                            LegacyComponentSerializer.legacySection().serialize(messageComponent));
                }

                instance.getKey(PlayerCache.getCouples(), player).sendMessage(messageComponent);
                player.sendMessage(messageComponent);
                instance.getServer().getAllPlayers().stream().filter
                                (players -> PlayerCache.getSpectators().contains(players.getUniqueId()))
                        .forEach(players -> players.sendMessage(messageComponent));
            });
        }
    }
}
