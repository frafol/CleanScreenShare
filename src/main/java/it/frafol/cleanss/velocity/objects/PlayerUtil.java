package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PlayerUtil {

    private final CleanSS instance = CleanSS.getInstance();

    public String getPlayersFormatted() {
        List<Player> players = new ArrayList<>();
        List<Player> staffers = new ArrayList<>();
        for (Player player : instance.getServer().getAllPlayers()) {
            if (player.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class))) {
                staffers.add(player);
                continue;
            }
            players.add(player);
        }
        return VelocityConfig.DISCORD_PLAYERS_FORMATTED.get(String.class)
                .replace("%players%", String.valueOf(players.size()))
                .replace("%staffers%", String.valueOf(staffers.size()))
                .replace("%allplayers%", String.valueOf(players.size() + staffers.size()));
    }
}
