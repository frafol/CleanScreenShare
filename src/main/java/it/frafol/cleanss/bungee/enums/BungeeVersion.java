package it.frafol.cleanss.bungee.enums;

import it.frafol.cleanss.bungee.CleanSS;

public enum BungeeVersion {

    VERSION("version");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    BungeeVersion(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getVersionTextFile().get(path));
    }

    public String color() {
        return get(String.class).replace("&", "§");
    }
}