package it.frafol.cleanss.bukkit.listeners;

import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WorldListener  implements Listener {

    @EventHandler
    public void onWorldWeatherChange(WeatherChangeEvent event) {
        if (SpigotConfig.WEATHER.get(Boolean.class)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobSpawning(EntitySpawnEvent event) {

        if (event.getEntity() instanceof Player) {
            return;
        }

        if (SpigotConfig.MOB_SPAWNING.get(Boolean.class)) {
            event.setCancelled(true);
        }
    }
}
