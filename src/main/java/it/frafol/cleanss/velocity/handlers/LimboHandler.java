package it.frafol.cleanss.velocity.handlers;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityCommandsConfig;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.MessageUtil;
import it.frafol.cleanss.velocity.objects.Placeholder;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class LimboHandler implements LimboSessionHandler {

    public static final HashMap<Player, LimboPlayer> limbo_players = new HashMap<>();
    public final Player proxyPlayer;

    LimboPlayer player;

    private final CleanSS instance;

    public LimboHandler(Player proxyPlayer, CleanSS plugin) {
        this.proxyPlayer = proxyPlayer;
        this.instance = plugin;
    }

    @Override
    public void onSpawn(Limbo server, LimboPlayer limboPlayer) {
        player = limboPlayer;
        limbo_players.put(proxyPlayer, limboPlayer);
        limboPlayer.disableFalling();
    }

    @Override
    public void onDisconnect() {
        limbo_players.remove(proxyPlayer);
    }

    @Override
    public void onChat(String chat) {

        if (player == null) {
            return;
        }

        if (chat.startsWith("/")) {
            instance.getServer().getCommandManager().executeImmediatelyAsync(proxyPlayer, chat.substring(1));
            return;
        }

        boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();
        String user_prefix = "";
        String user_suffix = "";

        if (luckperms) {
            final LuckPerms api = LuckPermsProvider.get();
            final User user = api.getUserManager().getUser(player.getProxyPlayer().getUniqueId());
            if (user == null) return;
            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            user_prefix = prefix == null ? "" : prefix;
            user_suffix = suffix == null ? "" : suffix;
        }

        if (PlayerCache.getCouples().containsKey(player.getProxyPlayer())) {
            instance.getValue(PlayerCache.getCouples(), player.getProxyPlayer()).sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", player.getProxyPlayer().getUsername())
                    .replace("%message%", chat)
                    .replace("%userprefix%", user_prefix.replace("&", "§"))
                    .replace("%usersuffix%", user_suffix.replace("&", "§"))
                    .replace("%state%", VelocityMessages.CONTROL_CHAT_STAFF.color())));
            player.getProxyPlayer().sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", player.getProxyPlayer().getUsername())
                    .replace("%message%", chat)
                    .replace("%userprefix%", user_prefix.replace("&", "§"))
                    .replace("%usersuffix%", user_suffix.replace("&", "§"))
                    .replace("%state%", VelocityMessages.CONTROL_CHAT_STAFF.color())));
            return;
        }

        if (PlayerCache.getCouples().containsValue(player.getProxyPlayer())) {
            instance.getKey(PlayerCache.getCouples(), player.getProxyPlayer()).sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", player.getProxyPlayer().getUsername())
                    .replace("%message%", chat)
                    .replace("%userprefix%", user_prefix.replace("&", "§"))
                    .replace("%usersuffix%", user_suffix.replace("&", "§"))
                    .replace("%state%", VelocityMessages.CONTROL_CHAT_SUS.color())));
            player.getProxyPlayer().sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", player.getProxyPlayer().getUsername())
                    .replace("%message%", chat)
                    .replace("%userprefix%", user_prefix.replace("&", "§"))
                    .replace("%usersuffix%", user_suffix.replace("&", "§"))
                    .replace("%state%", VelocityMessages.CONTROL_CHAT_SUS.color())));
        }
    }
}

