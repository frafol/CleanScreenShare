package it.frafol.cleanss.velocity.enums;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.objects.Placeholder;
import it.frafol.cleanss.velocity.objects.Utils;
import org.jetbrains.annotations.NotNull;

public enum VelocityMessages {

    PREFIX("messages.prefix"),

    USAGE("messages.usage"),

    ONLY_PLAYERS("messages.only_players"),
    NOT_ONLINE("messages.not_online"),

    NO_PERMISSION("messages.no_permission"),

    CONTROL_FORMAT("messages.staff_message.format"),

    CONTROL_CLEAN_NAME("messages.staff_message.clean.name"),
    CONTROL_CLEAN_COMMAND("messages.staff_message.clean.command"),

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

    CONTROLFINISH_USETITLE("messages.title.finish.enable"),
    CONTROLFINISH_TITLE("messages.title.finish.title"),
    CONTROLFINISH_SUBTITLE("messages.title.finish.subtitle"),
    CONTROLFINISH_FADEIN("messages.title.finish.fade_in"),
    CONTROLFINISH_FADEOUT("messages.title.finish.fade_out"),
    CONTROLFINISH_STAY("messages.title.finish.stay"),
    CONTROLFINISH_DELAY("messages.title.finish.delay"),

    YOURSELF("messages.yourself"),
    PLAYER_BYPASS("messages.player_bypass"),
    NO_EXIST("messages.server_offline"),
    MAINSUS("messages.control.suspicious_main"),
    LEAVESUS("messages.control.suspicious_disconnect"),
    FINISHSUS("messages.control.suspicious_finish"),

    NOT_CONTROL("messages.not_under_control"),
    CONTROL_CHAT("messages.chat.enable"),

    CONTROL_CHAT_FORMAT("messages.chat.format"),

    CONTROL_CHAT_SUS("messages.chat.states.suspect"),
    CONTROL_CHAT_STAFF("messages.chat.states.staffer"),

    DISCORD_STARTED("messages.discord.started"),
    DISCORD_FINISHED("messages.discord.finished"),
    DISCORD_QUIT("messages.discord.suspect_left_during_control"),
    INFO_MESSAGE("messages.info.main_message"),
    RELOADED("messages.reloaded");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    VelocityMessages(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getMessagesTextFile().getConfig().get(path));
    }

    public @NotNull String color() {
        return get(String.class).replace("&", "§");
    }

    public String getPath() {
        return path;
    }

    public void sendList(CommandSource commandSource, Player player_name, Placeholder... placeHolder) {
        Utils.sendFormattedList(this, commandSource, player_name, placeHolder);
    }

}