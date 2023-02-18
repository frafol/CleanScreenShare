package it.frafol.cleanss.bukkit.objects;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

@UtilityClass
public class PlayerCache {

    @Getter
    private final HashSet<UUID> no_chat = new HashSet<>();

    @Contract("_ -> new")
    public static @NotNull Location StringToLocation(@NotNull String line) {

        String[] loc = line.split(";");
        World world = Bukkit.getWorld(loc[0]);

        if (world == null) {
            world = Bukkit.createWorld(new WorldCreator(loc[0]));
        }

        return new Location(world, Double.parseDouble(loc[1]), Double.parseDouble(loc[2]), Double.parseDouble(loc[3]), Float.parseFloat(loc[4]), Float.parseFloat(loc[5]));
    }

    public static @NotNull String LocationToString(@NotNull Location location) {

        String world = location.getWorld().getName();
        String x = String.valueOf(location.getBlockX());
        String y = String.valueOf(location.getBlockY());
        String z = String.valueOf(location.getBlockZ());
        String yaw = String.valueOf(location.getYaw());
        String pitch = String.valueOf(location.getPitch());

        return world + ";" + x + ";" + y + ";" + z + ";" + yaw + ";" + pitch;
    }

}
