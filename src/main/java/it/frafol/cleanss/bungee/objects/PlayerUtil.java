package it.frafol.cleanss.bungee.objects;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PlayerUtil {

    private final CleanSS instance = CleanSS.getInstance();

    public String getPlayersFormatted() {
        List<ProxiedPlayer> players = new ArrayList<>();
        List<ProxiedPlayer> staffers = new ArrayList<>();
        for (ProxiedPlayer player : instance.getProxy().getPlayers()) {
            if (player.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class))) {
                staffers.add(player);
                continue;
            }
            players.add(player);
        }
        return BungeeConfig.DISCORD_PLAYERS_FORMATTED.get(String.class)
                .replace("%players%", String.valueOf(players.size()))
                .replace("%staffers%", String.valueOf(staffers.size()))
                .replace("%allplayers%", String.valueOf(players.size() + staffers.size()));
    }
}
