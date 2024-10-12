package it.frafol.cleanss.velocity.enums;

import it.frafol.cleanss.velocity.CleanSS;

public enum VelocityLimbo {

    USE("limbo.use_limbo_api"),

    X("limbo.world_settings.coords.x"),
    Y("limbo.world_settings.coords.y"),
    Z("limbo.world_settings.coords.z"),
    YAW("limbo.world_settings.coords.yaw"),
    PITCH("limbo.world_settings.coords.pitch"),

    SCHEM_X("limbo.world_settings.schematic.paste_coords.x"),
    SCHEM_Y("limbo.world_settings.schematic.paste_coords.y"),
    SCHEM_Z("limbo.world_settings.schematic.paste_coords.z"),

    SCHEMATIC_USE("limbo.world_settings.schematic.use"),
    SCHEMATIC_TYPE("limbo.world_settings.schematic.type"),
    SCHEMATIC_FILE("limbo.world_settings.schematic.name"),

    GAMEMODE("limbo.world_settings.gamemode"),
    DIMENSION("limbo.world_settings.dimension");

    private final String path;
    public static final CleanSS instance = CleanSS.getInstance();

    VelocityLimbo(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getLimboTextFile().getConfig().get(path));
    }

}