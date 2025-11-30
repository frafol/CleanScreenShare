package it.frafol.cleanss.bukkit.listeners;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotCache;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.objects.Placeholder;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import it.frafol.cleanss.bukkit.objects.TextFile;
import it.frafol.cleanss.bukkit.objects.utils.NametagUtil;
import it.frafol.cleanss.bukkit.objects.utils.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PluginMessageReceiver implements PluginMessageListener {

    private final CleanSS instance = CleanSS.getInstance();

    @SuppressWarnings({"UnstableApiUsage"})
    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        if (!(channel.equals("cleanss:join"))) return;
        ByteArrayDataInput dataInput = ByteStreams.newDataInput(message);
        String subChannel = dataInput.readUTF();
        switch (subChannel) {
            case "NO_CHAT" -> {
                String player_found = dataInput.readUTF();
                final Player final_player = instance.getServer().getPlayer(player_found);
                if (final_player == null) return;
                PlayerCache.getNo_chat().add(final_player.getUniqueId());
            }
            case "CHAT" -> {
                String player_found = dataInput.readUTF();
                final Player final_player = instance.getServer().getPlayer(player_found);
                if (final_player == null) return;
                Boolean staff = Boolean.valueOf(dataInput.readUTF());
                String chatMessage = dataInput.readUTF();
                String finalChatMessage = Placeholder.color(chatMessage, final_player);
                sendResponsePluginMessage(final_player, player_found, staff, finalChatMessage);
            }
            case "DISCONNECT_NOW" -> {
                String player_found = dataInput.readUTF();
                final Player final_player = instance.getServer().getPlayer(player_found);
                if (final_player == null) return;
                if (!final_player.isOnline()) return;
                final_player.kickPlayer(null);
            }
            case "RELOAD" -> {
                instance.getLogger().warning("CleanScreenShare is reloading on your proxy, running a global reload on this server.");
                player.sendMessage(SpigotConfig.RELOADED.color());
                TextFile.reloadAll();
            }
            case "FINISH" -> {
                String administrator = dataInput.readUTF();
                String suspect = dataInput.readUTF();
                handleFinish(administrator);
                handleFinish(suspect);
            }
            case "SUSPECT" -> {
                String player_found = dataInput.readUTF();
                instance.getLogger().warning("Received data (suspect) from the proxy. [" + player_found + "]");
                final Player final_player = instance.getServer().getPlayer(player_found);
                if (final_player == null) {
                    instance.getLogger().severe("The player " + player_found + " (suspect) is not in the server. Is your server configured correctly?");
                    return;
                }

                if (SpigotCache.SUSPECT_SPAWN.get(String.class) == null || SpigotCache.SUSPECT_SPAWN.get(String.class).equals("world;0.0;0.0;0.0;0.0;0.0")) {
                    instance.getLogger().severe("Suspect spawn is not set correctly, this may cause issues during the control check.");
                }

                if (SpigotConfig.SPAWN.get(Boolean.class)) {
                    UniversalScheduler.getScheduler(instance).runTaskLater(() ->
                                    final_player.teleport(PlayerCache.StringToLocation(SpigotCache.SUSPECT_SPAWN.get(String.class))),
                            5L);
                }

                PlayerCache.getSuspicious().add(final_player.getUniqueId());
                instance.startTimer(final_player.getUniqueId());
                if (SpigotConfig.SB_SUSPECT.get(Boolean.class)) PlayerCache.createSuspectScoreboard(final_player);
                if (SpigotConfig.JOIN_SOUNDS.get(Boolean.class)) SoundUtil.playSound(final_player, SpigotConfig.SOUND_SUSPECT.get(String.class));
                if (SpigotConfig.TABLIST_SUSPECT.get(Boolean.class)) UniversalScheduler.getScheduler(instance).runTaskLater(() -> PlayerCache.setSuspectTabList(player), 10);
            }
            case "ADMIN" -> {
                String player_found = dataInput.readUTF();
                String suspicious_found = dataInput.readUTF();
                instance.getLogger().warning("Received data (staff) from the proxy. [" + player_found + "]");
                final Player final_player = instance.getServer().getPlayer(player_found);

                if (final_player == null) {
                    instance.getLogger().severe("The player " + player_found + " (staff) is not in the server. Is your server configured correctly?");
                    return;
                }

                if (SpigotCache.ADMIN_SPAWN.get(String.class) == null || SpigotCache.ADMIN_SPAWN.get(String.class).equals("world;0.0;0.0;0.0;0.0;0.0")) {
                    instance.getLogger().severe("Administrator spawn is not set correctly, this may cause issues during the control check.");
                }

                if (SpigotConfig.SPAWN.get(Boolean.class)) {
                    UniversalScheduler.getScheduler(instance).runTaskLater(() ->
                                    final_player.teleport(PlayerCache.StringToLocation(SpigotCache.ADMIN_SPAWN.get(String.class))),
                            5L);
                }

                PlayerCache.getAdministrator().add(final_player.getUniqueId());
                instance.startTimer(final_player.getUniqueId());
                if (SpigotConfig.SB_STAFF.get(Boolean.class)) PlayerCache.createAdminScoreboard(final_player);
                if (SpigotConfig.JOIN_SOUNDS.get(Boolean.class)) SoundUtil.playSound(final_player, SpigotConfig.SOUND_STAFF.get(String.class));
                if (SpigotConfig.TABLIST_STAFF.get(Boolean.class)) UniversalScheduler.getScheduler(instance).runTaskLater(() -> PlayerCache.setStaffTabList(player), 10L);
                taskCouple(suspicious_found, final_player);
            }
        }
    }

    private void taskCouple(String suspicious_found, Player final_player) {
        UniversalScheduler.getScheduler(instance).runTaskLater(() -> {
            if (instance.getServer().getPlayer(suspicious_found) == null) return;
            Player final_suspicious = instance.getServer().getPlayer(suspicious_found);
            if (final_suspicious == null) {
                taskCouple(suspicious_found, final_player);
                return;
            }
            final_suspicious = final_suspicious.getPlayer();
            if (final_suspicious != null) PlayerCache.getCouples().put(final_player.getUniqueId(), final_suspicious.getUniqueId());
            instance.getLogger().warning("Processed data for " + suspicious_found + " (suspect) and " + final_player.getName() + " (staff).");
        }, 10L);
    }

    private void handleFinish(String username) {
        Player player = instance.getServer().getPlayer(username);
        if (player == null) return;
        instance.getLogger().warning("Finish command received: Removing " + username + " from the database.");
        NametagUtil.removeTag(player);
        PlayerCache.getNo_chat().remove(player.getUniqueId());
        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) PlayerCache.deleteSuspectScoreboard(player);
        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) PlayerCache.deleteAdminScoreboard(player);
        PlayerCache.deleteOtherScoreboard(player);
        PlayerCache.getSuspicious().remove(player.getUniqueId());
        PlayerCache.getAdministrator().remove(player.getUniqueId());
        instance.stopTimer(player.getUniqueId());
        if (PlayerCache.getCouples().get(player.getUniqueId()) != null) PlayerCache.getCouples().remove(player.getUniqueId());
    }

    private void sendResponsePluginMessage(Player player, String playerName, Boolean staff, String message) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(out);
            data.writeUTF("CHAT");
            data.writeUTF(playerName);
            data.writeUTF(String.valueOf(staff));
            data.writeUTF(message);
            player.sendPluginMessage(instance, "cleanss:chat", out.toByteArray());
        } catch (Exception ignored) {}
    }
}
