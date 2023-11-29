package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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

            if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
                Utils.sendChannelMessage(player, "SUSPECT");
            }

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                Utils.sendChannelAdvancedMessage(player, PlayerCache.getCouples().get(player), "ADMIN");
            }

        }, BungeeConfig.MESSAGE_DELAY.get(Integer.class), TimeUnit.SECONDS);
    }

    private void credits(ProxiedPlayer player) {
        player.sendMessage(TextComponent.fromLegacyText("§d| "));
        player.sendMessage(TextComponent.fromLegacyText("§d| §7CleanScreenShare Informations"));
        player.sendMessage(TextComponent.fromLegacyText("§d| "));
        player.sendMessage(TextComponent.fromLegacyText("§d| §7Version: §d" + instance.getDescription().getVersion()));
        player.sendMessage(TextComponent.fromLegacyText("§d| §7BungeeCord: §d" + instance.getProxy().getVersion()));
        player.sendMessage(TextComponent.fromLegacyText("§d| "));
    }
}
