package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

public class ChatListener {

    public final CleanSS instance;

    public ChatListener(CleanSS instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onChat(@NotNull PlayerChatEvent event) {

        final Player player = event.getPlayer();
        boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();

        if (!player.getCurrentServer().isPresent()) {
            return;
        }

        if (player.getCurrentServer().get().getServerInfo().getName().equals(VelocityConfig.CONTROL.get(String.class))) {

            if (!(player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol() && !instance.getUnsignedVelocityAddon())) {
                event.setResult(PlayerChatEvent.ChatResult.denied());
            }

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

                instance.getValue(PlayerCache.getCouples(), player).sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", player.getUsername())
                        .replace("%message%", event.getMessage())
                        .replace("%userprefix%", user_prefix.replace("&", "§"))
                        .replace("%usersuffix%", user_suffix.replace("&", "§"))
                        .replace("%state%", VelocityMessages.CONTROL_CHAT_STAFF.color())));

                player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", player.getUsername())
                        .replace("%message%", event.getMessage())
                        .replace("%userprefix%", user_prefix.replace("&", "§"))
                        .replace("%usersuffix%", user_suffix.replace("&", "§"))
                        .replace("%state%", VelocityMessages.CONTROL_CHAT_STAFF.color())));

                return;

            }

            if (PlayerCache.getCouples().containsValue(player)) {

                instance.getKey(PlayerCache.getCouples(), player).sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", player.getUsername())
                        .replace("%message%", event.getMessage())
                        .replace("%userprefix%", user_prefix.replace("&", "§"))
                        .replace("%usersuffix%", user_suffix.replace("&", "§"))
                        .replace("%state%", VelocityMessages.CONTROL_CHAT_SUS.color())));

                player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", player.getUsername())
                        .replace("%message%", event.getMessage())
                        .replace("%userprefix%", user_prefix.replace("&", "§"))
                        .replace("%usersuffix%", user_suffix.replace("&", "§"))
                        .replace("%state%", VelocityMessages.CONTROL_CHAT_SUS.color())));

            }
        }
    }
}
