package it.frafol.cleanss.bukkit.enums;

import it.frafol.cleanss.bukkit.CleanSS;

public enum SpigotCache {

    ADMIN_SPAWN("spawns.admin"),
    SUSPECT_SPAWN("spawns.suspect"),
    OTHER_SPAWN("spawns.other");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    SpigotCache(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getCacheTextFile().getConfig().get(path));
    }

    public String color() {
        return get(String.class).replace("&", "§");
    }

}