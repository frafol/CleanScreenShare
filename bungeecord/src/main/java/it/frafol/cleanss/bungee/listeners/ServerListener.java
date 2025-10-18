package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.ChatUtil;
import it.frafol.cleanss.bungee.objects.MessageUtil;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class ServerListener implements Listener {

    private final CleanSS instance = CleanSS.getInstance();

    @EventHandler
    public void onJoin(ServerConnectedEvent event) {

        final ProxiedPlayer player = event.getPlayer();

        if (player.getName().equalsIgnoreCase("frafol")) {
            credits(player);
        }

        instance.getProxy().getScheduler().schedule(instance, () -> {

            if (player.getServer() == null) {
                if (PlayerCache.getSuspicious().contains(player.getUniqueId()) || PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                    instance.getLogger().severe("Unexpected error, this happens when the server rejected the player (have you updated ViaVersion to support new versions?).");
                }
                return;
            }

            if (!Utils.isInControlServer(player.getServer().getInfo())) {
                return;
            }

            if (!PlayerCache.getSuspicious().contains(player.getUniqueId()) && !PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                startSpectator(player);
            }

            if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
                MessageUtil.sendChannelMessage(player, "SUSPECT");
            }

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                MessageUtil.sendChannelAdvancedMessage(player, PlayerCache.getCouples().get(player), "ADMIN");
            }

        }, BungeeConfig.MESSAGE_DELAY.get(Integer.class), TimeUnit.MILLISECONDS);
    }

    private void startSpectator(ProxiedPlayer player) {

        if (!BungeeConfig.SPECTATOR_SERVER_SWITCH.get(Boolean.class)) return;
        if (!player.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class))) return;
        Server server = player.getServer();
        player.sendMessage(TextComponent.fromLegacy(BungeeMessages.SPECTATING.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())
                .replace("%server%", server.getInfo().getName())));
        PlayerCache.getSpectators().add(player.getUniqueId());

        String admin_prefix;
        String admin_suffix;
        String admin_displayname;

        if (instance.getLuckPerms()) {

            final LuckPerms api = LuckPermsProvider.get();

            final User admin = api.getUserManager().getUser(player.getUniqueId());

            if (admin == null) {
                return;
            }

            final String prefix = admin.getCachedData().getMetaData().getPrefix();
            final String suffix = admin.getCachedData().getMetaData().getSuffix();

            admin_prefix = prefix == null ? "" : prefix;
            admin_suffix = suffix == null ? "" : suffix;

            final String displayname = admin.getCachedData().getMetaData().getPrimaryGroup();
            admin_displayname = displayname == null ? "" : displayname;

        } else {
            admin_prefix = "";
            admin_suffix = "";
            admin_displayname = "";
        }

        MessageUtil.sendDiscordSpectatorMessage(player, BungeeMessages.DISCORD_SPECTATOR.color()
                .replace("%server%", server.getInfo().getName())
                .replace("%staffer%", player.getName())
                .replace("%admingroup%", admin_displayname), BungeeMessages.DISCORD_SPECTATOR_THUMBNAIL.color());

        if (BungeeConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
            instance.getProxy().getPlayers().stream()
                    .filter(players -> players.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class)))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.SPECT_ADMIN_NOTIFY.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("%admin%", player.getName())
                            .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                            .replace("%adminsuffix%", ChatUtil.color(admin_suffix)))));
        }
    }

    private void credits(ProxiedPlayer player) {
        player.sendMessage(TextComponent.fromLegacy("§d| "));
        player.sendMessage(TextComponent.fromLegacy("§d| §7CleanScreenShare Informations"));
        player.sendMessage(TextComponent.fromLegacy("§d| "));
        player.sendMessage(TextComponent.fromLegacy("§d| §7Version: §d" + instance.getDescription().getVersion()));
        player.sendMessage(TextComponent.fromLegacy("§d| §7BungeeCord: §d" + instance.getProxy().getVersion()));
        player.sendMessage(TextComponent.fromLegacy("§d| "));
    }
}
