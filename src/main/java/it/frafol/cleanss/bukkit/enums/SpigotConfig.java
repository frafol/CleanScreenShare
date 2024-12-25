package it.frafol.cleanss.bukkit.enums;

import it.frafol.cleanss.bukkit.CleanSS;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SpigotConfig {

    UPDATE_CHECK("options.update_check"),
    AUTO_UPDATE("options.auto_update"),

    STAFF_PERMISSION("options.staff_permission"),
    ADMIN_PERMISSION("options.admin_permission"),

    SPAWN_SET("options.messages.spawn_set"),

    RELOADED("options.messages.reloaded"),
    SPAWN_ADMIN("options.messages.spawn_types.admin"),
    SPAWN_SPECTATOR("options.messages.spawn_types.other"),
    SPAWN_SUSPECT("options.messages.spawn_types.suspect"),

    PVP("options.prevent.player.pvp"),
    HUNGER("options.prevent.player.hunger"),
    VOID("options.prevent.player.void"),
    MOVE("options.prevent.player.move"),
    CHAT("options.prevent.player.chat"),
    CHATFORMAT("options.chat_format"),
    SPAWN("options.teleport_to_spawn_on_join"),
    GAMEMODE("options.change_gamemode_on_join"),

    BREAK("options.prevent.player.block_break"),
    PLACE("options.prevent.player.block_place"),

    WEATHER("options.prevent.world.weather_change"),
    DAY_CYCLE("options.prevent.world.daylight_cycle"),
    MOB_SPAWNING("options.prevent.world.mob_spawning"),

    SUSPECTPREFIX("options.messages.suspect_prefix"),
    STAFFPREFIX("options.messages.staff_prefix"),
    OTHERPREFIX("options.messages.other_prefix"),

    JOIN_SOUNDS("options.join_sounds.enabled"),
    SOUND_STAFF("options.join_sounds.staff_join"),
    SOUND_SUSPECT("options.join_sounds.suspect_join"),
    SOUND_OTHER("options.join_sounds.other_join"),

    NONE("options.messages.placeholder_none"),

    SB_UPDATE("options.scoreboard.update_task"),

    SB_STAFF("options.scoreboard.staff_board.enabled"),
    SB_STAFFTITLE("options.scoreboard.staff_board.title"),
    SB_STAFFLINES("options.scoreboard.staff_board.lines"),

    SB_SUSPECT("options.scoreboard.suspect_board.enabled"),
    SB_SUSPECTTITLE("options.scoreboard.suspect_board.title"),
    SB_SUSPECTLINES("options.scoreboard.suspect_board.lines"),

    SB_OTHER("options.scoreboard.other_board.enabled"),
    SB_OTHERTITLE("options.scoreboard.other_board.title"),
    SB_OTHERLINES("options.scoreboard.other_board.lines"),

    TABLIST_UPDATE("options.tablist.update_task"),
    TABLIST_FORMAT("options.tablist.format"),

    NAMETAG("options.nametag.enabled"),
    NAMETAG_PREFIX("options.nametag.nametag_prefix"),
    NAMETAG_SUFFIX("options.nametag.nametag_suffix"),

    TABLIST_STAFF("options.tablist.staff_tablist.enabled"),
    TABLIST_STAFFHEADER("options.tablist.staff_tablist.header"),
    TABLIST_STAFFFOOTER("options.tablist.staff_tablist.footer"),

    TABLIST_SUSPECT("options.tablist.suspect_tablist.enabled"),
    TABLIST_SUSPECTHEADER("options.tablist.suspect_tablist.header"),
    TABLIST_SUSPECTFOOTER("options.tablist.suspect_tablist.footer"),

    TABLIST_OTHER("options.tablist.other_tablist.enabled"),
    TABLIST_OTHERHEADER("options.tablist.other_tablist.header"),
    TABLIST_OTHERFOOTER("options.tablist.other_tablist.footer"),

    CUSTOM_JOIN_MESSAGE("options.custom_join_message"),
    CUSTOM_LEAVE_MESSAGE("options.custom_leave_message"),

    PAPI_HOOK("options.placeholderapi_hook"),

    INVINCIBLE("options.invincible");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    SpigotConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().getConfig().get(path));
    }

    public List<String> getStringList() {
        return instance.getConfigTextFile().getConfig().getStringList(path);
    }

    public List<String> parseScoreboard(Player player) {
        List<String> list = new ArrayList<>();
        for (String string : instance.getConfigTextFile().getConfig().getStringList(path)) {
            if (instance.isPAPI()) {
                list.add(color(PlaceholderAPI.setPlaceholders(player, string)).replace("%player%", player.getName()));
            } else {
                list.add(color(string).replace("%player%", player.getName()));
            }
        }
        return list;
    }

    public String color(String string) {
        String hex = convertHexColors(string);
        return hex.replace("&", "§");
    }

    public String color() {
        String hex = convertHexColors(get(String.class));
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