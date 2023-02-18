package it.frafol.cleanss.bungee.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ServerListener implements Listener {

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void onJoin(@NotNull ServerConnectedEvent event) {

        final ProxiedPlayer player = event.getPlayer();

        CleanSS.getInstance().getProxy().getScheduler().schedule(CleanSS.getInstance(), () -> {

            if (!player.getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class))) {
                return;
            }

            if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {

                final ByteArrayDataOutput buf = ByteStreams.newDataOutput();

                buf.writeUTF("SUSPECT");
                buf.writeUTF(player.getName());

                player.getServer().sendData("cleanss:join", buf.toByteArray());

            }

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {

                final ByteArrayDataOutput buf = ByteStreams.newDataOutput();

                buf.writeUTF("ADMIN");
                buf.writeUTF(player.getName());

                player.getServer().sendData("cleanss:join", buf.toByteArray());

            }

        }, 1L, TimeUnit.SECONDS);
    }
}
