package it.frafol.cleanss.velocity.enums;

import it.frafol.cleanss.velocity.CleanSS;

import java.util.List;

public enum VelocityConfig {

    CONTROL_PERMISSION("permissions.control"),
    BYPASS_PERMISSION("permissions.bypass"),
    INFO_PERMISSION("permissions.info"),
    SPEC_PERMISSION("permissions.spectate"),
    RELOAD_PERMISSION("permissions.reload"),
    PING_DELAY("settings.ping_delay"),
    CHECK_FOR_PROBLEMS("settings.check_for_problems"),
    UPDATE_CHECK("settings.update_check"),
    UPDATE_ALERT("settings.update_alert"),
    USE_DISCONNECT("settings.use_disconnect_instead_of_fallback"),
    CONTROL("settings.control_servers"),
    CONTROL_FALLBACK("settings.fallback_servers"),
    NOT_FALLBACK_STAFF("settings.do_not_fallback_staff"),
    CONTROL_BYPASS("settings.blocked_servers"),
    STRATEGY("settings.sort_strategy"),
    DISCORD_ENABLED("discord-webhook.enabled"),
    DISCORD_TOKEN("discord-webhook.token"),
    DISCORD_STATUS("discord-webhook.status"),
    DISCORD_ACTIVITY("discord-webhook.activity"),
    DISCORD_ACTIVITY_TYPE("discord-webhook.activity_type"),
    DISCORD_PLAYERS_FORMATTED("discord-webhook.discord_players_formatted_placeholder"),
    DISCORD_CHANNEL_ID("discord-webhook.channel_id"),
    DISCORD_EMBED_TITLE("discord-webhook.embed_title"),
    DISCORD_EMBED_FOOTER("discord-webhook.embed_footer"),
    DISCORD_EMBED_FOOTER_ICON("discord-webhook.embed_footer_icon"),
    SLOG_PUNISH("settings.slog.punish"),
    SLOG_COMMAND("settings.slog.punish_command"),
    BAN_COMMANDS("settings.slog.ban_commands"),
    DISABLE_PING("settings.disable_ping_check"),
    MYSQL("mysql.enable"),
    AUTO_UPDATE("settings.auto_update"),
    MYSQL_HOST("mysql.host"),
    MYSQL_USER("mysql.user"),
    PREMIUMVANISH("settings.premiumvanish_hook"),
    VELOCITYVANISH("settings.velocityvanish_hook"),
    MYSQL_DATABASE("mysql.database"),
    MYSQL_PASSWORD("mysql.password"),
    MYSQL_ARGUMENTS("mysql.arguments"),
    SEND_ADMIN_MESSAGE("settings.start.send_admin_message"),
    ENABLE_SPECTATING("settings.spectate.enable"),
    CHAT_DISABLED("settings.spectate.block_chat"),
    MESSAGE_DELAY("settings.start.server_message_delay"),
    COMMAND_REQUEST("settings.start.admin_commands.request"),
    COMMAND_TIME("settings.start.admin_commands.time_over"),
    ALLOWED_COMMANDS("settings.start.admin_commands.allowed_commands"),
    STATS("settings.stats");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    VelocityConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().getConfig().get(path));
    }

    public List<String> getStringList() {
        return instance.getConfigTextFile().getConfig().getStringList(path);
    }
}