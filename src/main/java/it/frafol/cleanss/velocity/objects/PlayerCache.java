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
    private final HashMap<Player, Player> couples = new HashMap<>();

}
