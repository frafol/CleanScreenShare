package it.frafol.cleanss.bukkit.objects;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import fr.mrmicky.fastboard.FastBoard;
import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.objects.utils.TabListUtil;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

@UtilityClass
public class PlayerCache {

    private final CleanSS instance = CleanSS.getInstance();

    @Getter
    private final HashSet<UUID> no_chat = new HashSet<>();

    @Getter
    private final HashMap<UUID, UUID> couples = new HashMap<>();

    @Getter
    private final HashSet<UUID> suspicious = new HashSet<>();

    @Getter
    private final HashSet<UUID> administrator = new HashSet<>();

    @Getter
    private HashMap<UUID, FastBoard> adminBoard = new HashMap<>();

    @Getter
    private HashMap<UUID, FastBoard> suspectBoard = new HashMap<>();

    @Contract("_ -> new")
    public static Location StringToLocation(String line) {

        String[] loc = line.split(";");
        World world = instance.getServer().getWorld(loc[0]);

        if (world == null) {
            world = instance.getServer().createWorld(new WorldCreator(loc[0]));
        }

        return new Location(world, Double.parseDouble(loc[1]), Double.parseDouble(loc[2]), Double.parseDouble(loc[3]), Float.parseFloat(loc[4]), Float.parseFloat(loc[5]));
    }

    public static String LocationToString(Location location) {

        String world = location.getWorld().getName();
        String x = String.valueOf(location.getBlockX());
        String y = String.valueOf(location.getBlockY());
        String z = String.valueOf(location.getBlockZ());
        String yaw = String.valueOf(location.getYaw());
        String pitch = String.valueOf(location.getPitch());

        return world + ";" + x + ";" + y + ";" + z + ";" + yaw + ";" + pitch;
    }

    public void createSuspectScoreboard(Player player) {
        FastBoard board = new FastBoard(player);
        suspectBoard.put(player.getUniqueId(), board);
    }

    public void createAdminScoreboard(Player player) {
        FastBoard board = new FastBoard(player);
        adminBoard.put(player.getUniqueId(), board);
    }

    public void deleteSuspectScoreboard(Player player) {

        if (!SpigotConfig.SB_SUSPECT.get(Boolean.class)) {
            return;
        }

        if (suspectBoard.get(player.getUniqueId()) == null) {
            return;
        }

        suspectBoard.get(player.getUniqueId()).delete();
        suspectBoard.remove(player.getUniqueId());
    }

    public void deleteAdminScoreboard(Player player) {

        if (!SpigotConfig.SB_STAFF.get(Boolean.class)) {
            return;
        }

        if (adminBoard.get(player.getUniqueId()) == null) {
            return;
        }

        adminBoard.get(player.getUniqueId()).delete();
        adminBoard.remove(player.getUniqueId());
    }

    public void updateScoreboardTask() {
        UniversalScheduler.getScheduler(instance).runTaskTimer(PlayerCache::updateScoreboard, SpigotConfig.SB_UPDATE.get(Integer.class), SpigotConfig.SB_UPDATE.get(Integer.class));
    }

    public void updateTabListTask() {
        UniversalScheduler.getScheduler(instance).runTaskTimer(PlayerCache::updateTabList, SpigotConfig.TABLIST_UPDATE.get(Integer.class), SpigotConfig.TABLIST_UPDATE.get(Integer.class));
    }

    private void updateScoreboard() {
        instance.getServer().getOnlinePlayers().forEach(player -> {

            if (SpigotConfig.SB_SUSPECT.get(Boolean.class)) {
                FastBoard board = suspectBoard.get(player.getUniqueId());
                if (suspectBoard.get(player.getUniqueId()) != null && board != null) {
                    if (!instance.isPAPI()) {
                        board.updateTitle(SpigotConfig.SB_SUSPECTTITLE.color());
                        board.updateLines(SpigotConfig.SB_SUSPECTLINES.parseScoreboard(player));
                    } else if (!PlaceholderAPI.setPlaceholders(player, "%screenshare_administrator%").equalsIgnoreCase("none")) {
                        board.updateTitle(SpigotConfig.SB_SUSPECTTITLE.color());
                        board.updateLines(SpigotConfig.SB_SUSPECTLINES.parseScoreboard(player));
                    }
                }
            }

            if (SpigotConfig.SB_STAFF.get(Boolean.class)) {
                FastBoard board = adminBoard.get(player.getUniqueId());
                if (adminBoard.get(player.getUniqueId()) != null && board != null) {
                    if (!instance.isPAPI()) {
                        board.updateTitle(SpigotConfig.SB_STAFFTITLE.color());
                        board.updateLines(SpigotConfig.SB_STAFFLINES.parseScoreboard(player));
                    } else if (!PlaceholderAPI.setPlaceholders(player, "%screenshare_suspicious%").equalsIgnoreCase("none")) {
                        board.updateTitle(SpigotConfig.SB_STAFFTITLE.color());
                        board.updateLines(SpigotConfig.SB_STAFFLINES.parseScoreboard(player));
                    }
                }
            }
        });
    }

    private void updateTabList() {
        for (UUID uuid : PlayerCache.getSuspicious()) {

            if (!SpigotConfig.TABLIST_SUSPECT.get(Boolean.class)) {
                continue;
            }

            setSuspectTabList(instance.getServer().getPlayer(uuid));
        }

        for (UUID uuid : PlayerCache.getAdministrator()) {

            if (!SpigotConfig.TABLIST_STAFF.get(Boolean.class)) {
                continue;
            }

            setStaffTabList(instance.getServer().getPlayer(uuid));
        }
    }

    public void setStaffTabList(Player player) {
        String header = String.join("\n", SpigotConfig.TABLIST_STAFFHEADER.getStringList());
        String footer = String.join("\n", SpigotConfig.TABLIST_STAFFFOOTER.getStringList());

        if (!instance.isPAPI()) {
            TabListUtil.sendTabList(player, header, footer);
            return;
        }

        String final_header = PlaceholderAPI.setPlaceholders(player, header);
        String final_footer = PlaceholderAPI.setPlaceholders(player, footer);
        TabListUtil.sendTabList(player, final_header, final_footer);
    }

    public void setSuspectTabList(Player player) {

        String header = String.join("\n", SpigotConfig.TABLIST_SUSPECTHEADER.getStringList());
        String footer = String.join("\n", SpigotConfig.TABLIST_SUSPECTFOOTER.getStringList());

        if (!instance.isPAPI()) {
            TabListUtil.sendTabList(player, header, footer);
            return;
        }

        String final_header = PlaceholderAPI.setPlaceholders(player, header);
        String final_footer = PlaceholderAPI.setPlaceholders(player, footer);
        TabListUtil.sendTabList(player, final_header, final_footer);
    }
}
