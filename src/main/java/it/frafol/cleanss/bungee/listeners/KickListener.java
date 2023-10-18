package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KickListener implements Listener {

    public CleanSS instance;

    public KickListener(CleanSS instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PostLoginEvent event) {

        final ProxiedPlayer player = event.getPlayer();

        if (player.hasPermission(BungeeConfig.RELOAD_PERMISSION.get(String.class))) {
                instance.UpdateChecker(player);
        }

        if (instance.getData() != null) {
            instance.getData().setupPlayer(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerChange(@NotNull ServerConnectEvent event) {

        final ProxiedPlayer player = event.getPlayer();
        final ServerInfo server = event.getTarget();
        boolean luckperms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;

        if (PlayerCache.getSpectators().contains(player.getUniqueId())) {

            if (Utils.isInControlServer(server)) {
                return;
            }

            player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_SPECTATING.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            PlayerCache.getSpectators().remove(player.getUniqueId());

            Utils.sendDiscordSpectatorMessage(player, BungeeMessages.DISCORD_SPECTATOR_END.color()
                    .replace("%player%", player.getName()));

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

            if (BungeeConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
                instance.getProxy().getPlayers().stream()
                        .filter(players -> players.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.SPECT_ADMIN_NOTIFY_FINISH.color()
                                .replace("%prefix%", BungeeMessages.PREFIX.color())
                                .replace("%admin%", player.getName())
                                .replace("%adminprefix%", Utils.color(admin_prefix))
                                .replace("%adminsuffix%", Utils.color(admin_suffix)))));
            }
            return;
        }

        if (PlayerCache.getAdministrator().contains(player.getUniqueId()) ||
                PlayerCache.getSuspicious().contains(player.getUniqueId())) {

            if (!Utils.isInControlServer(server)) {
                return;
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(@NotNull PlayerDisconnectEvent event) {

        if (!event.getPlayer().isConnected()) {
            return;
        }

        List<ServerInfo> servers = Utils.getServerList(BungeeConfig.CONTROL_FALLBACK.getStringList());

        if (!BungeeConfig.DISABLE_PING.get(Boolean.class)) {
            servers = Utils.getOnlineServers(servers);
        }

        final ServerInfo proxyServer = Utils.getBestServer(servers);
        final ProxiedPlayer player = event.getPlayer();

        PlayerCache.getSpectators().remove(player.getUniqueId());

        if (proxyServer == null) {
            instance.getLogger().severe("Fallback server was not found in your BungeeCord configuration or is offline, players will not be able to reconnect to the server.");

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                instance.getValue(PlayerCache.getCouples(), player).disconnect(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color()));
            }

            if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
                instance.getKey(PlayerCache.getCouples(), player).disconnect(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color()));
            }
        }

        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            Utils.finishControl(instance.getValue(PlayerCache.getCouples(), player), player, proxyServer);
        }

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            Utils.punishPlayer(instance.getKey(PlayerCache.getCouples(), player).getUniqueId(), player.getName(), instance.getKey(PlayerCache.getCouples(), player), player);
            Utils.finishControl(player, instance.getKey(PlayerCache.getCouples(), player), proxyServer);
        }
    }
}
