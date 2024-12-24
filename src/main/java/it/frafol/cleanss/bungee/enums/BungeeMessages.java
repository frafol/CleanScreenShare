package it.frafol.cleanss.bungee.enums;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.objects.ChatUtil;
import it.frafol.cleanss.bungee.objects.Placeholder;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum BungeeMessages {

    PREFIX("messages.prefix"),

    USAGE("messages.usage"),

    ONLY_PLAYERS("messages.only_players"),
    NOT_ONLINE("messages.not_online"),
    NOT_VALID("messages.server_not_valid"),

    PLAYER_MISSING("messages.player_missing"),
    NO_PERMISSION("messages.no_permission"),

    CONTROL_DELAYMESSAGE("messages.staff_message.delay"),
    CONTROL_USEVERTICALFORMAT("messages.staff_message.use_vertical"),
    CONTROL_VERTICALFORMAT("messages.staff_message.vertical_format"),
    CONTROL_HORIZONTALFORMAT("messages.staff_message.horizontal_format"),

    BUTTON_EXECUTION("messages.staff_message.execution"),

    COMMAND_BLOCKED("messages.control.command_blocked"),
    CANT_SWITCH("messages.control.cant_switch"),

    ADMIN_NOTIFY("messages.control.admin_notification"),
    ADMIN_NOTIFY_FINISH("messages.control.admin_notification_finish"),

    CONTROL_ALREADY("messages.already_in_control"),
    COMMAND_REQUEST("messages.control.admin_commands.request"),

    CONTROL_USETITLE("messages.title.start.enable"),
    CONTROL_TITLE("messages.title.start.title"),
    CONTROL_SUBTITLE("messages.title.start.subtitle"),
    CONTROL_FADEIN("messages.title.start.fade_in"),
    CONTROL_FADEOUT("messages.title.start.fade_out"),
    CONTROL_STAY("messages.title.start.stay"),
    CONTROL_DELAY("messages.title.start.delay"),

    ADMINCONTROL_USETITLE("messages.title.admin_start.enable"),
    ADMINCONTROL_TITLE("messages.title.admin_start.title"),
    ADMINCONTROL_SUBTITLE("messages.title.admin_start.subtitle"),
    ADMINCONTROL_FADEIN("messages.title.admin_start.fade_in"),
    ADMINCONTROL_FADEOUT("messages.title.admin_start.fade_out"),
    ADMINCONTROL_STAY("messages.title.admin_start.stay"),
    ADMINCONTROL_DELAY("messages.title.admin_start.delay"),

    CONTROLFINISH_USETITLE("messages.title.finish.enable"),
    CONTROLFINISH_TITLE("messages.title.finish.title"),
    CONTROLFINISH_SUBTITLE("messages.title.finish.subtitle"),
    CONTROLFINISH_FADEIN("messages.title.finish.fade_in"),
    CONTROLFINISH_FADEOUT("messages.title.finish.fade_out"),
    CONTROLFINISH_STAY("messages.title.finish.stay"),
    CONTROLFINISH_DELAY("messages.title.finish.delay"),

    ADMINCONTROLFINISH_USETITLE("messages.title.admin_finish.enable"),
    ADMINCONTROLFINISH_TITLE("messages.title.admin_finish.title"),
    ADMINCONTROLFINISH_SUBTITLE("messages.title.admin_finish.subtitle"),
    ADMINCONTROLFINISH_FADEIN("messages.title.admin_finish.fade_in"),
    ADMINCONTROLFINISH_FADEOUT("messages.title.admin_finish.fade_out"),
    ADMINCONTROLFINISH_STAY("messages.title.admin_finish.stay"),
    ADMINCONTROLFINISH_DELAY("messages.title.admin_finish.delay"),

    YOURSELF("messages.yourself"),
    PLAYER_BYPASS("messages.player_bypass"),
    PLAYER_BYPASS_SERVER("messages.player_bypass_server"),
    NO_EXIST("messages.server_offline"),
    MAINSUS("messages.control.suspicious_main"),
    LEAVESUS("messages.control.suspicious_disconnect"),
    FINISHSUS("messages.control.suspicious_finish"),

    NOT_CONTROL("messages.not_under_control"),
    CONTROL_CHAT("messages.chat.enable"),

    CONTROL_CHAT_FORMAT("messages.chat.format"),

    CONTROL_CHAT_SUS("messages.chat.states.suspect"),
    CONTROL_CHAT_STAFF("messages.chat.states.staffer"),

    SPECTATING("messages.spectate.spectating"),
    NOT_SPECTATING("messages.spectate.not_spectating"),
    SPECT_ADMIN_NOTIFY("messages.spectate.admin_notification"),
    SPECT_ADMIN_NOTIFY_FINISH("messages.spectate.admin_notification_finish"),
    CHAT_DISABLED("messages.spectate.chat_disabled"),
    INVALID_SERVER("messages.spectate.invalid_server"),
    IN_CONTROL_ERROR("messages.spectate.in_control_error"),

    UPDATE_ALERT("messages.update_found.alert"),
    UPDATE_LINK("messages.update_found.clickable_link"),

    DISCORD_STARTED("messages.discord.started"),
    DISCORD_STARTED_THUMBNAIL("messages.discord.started_thumbnail"),
    DISCORD_FINISHED("messages.discord.finished"),
    DISCORD_FINISHED_THUMBNAIL("messages.discord.finished_thumbnail"),
    DISCORD_SPECTATOR("messages.discord.spectator"),
    DISCORD_SPECTATOR_THUMBNAIL("messages.discord.spectator_thumbnail"),
    DISCORD_SPECTATOR_END("messages.discord.spectator_end"),
    DISCORD_SPECTATOR_END_THUMBNAIL("messages.discord.spectator_end_thumbnail"),
    DISCORD_QUIT("messages.discord.suspect_left_during_control"),
    DISCORD_QUIT_THUMBNAIL("messages.discord.suspect_left_during_control_thumbnail"),
    DISCORD_LUCKPERMS_FIX("messages.discord.luckperms.default_group_displayname"),
    INFO_MESSAGE("messages.info.main_message"),
    INFO_TRUE("messages.info.true_message"),
    INFO_FALSE("messages.info.false_message"),
    CLEAN("messages.discord.results.clean"),
    CHEATER("messages.discord.results.cheater"),
    LEFT("messages.discord.results.left"),
    RELOADED("messages.reloaded");

    @Getter
    private final String path;

    public static final CleanSS instance = CleanSS.getInstance();

    BungeeMessages(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getMessagesTextFile().get(path));
    }

    public String color() {
        String hex = convertHexColors(get(String.class));
        return hex.replace("&", "§");
    }

    public static String convertHexColors(String str) {
        Pattern unicode = Pattern.compile("\\\\u\\+[a-fA-F0-9]{4}");
        Matcher match = unicode.matcher(str);
        while (match.find()) {
            String code = str.substring(match.start(),match.end());
            str = str.replace(code,Character.toString((char) Integer.parseInt(code.replace("\\u+",""),16)));
            match = unicode.matcher(str);
        }
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        match = pattern.matcher(str);
        while (match.find()) {
            String color = str.substring(match.start(),match.end());
            str = str.replace(color,ChatColor.of(color.replace("&","")) + "");
            match = pattern.matcher(str);
        }
        return ChatColor.translateAlternateColorCodes('&',str);
    }

    public void sendList(CommandSender commandSource, Placeholder... placeholder) {
        ChatUtil.sendFormattedList(this, commandSource, null, false, placeholder);
    }

    public void sendStartList(CommandSender commandSource, ProxiedPlayer player_name, Placeholder... placeholder) {
        ChatUtil.sendFormattedList(this, commandSource, player_name, true, placeholder);
    }
}