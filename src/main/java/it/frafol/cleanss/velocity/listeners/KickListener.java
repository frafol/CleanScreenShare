package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
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

import java.util.List;
import java.util.Optional;

public class KickListener {

    public CleanSS instance;

    public KickListener(CleanSS instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPlayerConnect(ServerPreConnectEvent event) {

        final Player player = event.getPlayer();
        final RegisteredServer server = event.getOriginalServer();
        boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();

        if (server == null) {
            return;
        }

        if (PlayerCache.getSpectators().contains(player.getUniqueId())) {

            if (instance.useLimbo) {
                return;
            }

            if (Utils.isInControlServer(server)) {
                return;
            }

            player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_SPECTATING.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            PlayerCache.getSpectators().remove(player.getUniqueId());

            MessageUtil.sendDiscordSpectatorMessage(player,
                    VelocityMessages.DISCORD_SPECTATOR_END.color().replace("%player%", player.getUsername()),
                    VelocityMessages.DISCORD_SPECTATOR_THUMBNAIL.color());

            String admin_prefix;
            String admin_suffix;

            if (luckperms) {

                final LuckPerms api = LuckPermsProvider.get();

                final User admin = api.getUserManager().getUser(player.getUniqueId());

                if (admin == null) {
                    return;
                }

                final String prefix = admin.getCachedData().getMetaData().getPrefix();
                final String suffix = admin.getCachedData().getMetaData().getSuffix();

                admin_prefix = prefix == null ? "" : prefix;
                admin_suffix = suffix == null ? "" : suffix;

            } else {
                admin_prefix = "";
                admin_suffix = "";
            }

            if (VelocityConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
                instance.getServer().getAllPlayers().stream()
                        .filter(players -> players.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.SPECT_ADMIN_NOTIFY_FINISH.color()
                                .replace("%prefix%", VelocityMessages.PREFIX.color())
                                .replace("%admin%", player.getUsername())
                                .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                                .replace("%adminsuffix%", ChatUtil.color(admin_suffix)))));
            }
            return;
        }

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())
                || PlayerCache.getAdministrator().contains(player.getUniqueId())) {

            if (Utils.isInControlServer(server) || instance.useLimbo) {
                return;
            }

            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }

    @Subscribe
    public void onPlayerConnected(PostLoginEvent event) {

        final Player player = event.getPlayer();

        if (player.hasPermission(VelocityConfig.RELOAD_PERMISSION.get(String.class))) {
            instance.UpdateChecker(player);
        }

        if (instance.getData() != null) {
            instance.getData().setupPlayer(player.getUniqueId());
        }

        PlayerCache.getControls().putIfAbsent(player.getUniqueId(), 0);
        PlayerCache.getControls_suffered().putIfAbsent(player.getUniqueId(), 0);
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {

        final Player player = event.getPlayer();

        List<Optional<RegisteredServer>> servers = Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList());

        if (!VelocityConfig.DISABLE_PING.get(Boolean.class)) {
            servers = Utils.getOnlineServers(servers);
        }

        Optional<RegisteredServer> proxyServer = Utils.getBestServer(servers);

        if (PlayerCache.getSpectators().contains(player.getUniqueId())) {
            PlayerCache.getSpectators().remove(player.getUniqueId());
            return;
        }

        if (!proxyServer.isPresent()) {

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                instance.getValue(PlayerCache.getCouples(), player).disconnect(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.FINISHSUS.color()));
                instance.getLogger().error("Fallback server was not found in your Velocity configuration or is offline, " + player.getUsername() + " will not be able to reconnect to the server.");
            }

            if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
                instance.getKey(PlayerCache.getCouples(), player).disconnect(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.FINISHSUS.color()));
                instance.getLogger().error("Fallback server was not found in your Velocity configuration or is offline, " + player.getUsername() + " will not be able to reconnect to the server.");
            }

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, null);

            } else if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {

                if (instance.useLimbo && PlayerCache.getNow_started_sus().contains(player.getUniqueId())) {
                    Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, null);
                    return;
                }

                Utils.punishPlayer(instance.getKey(PlayerCache.getCouples(), player).getUniqueId(), player.getUsername(), instance.getKey(PlayerCache.getCouples(), player), player);
                Utils.finishControl(player, instance.getKey(PlayerCache.getCouples(), player), null);
            }

            return;
        }

        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, proxyServer.get());

        } else if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {

            if (instance.useLimbo && PlayerCache.getNow_started_sus().contains(player.getUniqueId())) {
                Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, proxyServer.get());
                return;
            }

            Utils.punishPlayer(instance.getKey(PlayerCache.getCouples(), player).getUniqueId(), player.getUsername(), instance.getKey(PlayerCache.getCouples(), player), player);
            Utils.finishControl(player, instance.getKey(PlayerCache.getCouples(), player), proxyServer.get());

        }
    }
}
