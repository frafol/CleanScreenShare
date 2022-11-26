package it.frafol.cleanss.velocity.enums;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.objects.ChatUtil;
import it.frafol.cleanss.velocity.objects.Placeholder;
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
    CONTROL_ADMIT_NAME("messages.staff_message.admit.name"),
    CONTROL_ADMIT_COMMAND("messages.staff_message.admit.command"),

    CONTROL_USETITLE("messages.title.enable"),
    CONTROL_TITLE("messages.title.title"),
    CONTROL_SUBTITLE("messages.title.subtitle"),
    CONTROL_FADEIN("messages.title.fade_in"),
    CONTROL_FADEOUT("messages.title.fade_out"),
    CONTROL_STAY("messages.title.stay"),
    CONTROL_DELAY("messages.title.delay"),

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
        ChatUtil.sendFormattedList(this, commandSource, player_name, placeHolder);
    }

}