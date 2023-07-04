package it.frafol.cleanss.velocity.handlers;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
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

        if (chat.startsWith("/controlfinish") || chat.startsWith("/ssfinish") || chat.startsWith("/cleanssfinish")) {
            limboFinishCommand(player, chat);
            return;
        }

        if (chat.startsWith("/")) {
            player.getProxyPlayer().sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_IN_LIMBO.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();

        if (player.getProxyPlayer().getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol() && !instance.getUnsignedVelocityAddon()) {
            return;
        }

        String user_prefix = "";
        String user_suffix = "";

        if (luckperms) {

            final LuckPerms api = LuckPermsProvider.get();

            final User user = api.getUserManager().getUser(player.getProxyPlayer().getUniqueId());

            if (user == null) {
                return;
            }

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

    private void limboFinishCommand(LimboPlayer limboPlayer, String message) {

        final Player source = limboPlayer.getProxyPlayer();
        String[] words = message.split(" ");

        String secondWord;
        if (words.length == 2) {
            secondWord = words[1];
        } else {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.USAGE.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        boolean luckperms = instance.getServer().getPluginManager().isLoaded("luckperms");

        if (Utils.isConsole(source)) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ONLY_PLAYERS.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (!source.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class))) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (instance.getServer().getAllPlayers().toString().contains(secondWord)) {

            final Optional<Player> player = instance.getServer().getPlayer(secondWord);
            final Optional<RegisteredServer> proxyServer = instance.getServer().getServer(VelocityConfig.CONTROL_FALLBACK.get(String.class));

            if (!player.isPresent()) {
                source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.get(String.class)
                        .replace("%prefix%", VelocityMessages.PREFIX.color())));
                return;
            }

            if (!PlayerCache.getSuspicious().contains(player.get().getUniqueId())) {
                source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_CONTROL.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
                return;
            }

            if (instance.getValue(PlayerCache.getCouples(), source) == null || instance.getValue(PlayerCache.getCouples(), source) != player.get()) {
                source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_CONTROL.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
                return;
            }

            if (!proxyServer.isPresent()) {
                return;
            }

            System.out.println("Limbo finish");
            Utils.finishControl(player.get(), source, proxyServer.get());

            String admin_group = "";
            String suspect_group = "";

            if (luckperms) {

                final LuckPerms api = LuckPermsProvider.get();

                final User admin = api.getUserManager().getUser(source.getUniqueId());
                final User suspect = api.getUserManager().getUser(player.get().getUniqueId());

                if (admin == null || suspect == null) {
                    return;
                }

                final String admingroup = admin.getCachedData().getMetaData().getPrimaryGroup();
                admin_group = admingroup == null ? "" : admingroup;

                final String suspectgroup = suspect.getCachedData().getMetaData().getPrimaryGroup();
                suspect_group = suspectgroup == null ? "" : suspectgroup;

            }

            Utils.sendDiscordMessage(player.get(), source, VelocityMessages.DISCORD_FINISHED.get(String.class).replace("%suspectgroup%", suspect_group).replace("%admingroup%", admin_group), VelocityMessages.CLEAN.get(String.class));

        } else {

            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", secondWord)));

        }
    }
}

