package it.frafol.cleanss.bukkit.objects.utils;

import it.frafol.cleanss.bukkit.CleanSS;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@UtilityClass
public class SoundUtil {

    private final CleanSS instance = CleanSS.getInstance();

    public void playSound(Player player, String sound) {
        if (getSound(sound) != null) instance.getServer().getScheduler().runTaskLater(instance, () -> player.playSound(player.getLocation(), getSound(sound), 1, 0), 10L);
    }

    public Sound getSound(String sound) {
        try {
            return Sound.valueOf(sound);
        } catch (Exception e) {
            return null;
        }
    }
}
