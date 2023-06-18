package it.frafol.cleanss.bukkit.enums;

import it.frafol.cleanss.bukkit.CleanSS;
import org.jetbrains.annotations.NotNull;

public enum SpigotVersion {

    VERSION("version");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    SpigotVersion(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getVersionTextFile().getConfig().get(path));
    }

    public @NotNull String color() {
        return get(String.class).replace("&", "§");
    }

}