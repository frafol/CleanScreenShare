package it.frafol.cleanss.bukkit.objects.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@UtilityClass
public class SoundUtil {

    public void playSound(Player player, String sound) {
        if (getSound(sound) != null) player.playSound(player.getLocation(), sound, 1, 0);
    }

    public Sound getSound(String sound) {
        Sound finalSound;
        try {
            finalSound = Sound.valueOf(sound);
            return finalSound;
        } catch (Exception e) {
            return null;
        }
    }
}
