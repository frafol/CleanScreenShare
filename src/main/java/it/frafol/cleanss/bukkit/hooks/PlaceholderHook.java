package it.frafol.cleanss.bukkit.hooks;

import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class PlaceholderHook extends PlaceholderExpansion {

    public final CleanSS plugin;

    public PlaceholderHook(CleanSS plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "screenshare";
    }

    @Override
    public @NotNull String getAuthor() {
        return "frafol";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String placeholder) {

        if (placeholder.equalsIgnoreCase("seconds")) {
            if (plugin.getSeconds(player.getUniqueId()) != null) {
                return String.valueOf(plugin.getSeconds(player.getUniqueId()));
            } else {
                return "0";
            }
        }

        if (placeholder.equalsIgnoreCase("time")) {
            if (plugin.getFormattedSeconds(player.getUniqueId()) != null) {
                return plugin.getFormattedSeconds(player.getUniqueId());
            } else {
                return "00:00";
            }
        }

        if (placeholder.equalsIgnoreCase("suspicious")) {
            if (PlayerCache.getCouples().get(player.getUniqueId()) != null) {

                Player checked = plugin.getServer().getPlayer(PlayerCache.getCouples().get(player.getUniqueId()));
                if (checked == null) {
                    return "none";
                }

                if (!checked.isOnline()) {
                    return "none";
                }

                return checked.getName();
            } else {
                return "none";
            }
        }

        if (placeholder.equalsIgnoreCase("administrator")) {
            if (getKeyByValue(PlayerCache.getCouples(), player.getUniqueId()) != null) {

                Player checked = plugin.getServer().getPlayer(getKeyByValue(PlayerCache.getCouples(), player.getUniqueId()));
                if (checked == null) {
                    return "none";
                }

                if (!checked.isOnline()) {
                    return "none";
                }

                return checked.getName();
            } else {
                return "none";
            }
        }

        if (placeholder.equalsIgnoreCase("prefix")) {
            if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
                return SpigotConfig.SUSPECTPREFIX.color();
            }

            if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
                return SpigotConfig.STAFFPREFIX.color();
            }

            return "";
        }

        return null;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}