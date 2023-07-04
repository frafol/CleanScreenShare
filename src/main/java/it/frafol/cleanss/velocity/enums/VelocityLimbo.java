package it.frafol.cleanss.velocity.enums;

import it.frafol.cleanss.velocity.CleanSS;
import org.jetbrains.annotations.NotNull;

public enum VelocityLimbo {

    USE("limbo.use_limbo_api");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    VelocityLimbo(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getLimboTextFile().getConfig().get(path));
    }

}