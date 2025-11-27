package it.frafol.cleanss.bungee.objects;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.william278.papiproxybridge.api.PlaceholderAPI;

import java.util.concurrent.CompletableFuture;

@UtilityClass
public class PlaceholderUtil {

    public CompletableFuture<String> applyPlaceholders(String string, ProxiedPlayer player) {
        PlaceholderAPI papi = PlaceholderAPI.createInstance();
        return papi.formatPlaceholders(string, player.getUniqueId());
    }
}
