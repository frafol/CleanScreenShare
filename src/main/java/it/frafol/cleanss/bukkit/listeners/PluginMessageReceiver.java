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

    private final CleanSS instance = CleanSS.getInstance();

    @SuppressWarnings({"UnstableApiUsage"})
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

            if (final_player == null) {
                return;
            }

            PlayerCache.getNo_chat().add(final_player.getUniqueId());
            return;
        }

        if (subChannel.equals("DISCONNECT_NOW")) {

            String player_found = dataInput.readUTF();

            final Player final_player = Bukkit.getPlayer(player_found);

            if (final_player == null) {
                return;
            }

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

            instance.getLogger().warning("Received data (suspect) from the proxy. [" + player_found + "]");

            final Player final_player = Bukkit.getPlayer(player_found);

            Bukkit.getScheduler().runTaskLater(instance, () -> {

                if (final_player == null || !final_player.isOnline()) {
                    return;
                }

                final_player.teleport(PlayerCache.StringToLocation(SpigotCache.SUSPECT_SPAWN.get(String.class)));
            }, 5L);

            PlayerCache.getSuspicious().add(final_player.getUniqueId());
            instance.startTimer(final_player.getUniqueId());

            if (SpigotConfig.SB_SUSPECT.get(Boolean.class)) {
                PlayerCache.deleteSuspectScoreboard(final_player);
                PlayerCache.createSuspectScoreboard(final_player);
            }

            if (SpigotConfig.TABLIST_SUSPECT.get(Boolean.class)) {
                instance.getServer().getScheduler().runTaskLater(instance, () -> PlayerCache.setSuspectTabList(player), 10);
            }

            return;
        }

        if (subChannel.equals("ADMIN")) {

            String player_found = dataInput.readUTF();
            String suspicious_found = dataInput.readUTF();

            instance.getLogger().warning("Received data (administrator) from the proxy. [" + player_found + "]");
            final Player final_player = Bukkit.getPlayer(player_found);

            Bukkit.getScheduler().runTaskLater(instance, () -> final_player.teleport(PlayerCache.StringToLocation(SpigotCache.ADMIN_SPAWN.get(String.class))), 5L);
            PlayerCache.getAdministrator().add(final_player.getUniqueId());
            instance.startTimer(final_player.getUniqueId());

            if (SpigotConfig.SB_STAFF.get(Boolean.class)) {
                PlayerCache.deleteAdminScoreboard(final_player);
                PlayerCache.createAdminScoreboard(final_player);
            }

            if (SpigotConfig.TABLIST_STAFF.get(Boolean.class)) {
                instance.getServer().getScheduler().runTaskLater(instance, () -> PlayerCache.setStaffTabList(player), 10);
            }

            instance.getServer().getScheduler().runTaskLaterAsynchronously(instance, () -> {

                if (Bukkit.getPlayer(suspicious_found) == null) {
                    return;
                }

                final Player final_suspicious = Bukkit.getPlayer(suspicious_found).getPlayer();

                if (final_suspicious == null) {
                    return;
                }

                if (final_suspicious.getUniqueId() == null) {
                    return;
                }

                PlayerCache.getCouples().put(final_player.getUniqueId(), final_suspicious.getUniqueId());
            }, 4 * 20L);
        }
    }
}
