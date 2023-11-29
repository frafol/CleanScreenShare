package it.frafol.cleanss.bukkit.objects;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.google.common.base.Strings;
import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.objects.utils.ReflectionUtils;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.Stijn.ScoreboardAPI.ScoreboardAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private String genEntry(int slot) {
        return ChatColor.values()[slot].toString();
    }

    private String getFirstSplit(String s) {
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    private String getSecondSplit(String s) {
        if (s.length() > 32) {
            s = s.substring(0, 32);
        }
        return s.length() > 16 ? s.substring(16) : "";
    }

    public void createSuspectScoreboard(Player player) {

        ScoreboardAPI scoreboard = new ScoreboardAPI(player.getName(), SpigotConfig.SB_SUSPECTTITLE.color(), DisplaySlot.SIDEBAR);

        int scoreValue = 15;

        for (String line : SpigotConfig.SB_SUSPECTLINES.getStringList()) {

            Team team = scoreboard.getScoreboard(player).registerNewTeam("SLOT_" + scoreValue);
            team.addEntry(genEntry(scoreValue));

            String final_line = line;
            if (instance.isPAPI()) {
                final_line = PlaceholderAPI.setPlaceholders(player, line);
            }

            try {

                if (scoreValue == 0) {
                    return;
                }

                if (!scoreboard.getScoreboard(player).getEntries().contains(final_line)) {
                    scoreboard.setScore(final_line, scoreValue);
                }

                String pre = getFirstSplit(final_line);
                String suf = getFirstSplit(ChatColor.getLastColors(pre) + getSecondSplit(final_line));
                team.setPrefix(pre);
                team.setSuffix(suf);
                scoreValue--;
                continue;

            } catch (IllegalArgumentException ignored) {
                instance.getLogger().severe("Cannot set score for " + player.getName() + " (suspect), too many characters. (is PlaceholderAPI installed?)");
            }

            scoreValue--;
        }

        scoreboard.setScoreboard(player);
    }

    public void createAdminScoreboard(Player player) {

        ScoreboardAPI scoreboard = new ScoreboardAPI(player.getName(), SpigotConfig.SB_STAFFTITLE.color(), DisplaySlot.SIDEBAR);

        int scoreValue = 15;
        for (String line : SpigotConfig.SB_STAFFLINES.getStringList()) {

            Team team = scoreboard.getScoreboard(player).registerNewTeam("SLOT_" + scoreValue);
            team.addEntry(genEntry(scoreValue));

            String final_line = line;
            if (instance.isPAPI()) {
                final_line = PlaceholderAPI.setPlaceholders(player, line);
            }

            try {

                if (scoreValue == 0) {
                    return;
                }

                if (!scoreboard.getScoreboard(player).getEntries().contains(final_line)) {
                    scoreboard.setScore(final_line, scoreValue);
                }

                String pre = getFirstSplit(final_line);
                String suf = getFirstSplit(ChatColor.getLastColors(pre) + getSecondSplit(final_line));
                team.setPrefix(pre);
                team.setSuffix(suf);
                scoreValue--;
                continue;

            } catch (IllegalArgumentException ignored) {
                instance.getLogger().severe("Cannot set score for " + player.getName() + " (administrator), too many characters. (is PlaceholderAPI installed?)");
            }

            scoreValue--;
        }

        scoreboard.setScoreboard(player);
    }

    public void deleteSuspectScoreboard(Player player) {

        if (!SpigotConfig.SB_SUSPECT.get(Boolean.class)) {
            return;
        }

        ScoreboardAPI scoreboard = new ScoreboardAPI(player.getName(), SpigotConfig.SB_SUSPECTTITLE.color(), DisplaySlot.SIDEBAR);

        for (Team team : scoreboard.getScoreboard(player).getTeams()) {
            team.unregister();
        }
    }

    public void deleteAdminScoreboard(Player player) {

        if (!SpigotConfig.SB_STAFF.get(Boolean.class)) {
            return;
        }

        ScoreboardAPI scoreboard = new ScoreboardAPI(player.getName(), SpigotConfig.SB_STAFFTITLE.color(), DisplaySlot.SIDEBAR);

        for (Team team : scoreboard.getScoreboard(player).getTeams()) {
            team.unregister();
        }
    }

    public void updateScoreboardTask() {
        UniversalScheduler.getScheduler(instance).runTaskTimer(PlayerCache::updateScoreboard, SpigotConfig.SB_UPDATE.get(Integer.class), SpigotConfig.SB_UPDATE.get(Integer.class));
    }

    public void updateTabListTask() {
        UniversalScheduler.getScheduler(instance).runTaskTimer(PlayerCache::updateTabList, SpigotConfig.TABLIST_UPDATE.get(Integer.class), SpigotConfig.TABLIST_UPDATE.get(Integer.class));
    }

    private void updateScoreboard() {
        for (UUID uuid : PlayerCache.getSuspicious()) {

            if (!SpigotConfig.SB_SUSPECT.get(Boolean.class)) {
                continue;
            }

            deleteSuspectScoreboard(instance.getServer().getPlayer(uuid));
            createSuspectScoreboard(instance.getServer().getPlayer(uuid));
        }

        for (UUID uuid : PlayerCache.getAdministrator()) {

            if (!SpigotConfig.SB_STAFF.get(Boolean.class)) {
                continue;
            }

            deleteAdminScoreboard(instance.getServer().getPlayer(uuid));
            createAdminScoreboard(instance.getServer().getPlayer(uuid));
        }
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
            sendTabList(player, header, footer);
            return;
        }

        String final_header = PlaceholderAPI.setPlaceholders(player, header);
        String final_footer = PlaceholderAPI.setPlaceholders(player, footer);
        sendTabList(player, final_header, final_footer);
    }

    public void setSuspectTabList(Player player) {

        String header = String.join("\n", SpigotConfig.TABLIST_SUSPECTHEADER.getStringList());
        String footer = String.join("\n", SpigotConfig.TABLIST_SUSPECTFOOTER.getStringList());

        if (!instance.isPAPI()) {
            sendTabList(player, header, footer);
            return;
        }

        String final_header = PlaceholderAPI.setPlaceholders(player, header);
        String final_footer = PlaceholderAPI.setPlaceholders(player, footer);
        sendTabList(player, final_header, final_footer);
    }

    public static void sendTabList(Player player, String header, String footer) {

        header = Strings.isNullOrEmpty(header) ? "" : color(header);
        footer = Strings.isNullOrEmpty(footer) ? "" : color(footer);

        if (!isVersionLessThanOrEqual("1.12.2")) {
            player.setPlayerListHeaderFooter(header, footer);

            if (!instance.isPAPI()) {
                player.setPlayerListName(SpigotConfig.TABLIST_FORMAT.color().replace("%player%", player.getName()));
                return;
            }

            player.setPlayerListName(color(PlaceholderAPI.setPlaceholders(player, SpigotConfig.TABLIST_FORMAT.get(String.class).replace("%player%", player.getName()))));
            return;
        }

        try {
            Method chatComponentBuilderMethod = Objects.requireNonNull(ReflectionUtils.getNMSClass("IChatBaseComponent")).getDeclaredClasses()[0].getMethod("a", String.class);
            Object tabHeader = chatComponentBuilderMethod.invoke(null, "{\"text\":\"" + header + "\"}");
            Object tabFooter = chatComponentBuilderMethod.invoke(null, "{\"text\":\"" + footer + "\"}");
            Object packet = Objects.requireNonNull(ReflectionUtils.getNMSClass("PacketPlayOutPlayerListHeaderFooter")).getConstructor().newInstance();

            Field aField;
            Field bField;
            try {
                aField = packet.getClass().getDeclaredField("a");
                bField = packet.getClass().getDeclaredField("b");
            } catch (Exception ex) {
                aField = packet.getClass().getDeclaredField("header");
                bField = packet.getClass().getDeclaredField("footer");
            }

            aField.setAccessible(true);
            aField.set(packet, tabHeader);

            bField.setAccessible(true);
            bField.set(packet, tabFooter);

            ReflectionUtils.sendPacket(player, packet);

        } catch (Exception ignored) {
            instance.getLogger().severe("Cannot send tablist to " + player.getName() + ".");
        }
    }

    public static boolean isVersionLessThanOrEqual(String version) {
        String serverVersion = Bukkit.getVersion();

        Pattern pattern = Pattern.compile(".*(\\d+\\.\\d+\\.\\d+).*");
        Matcher matcher = pattern.matcher(serverVersion);

        if (matcher.matches()) {
            String serverVersionNumber = matcher.group(1);
            String[] serverVersionParts = serverVersionNumber.split("\\.");
            String[] targetVersionParts = version.split("\\.");

            for (int i = 0; i < Math.min(serverVersionParts.length, targetVersionParts.length); i++) {
                int serverPart = Integer.parseInt(serverVersionParts[i]);
                int targetPart = Integer.parseInt(targetVersionParts[i]);

                if (serverPart < targetPart) {
                    return true;
                } else if (serverPart > targetPart) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public String color(String string) {
        String hex = convertHexColors(string);
        return hex.replace("&", "§");
    }

    private String convertHexColors(String message) {

        if (!containsHexColor(message)) {
            return message;
        }

        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return message;
    }

    private boolean containsHexColor(String message) {
        String hexColorPattern = "(?i)&#[a-f0-9]{6}";
        return message.matches(".*" + hexColorPattern + ".*");
    }
}
