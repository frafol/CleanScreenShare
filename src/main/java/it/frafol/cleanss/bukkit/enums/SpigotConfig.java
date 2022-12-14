package it.frafol.cleanss.bukkit.enums;

import it.frafol.cleanss.bukkit.CleanSS;

public enum SpigotConfig {

    STAFF_PERMISSION("options.staff_permission"),

    CHAT_ENABLE("chat.enable"),
    CHAT_FORMAT("chat.format"),

    BLOCK_COMMANDS("options.prevent.commands"),
    HUNGER("options.prevent.hunger"),
    VOID("options.prevent.void"),
    SPAWN("options.teleport_to_spawn_on_join"),
    GAMEMODE("options.change_gamemode_on_join"),

    SUSPECT("chat.states.suspect"),
    STAFFER("chat.states.staffer"),

    INVINCIBLE("options.invincible");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    SpigotConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().getConfig().get(path));
    }

    public String color() {
        return get(String.class).replace("&", "§");
    }

}