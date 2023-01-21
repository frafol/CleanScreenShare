package it.frafol.cleanss.velocity.enums;

import it.frafol.cleanss.velocity.CleanSS;
import org.jetbrains.annotations.NotNull;

public enum VelocityConfig2 {

    REMOVE_WARNINGS("remove_warnings");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    VelocityConfig2(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().getConfig().get(path));
    }

}