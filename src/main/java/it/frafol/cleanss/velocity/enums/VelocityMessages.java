package it.frafol.cleanss.velocity.enums;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.objects.Placeholder;
import it.frafol.cleanss.velocity.objects.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum VelocityMessages {

    PREFIX("messages.prefix"),

    USAGE("messages.usage"),

    ONLY_PLAYERS("messages.only_players"),
    NOT_ONLINE("messages.not_online"),

    NO_PERMISSION("messages.no_permission"),

    CONTROL_FORMAT("messages.staff_message.format"),

    CONTROL_CLEAN_NAME("messages.staff_message.clean.name"),
    CONTROL_CLEAN_COMMAND("messages.staff_message.clean.command"),

    ADMIN_NOTIFY("messages.control.admin_notification"),
    ADMIN_NOTIFY_FINISH("messages.control.admin_notification_finish"),

    CONTROL_CHEATER_NAME("messages.staff_message.cheater.name"),
    CONTROL_CHEATER_COMMAND("messages.staff_message.cheater.command"),

    CONTROL_ALREADY("messages.already_in_control"),

    CONTROL_ADMIT_NAME("messages.staff_message.admit.name"),
    CONTROL_ADMIT_COMMAND("messages.staff_message.admit.command"),

    CONTROL_REFUSE_NAME("messages.staff_message.refuse.name"),
    CONTROL_REFUSE_COMMAND("messages.staff_message.refuse.command"),

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
    NO_EXIST("messages.server_offline"),
    MAINSUS("messages.control.suspicious_main"),
    LEAVESUS("messages.control.suspicious_disconnect"),
    FINISHSUS("messages.control.suspicious_finish"),

    NOT_IN_LIMBO("messages.chat.limbo_addon.command_blocked"),

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
    LIMBO_ERROR("messages.spectate.limbo_error"),
    IN_CONTROL_ERROR("messages.spectate.in_control_error"),

    DISCORD_STARTED("messages.discord.started"),
    DISCORD_FINISHED("messages.discord.finished"),
    DISCORD_SPECTATOR("messages.discord.spectator"),
    DISCORD_SPECTATOR_END("messages.discord.spectator_end"),
    DISCORD_QUIT("messages.discord.suspect_left_during_control"),
    DISCORD_LUCKPERMS_FIX("messages.discord.luckperms.default_group_displayname"),
    INFO_MESSAGE("messages.info.main_message"),
    CLEAN("messages.discord.results.clean"),
    CHEATER("messages.discord.results.cheater"),
    LEFT("messages.discord.results.left"),
    RELOADED("messages.reloaded");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    VelocityMessages(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getMessagesTextFile().getConfig().get(path));
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

    public String getPath() {
        return path;
    }

    public void sendList(CommandSource commandSource, Player player_name, Placeholder... placeHolder) {
        Utils.sendFormattedList(this, commandSource, player_name, placeHolder);
    }

}