package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.proxy.Player;
import ir.syrent.velocityvanish.velocity.VelocityVanish;
import it.frafol.cleanss.velocity.CleanSS;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class VelocityVanishUtils {

    public int getOnlinePlayers(CleanSS instance) {
        return instance.getServer().getAllPlayers().size() - VelocityVanish.instance.getVanishedPlayers().size();
    }

    public boolean isVanished(Player player) {
        return VelocityVanish.instance.getVanishedPlayers().contains(player.getUsername());
    }

    public boolean isVanished(String name) {
        return VelocityVanish.instance.getVanishedPlayers().contains(name);
    }
}
