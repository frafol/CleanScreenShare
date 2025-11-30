package it.frafol.cleanss.bungee.enums;

import it.frafol.cleanss.bungee.CleanSS;

import java.util.List;

public enum BungeeConfig {

    CONTROL_PERMISSION("permissions.control"),
    BYPASS_PERMISSION("permissions.bypass"),
    INFO_PERMISSION("permissions.info"),
    RELOAD_PERMISSION("permissions.reload"),
    CHECK_FOR_PROBLEMS("settings.check_for_problems"),
    CONNECTION_DELAY("settings.connection_delay"),
    PING_DELAY("settings.ping_delay"),
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
    DISCORD_ACTIVITY("discord-webhook.activity"),
    DISCORD_ACTIVITY_TYPE("discord-webhook.activity_type"),
    DISCORD_STATUS("discord-webhook.status"),
    DISCORD_CHANNEL_ID("discord-webhook.channel_id"),
    DISCORD_PLAYERS_FORMATTED("discord-webhook.discord_players_formatted_placeholder"),
    DISCORD_EMBED_TITLE("discord-webhook.embed_title"),
    DISCORD_EMBED_FOOTER("discord-webhook.embed_footer"),
    DISCORD_EMBED_FOOTER_ICON("discord-webhook.embed_footer_icon"),
    SLOG_PUNISH("settings.slog.punish"),
    SLOG_COMMAND("settings.slog.punish_command"),
    BAN_COMMANDS("settings.slog.ban_commands"),
    DISABLE_PING("settings.disable_ping_check"),
    AUTO_UPDATE("settings.auto_update"),
    MYSQL("mysql.enable"),
    PREMIUMVANISH("settings.premiumvanish_hook"),
    MYSQL_HOST("mysql.host"),
    MYSQL_USER("mysql.user"),
    MYSQL_DATABASE("mysql.database"),
    MYSQL_PASSWORD("mysql.password"),
    MYSQL_ARGUMENTS("mysql.arguments"),
    TAKE_CHATLOGS("settings.chat_logs.enable"),
    EXPIRE_CHATLOGS("settings.chat_logs.logs_expire"),
    UPLOAD_CHATLOGS("settings.chat_logs.upload"),
    SEND_ADMIN_MESSAGE("settings.start.send_admin_message"),
    PAPI_PROXYBRIDGE("settings.papiproxybridge_hook"),
    PAPI_NATIVE("settings.native_papi_hook"),
    ENABLE_SPECTATING("settings.spectate.enable"),
    SPECTATOR_SERVER_SWITCH("settings.spectate.teleport_spectator"),
    CHAT_DISABLED("settings.spectate.block_chat"),
    MESSAGE_DELAY("settings.start.server_message_delay"),
    COMMAND_REQUEST("settings.start.admin_commands.request"),
    COMMAND_TIME("settings.start.admin_commands.time_over"),
    ALLOWED_COMMANDS("settings.start.admin_commands.allowed_commands"),
    STATS("settings.stats");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    BungeeConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().get(path));
    }

    public List<String> getStringList() {
        return instance.getConfigTextFile().getStringList(path);
    }

    public String color() {
        return get(String.class).replace("&", "§");
    }
}