package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import lombok.experimental.UtilityClass;

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
    private final HashSet<UUID> ban_execution = new HashSet<>();

    @Getter
    private final HashMap<Player, Player> couples = new HashMap<>();

    @Getter
    private final HashMap<UUID, Integer> controls = new HashMap<>();

    @Getter
    private final HashMap<UUID, Integer> controls_suffered = new HashMap<>();

    @Getter
    private final HashMap<UUID, Integer> in_control = new HashMap<>();

}
