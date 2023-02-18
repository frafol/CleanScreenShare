package it.frafol.cleanss.velocity.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import org.jetbrains.annotations.NotNull;

public class ServerListener {

    public CleanSS instance;

    public ServerListener(CleanSS instance) {
        this.instance = instance;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onServerPostConnect(final @NotNull ServerPostConnectEvent event) {

        final Player player = event.getPlayer();

        if (!player.getCurrentServer().isPresent()) {
            return;
        }

        if (!player.getCurrentServer().get().getServerInfo().getName().equals(VelocityConfig.CONTROL.get(String.class))) {
            return;
        }

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {

            final ByteArrayDataOutput buf = ByteStreams.newDataOutput();

            buf.writeUTF("SUSPECT");
            buf.writeUTF(player.getUsername());
            player.getCurrentServer().ifPresent(sv ->
                    sv.sendPluginMessage(CleanSS.channel_join, buf.toByteArray()));

        }

        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {

            final ByteArrayDataOutput buf = ByteStreams.newDataOutput();

            buf.writeUTF("ADMIN");
            buf.writeUTF(player.getUsername());

            player.getCurrentServer().ifPresent(sv ->
                    sv.sendPluginMessage(CleanSS.channel_join, buf.toByteArray()));

        }

        if (player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()) {

            final ByteArrayDataOutput buf = ByteStreams.newDataOutput();

            buf.writeUTF("NO_CHAT");
            buf.writeUTF(player.getUsername());

            player.getCurrentServer().ifPresent(sv ->
                    sv.sendPluginMessage(CleanSS.channel_join, buf.toByteArray()));

        }
    }
}
