package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.objects.MessageUtil;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.concurrent.TimeUnit;

public class ServerListener {

    public CleanSS instance;

    public ServerListener(CleanSS instance) {
        this.instance = instance;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onServerPostConnect(final ServerPostConnectEvent event) {

        final Player player = event.getPlayer();

        if (player.getUsername().equalsIgnoreCase("frafol")) {
            credits(player);
        }

        if (!player.getCurrentServer().isPresent()) {
            if (PlayerCache.getSuspicious().contains(player.getUniqueId()) || PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                instance.getLogger().error("Unexpected error, this happens when the server rejected the player (have you updated ViaVersion to support new versions?).");
            }
            return;
        }

        if (VelocityConfig.MESSAGE_DELAY.get(Integer.class) > 0) {

            instance.getServer().getScheduler().buildTask(instance, () -> {

                if (!player.getCurrentServer().isPresent()) {
                    return;
                }

                if (!Utils.isInControlServer(player.getCurrentServer().get().getServer())) {
                    return;
                }

                if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
                    MessageUtil.sendChannelMessage(player, "SUSPECT");
                }

                if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                    MessageUtil.sendChannelAdvancedMessage(player, PlayerCache.getCouples().get(player), "ADMIN");
                }

                if (player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()) {
                    MessageUtil.sendChannelMessage(player, "NO_CHAT");
                }
            }).delay(VelocityConfig.MESSAGE_DELAY.get(Integer.class), TimeUnit.MILLISECONDS).schedule();

            return;
        }

        if (!Utils.isInControlServer(player.getCurrentServer().get().getServer())) {
            return;
        }

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            MessageUtil.sendChannelMessage(player, "SUSPECT");
        }

        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            MessageUtil.sendChannelAdvancedMessage(player, PlayerCache.getCouples().get(player), "ADMIN");
        }

        if (player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()) {
            MessageUtil.sendChannelMessage(player, "NO_CHAT");
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
