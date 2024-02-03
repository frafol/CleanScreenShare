package it.frafol.cleanss.bungee.enums;

import it.frafol.cleanss.bungee.CleanSS;

import java.util.List;

public enum BungeeCommandsConfig {

    SS_PLAYER("aliases.screenshare.main"),
    SS_FINISH("aliases.screenshare.finish"),
    SS_INFO("aliases.screenshare.info"),
    SS_SPECTATE("aliases.spectator.main");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    BungeeCommandsConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getAliasesTextFile().get(path));
    }

    public List<String> getStringList() {
        return instance.getAliasesTextFile().getStringList(path);
    }
}
