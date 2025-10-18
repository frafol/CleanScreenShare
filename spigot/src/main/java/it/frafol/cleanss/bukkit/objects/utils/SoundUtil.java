package it.frafol.cleanss.bukkit.objects.utils;

import it.frafol.cleanss.bukkit.CleanSS;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@UtilityClass
public class SoundUtil {

    private final CleanSS instance = CleanSS.getInstance();

    public void playSound(Player player, String sound) {
        if (getSound(sound) != null) instance.getServer().getScheduler().runTaskLater(instance, () -> player.playSound(player.getLocation(), getSound(sound), 1L, 1L), 10L);
    }

    public Sound getSound(String sound) {
        try {
            return Sound.valueOf(sound.toUpperCase());
        } catch (Exception e) {
            instance.getLogger().severe("Unable to find the sound: " + sound + ".");
            return null;
        }
    }
}
