package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.proxy.Player;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import it.frafol.cleanss.velocity.CleanSS;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class PremiumVanishUtils {

    public int getOnlinePlayers(CleanSS instance) {
        return instance.getServer().getAllPlayers().size() - VelocityVanishAPI.getInvisiblePlayers().size();
    }

    public boolean isVanished(Player player) {
        return VelocityVanishAPI.getInvisiblePlayers().contains(player.getUniqueId());
    }

    public boolean isVanished(UUID uuid) {
        return VelocityVanishAPI.getInvisiblePlayers().contains(uuid);
    }
}
