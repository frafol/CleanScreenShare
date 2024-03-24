package it.frafol.cleanss.bungee.enums;

import it.frafol.cleanss.bungee.CleanSS;

import java.util.List;

public enum BungeeConfig {

    CONTROL_PERMISSION("permissions.control"),
    BYPASS_PERMISSION("permissions.bypass"),
    INFO_PERMISSION("permissions.info"),
    RELOAD_PERMISSION("permissions.reload"),
    CHECK_FOR_PROBLEMS("settings.check_for_problems"),
    PING_DELAY("settings.ping_delay"),
    UPDATE_CHECK("settings.update_check"),
    USE_DISCONNECT("settings.use_disconnect_instead_of_fallback"),
    CONTROL("settings.control_servers"),
    CONTROL_FALLBACK("settings.fallback_servers"),
    STRATEGY("settings.sort_strategy"),
    DISCORD_ENABLED("discord-webhook.enabled"),
    DISCORD_TOKEN("discord-webhook.token"),
    DISCORD_ACTIVITY("discord-webhook.activity"),
    DISCORD_ACTIVITY_TYPE("discord-webhook.activity_type"),
    DISCORD_STATUS("discord-webhook.status"),
    DISCORD_CHANNEL_ID("discord-webhook.channel_id"),
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
    SEND_ADMIN_MESSAGE("settings.start.send_admin_message"),
    ENABLE_SPECTATING("settings.spectate.enable"),
    CHAT_DISABLED("settings.spectate.block_chat"),
    MESSAGE_DELAY("settings.start.message_to_control_delay"),
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