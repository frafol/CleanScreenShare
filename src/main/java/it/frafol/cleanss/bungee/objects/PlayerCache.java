package it.frafol.cleanss.bungee.objects;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

@UtilityClass
public class PlayerCache {

    @Getter
    private final HashSet<UUID> Suspicious = new HashSet<>();

    @Getter
    private final HashSet<UUID> Administrator = new HashSet<>();

    @Getter
    private final HashMap<ProxiedPlayer, ProxiedPlayer> couples = new HashMap<>();

    @Getter
    private final HashMap<UUID, Integer> controls = new HashMap<>();

    @Getter
    private final HashMap<UUID, Integer> controls_suffered = new HashMap<>();

    @Getter
    private final HashMap<UUID, Integer> in_control = new HashMap<>();

}
