package it.frafol.cleanss.bukkit.objects.utils;

import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@UtilityClass
public class NametagUtil {

    private final CleanSS instance = CleanSS.getInstance();

    @SuppressWarnings("deprecation")
    public void setTag(Player player) {

        if (!SpigotConfig.NAMETAG.get(Boolean.class)) return;
        Location hologramLocation = player.getLocation().clone().add(0, 2, 0);
        ArmorStand armorStand = (ArmorStand) hologramLocation.getWorld().spawnEntity(hologramLocation, EntityType.ARMOR_STAND);

        String prefix = SpigotConfig.NAMETAG_PREFIX.get(String.class);
        String suffix = SpigotConfig.NAMETAG_SUFFIX.get(String.class);

        if (instance.isPAPI()) {
            prefix = PlaceholderAPI.setPlaceholders(player, prefix);
            suffix = PlaceholderAPI.setPlaceholders(player, suffix);
        }

        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(prefix + player.getName() + suffix);
        armorStand.setSmall(true);

        armorStand.setPassenger(player);
        player.setCustomName("");
    }

    @SuppressWarnings("deprecation")
    public void removeTag(Player player) {
        if (!SpigotConfig.NAMETAG.get(Boolean.class)) return;
        if (player.getPassenger() != null) player.getPassenger().remove();
        player.setCustomName(player.getName());
    }
}
