package it.frafol.cleanss.bukkit.enums;

import it.frafol.cleanss.bukkit.CleanSS;
import org.jetbrains.annotations.NotNull;

public enum SpigotConfig {

    UPDATE_CHECK("options.update_check"),
    AUTO_UPDATE("options.auto_update"),

    STAFF_PERMISSION("options.staff_permission"),
    ADMIN_PERMISSION("options.admin_permission"),

    SPAWN_SET("options.messages.spawn_set"),

    PVP("options.prevent.player.pvp"),
    HUNGER("options.prevent.player.hunger"),
    VOID("options.prevent.player.void"),
    MOVE("options.prevent.player.move"),
    CHAT("options.prevent.player.chat"),
    SPAWN("options.teleport_to_spawn_on_join"),
    GAMEMODE("options.change_gamemode_on_join"),

    BREAK("options.prevent.player.block_break"),
    PLACE("options.prevent.player.block_place"),

    WEATHER("options.prevent.world.weather_change"),
    DAY_CYCLE("options.prevent.world.daylight_cycle"),
    MOB_SPAWNING("options.prevent.world.mob_spawning"),

    INVINCIBLE("options.invincible");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    SpigotConfig(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().getConfig().get(path));
    }

    public @NotNull String color() {
        return get(String.class).replace("&", "§");
    }

}