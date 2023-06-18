package it.frafol.cleanss.bukkit.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotCache;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import it.frafol.cleanss.bukkit.objects.TextFile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class PluginMessageReceiver implements PluginMessageListener {

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onPluginMessageReceived(@NotNull String channel, Player player, byte[] message) {

        if (!(channel.equals("cleanss:join"))) {
            return;
        }

        ByteArrayDataInput dataInput = ByteStreams.newDataInput(message);
        String subChannel = dataInput.readUTF();

        if (subChannel.equals("NO_CHAT")) {

            String player_found = dataInput.readUTF();

            final Player final_player = Bukkit.getPlayer(player_found);
            PlayerCache.getNo_chat().add(final_player.getUniqueId());

            return;

        }

        if (subChannel.equals("DISCONNECT_NOW")) {

            String player_found = dataInput.readUTF();

            final Player final_player = Bukkit.getPlayer(player_found);

            if (!final_player.isOnline()) {
                return;
            }

            final_player.kickPlayer(null);
            return;

        }

        if (subChannel.equals("RELOAD")) {
            CleanSS.getInstance().getLogger().warning("CleanScreenShare is reloading on your proxy, " +
                    "running a global reload on this server.");
            TextFile.reloadAll();
        }

        if (!SpigotConfig.SPAWN.get(Boolean.class)) {
            return;
        }

        if (subChannel.equals("SUSPECT")) {

            String player_found = dataInput.readUTF();

            final Player final_player = Bukkit.getPlayer(player_found);
            Bukkit.getScheduler().runTaskLater(CleanSS.getInstance(), () -> final_player.teleport(PlayerCache.StringToLocation(SpigotCache.SUSPECT_SPAWN.get(String.class))), 5L);
            return;

        }

        if (subChannel.equals("ADMIN")) {

            String player_found = dataInput.readUTF();

            final Player final_player = Bukkit.getPlayer(player_found);
            Bukkit.getScheduler().runTaskLater(CleanSS.getInstance(), () -> final_player.teleport(PlayerCache.StringToLocation(SpigotCache.ADMIN_SPAWN.get(String.class))), 5L);

        }
    }
}
