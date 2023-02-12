package it.frafol.cleanss.bukkit.enums;

import it.frafol.cleanss.bukkit.CleanSS;
import org.jetbrains.annotations.NotNull;

public enum SpigotCache {

    ADMIN_SPAWN("spawns.admin"),
    SUSPECT_SPAWN("spawns.suspect");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    SpigotCache(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getCacheTextFile().getConfig().get(path));
    }

    public @NotNull String color() {
        return get(String.class).replace("&", "§");
    }

}