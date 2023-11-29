package it.frafol.cleanss.velocity.enums;

import it.frafol.cleanss.velocity.CleanSS;

public enum VelocityLimbo {

    USE("limbo.use_limbo_api");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    VelocityLimbo(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getLimboTextFile().getConfig().get(path));
    }

}