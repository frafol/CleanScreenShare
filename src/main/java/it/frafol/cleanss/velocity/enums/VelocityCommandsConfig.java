package it.frafol.cleanss.velocity.enums;

import it.frafol.cleanss.velocity.CleanSS;

import java.util.List;

public enum VelocityCommandsConfig {

    SS_PLAYER("aliases.screenshare.main"),
    SS_FINISH("aliases.screenshare.finish"),
    SS_INFO("aliases.screenshare.info"),
    SS_SPECTATE("aliases.spectator.main");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    VelocityCommandsConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getAliasesTextFile().getConfig().get(path));
    }

    public List<String> getStringList() {
        return instance.getAliasesTextFile().getConfig().getStringList(path);
    }

}