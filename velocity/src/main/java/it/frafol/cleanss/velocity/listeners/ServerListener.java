package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.ChatUtil;
import it.frafol.cleanss.velocity.objects.MessageUtil;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.concurrent.TimeUnit;

public class ServerListener {

    public CleanSS instance;

    public ServerListener(CleanSS instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onServerPostConnect(final ServerPostConnectEvent event) {

        final Player player = event.getPlayer();
        if (player.getUsername().equalsIgnoreCase("frafol")) credits(player);
        if (player.getCurrentServer().isEmpty()) {
            if (PlayerCache.getSuspicious().contains(player.getUniqueId()) || PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                instance.getLogger().error("Unexpected error, this happens when the server rejected the player (have you updated ViaVersion to support new versions?).");
            }
            return;
        }

        if (VelocityConfig.MESSAGE_DELAY.get(Integer.class) > 0) {
            instance.getServer().getScheduler().buildTask(instance, () -> {
                if (player.getCurrentServer().isEmpty()) return;
                if (!Utils.isInControlServer(player.getCurrentServer().get().getServer())) return;
                if (!PlayerCache.getSuspicious().contains(player.getUniqueId()) && !PlayerCache.getAdministrator().contains(player.getUniqueId())) startSpectate(player);
                if (PlayerCache.getSuspicious().contains(player.getUniqueId())) MessageUtil.sendChannelMessage(player, "SUSPECT");
                if (PlayerCache.getAdministrator().contains(player.getUniqueId())) MessageUtil.sendChannelAdvancedMessage(player, PlayerCache.getCouples().get(player), "ADMIN");
                if (player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()) MessageUtil.sendChannelMessage(player, "NO_CHAT");
            }).delay(VelocityConfig.MESSAGE_DELAY.get(Integer.class), TimeUnit.MILLISECONDS).schedule();
            return;
        }

        if (!Utils.isInControlServer(player.getCurrentServer().get().getServer())) return;
        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) MessageUtil.sendChannelMessage(player, "SUSPECT");
        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) MessageUtil.sendChannelAdvancedMessage(player, PlayerCache.getCouples().get(player), "ADMIN");
        if (player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()) MessageUtil.sendChannelMessage(player, "NO_CHAT");
    }

    private void startSpectate(Player player) {
        if (!player.getCurrentServer().isPresent()) return;
        if (!VelocityConfig.SPECTATOR_SERVER_SWITCH.get(Boolean.class)) return;
        if (!player.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class))) return;
        ServerInfo server = player.getCurrentServer().get().getServer().getServerInfo();
        player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.SPECTATING.color()
                .replace("%prefix%", VelocityMessages.PREFIX.color())
                .replace("%server%", server.getName())));
        PlayerCache.getSpectators().add(player.getUniqueId());

        String admin_prefix;
        String admin_suffix;
        String admin_displayname;

        if (instance.getLuckPerms()) {
            final LuckPerms api = LuckPermsProvider.get();
            final User admin = api.getUserManager().getUser(player.getUniqueId());
            if (admin == null) return;
            final String prefix = admin.getCachedData().getMetaData().getPrefix();
            final String suffix = admin.getCachedData().getMetaData().getSuffix();
            final String displayname = admin.getCachedData().getMetaData().getPrimaryGroup();
            admin_prefix = prefix == null ? "" : prefix;
            admin_suffix = suffix == null ? "" : suffix;
            admin_displayname = displayname == null ? "" : displayname;
        } else {
            admin_prefix = "";
            admin_suffix = "";
            admin_displayname = "";
        }

        MessageUtil.sendDiscordSpectatorMessage(player, VelocityMessages.DISCORD_SPECTATOR.color()
                        .replace("%staffer%", player.getUsername())
                        .replace("%server%", server.getName())
                        .replace("%admingroup%", admin_displayname),
                VelocityMessages.DISCORD_SPECTATOR_THUMBNAIL.color());

        if (VelocityConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
            instance.getServer().getAllPlayers().stream()
                    .filter(players -> players.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class)))
                    .forEach(players -> players.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.SPECT_ADMIN_NOTIFY.color()
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("%admin%", player.getUsername())
                            .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                            .replace("%adminsuffix%", ChatUtil.color(admin_suffix)))));
        }
    }

    private void credits(Player player) {
        player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
        player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7CleanScreenShare Informations"));
        player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
        if (instance.getContainer().getDescription().getVersion().isPresent()) {
            player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Version: §d" + instance.getContainer().getDescription().getVersion().get()));
        }
        player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Velocity: §d" + instance.getServer().getVersion().getVersion()));
        player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
    }
}
