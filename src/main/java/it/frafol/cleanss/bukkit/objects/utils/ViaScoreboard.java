package it.frafol.cleanss.bukkit.objects.utils;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import fr.mrmicky.fastboard.FastBoard;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class ViaScoreboard {

    public void createViaSuspectScoreboard(Player player) {
        FastBoard board = new FastBoard(player) {
            @Override
            public boolean hasLinesMaxLength() {
                return Via.getAPI().getPlayerVersion(player.getUniqueId()) < ProtocolVersion.v1_13.getVersion();
            }
        };
        PlayerCache.getSuspectBoard().put(player.getUniqueId(), board);
    }

    public void createViaAdminScoreboard(Player player) {
        FastBoard board = new FastBoard(player) {
            @Override
            public boolean hasLinesMaxLength() {
                return Via.getAPI().getPlayerVersion(player.getUniqueId()) < ProtocolVersion.v1_13.getVersion();
            }
        };
        PlayerCache.getAdminBoard().put(player.getUniqueId(), board);
    }

    public void createViaOtherScoreboard(Player player) {
        FastBoard board = new FastBoard(player) {
            @Override
            public boolean hasLinesMaxLength() {
                return Via.getAPI().getPlayerVersion(player.getUniqueId()) < ProtocolVersion.v1_13.getVersion();
            }
        };
        PlayerCache.getOtherBoard().put(player.getUniqueId(), board);
    }
}
