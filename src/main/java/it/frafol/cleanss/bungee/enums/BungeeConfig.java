package it.frafol.cleanss.bungee.enums;

import it.frafol.cleanss.bungee.CleanSS;
import org.jetbrains.annotations.NotNull;

public enum BungeeConfig {

    CONTROL_PERMISSION("permissions.control"),
    BYPASS_PERMISSION("permissions.bypass"),
    RELOAD_PERMISSION("permissions.reload"),
    CHECK_FOR_PROBLEMS("settings.check_for_problems"),
    UPDATE_CHECK("settings.update_check"),
    CONTROL("settings.server_name"),
    CONTROL_FALLBACK("settings.fallback_server_name"),
    DISCORD_ENABLED("discord-webhook.enabled"),
    DISCORD_TOKEN("discord-webhook.token"),
    DISCORD_ACTIVITY("discord-webhook.activity"),
    DISCORD_ACTIVITY_TYPE("discord-webhook.activity_type"),
    DISCORD_CHANNEL_ID("discord-webhook.channel_id"),
    DISCORD_EMBED_TITLE("discord-webhook.embed_title"),

    STATS("settings.stats");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    BungeeConfig(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().get(path));
    }

    public @NotNull String color() {
        return get(String.class).replace("&", "§");
    }
}