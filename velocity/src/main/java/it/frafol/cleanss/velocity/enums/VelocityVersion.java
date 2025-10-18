package it.frafol.cleanss.velocity.enums;

import it.frafol.cleanss.velocity.CleanSS;

public enum VelocityVersion {

    VERSION("version");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    VelocityVersion(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getVersionTextFile().getConfig().get(path));
    }

}