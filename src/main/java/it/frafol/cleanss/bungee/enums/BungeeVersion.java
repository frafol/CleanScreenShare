package it.frafol.cleanss.bungee.enums;

import it.frafol.cleanss.bungee.CleanSS;
import org.jetbrains.annotations.NotNull;

public enum BungeeVersion {

    VERSION("version");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    BungeeVersion(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getVersionTextFile().get(path));
    }

    public @NotNull String color() {
        return get(String.class).replace("&", "§");
    }
}