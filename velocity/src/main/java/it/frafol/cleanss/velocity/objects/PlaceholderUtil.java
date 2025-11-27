package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.proxy.Player;
import lombok.experimental.UtilityClass;
import net.william278.papiproxybridge.api.PlaceholderAPI;

import java.util.concurrent.CompletableFuture;

@UtilityClass
public class PlaceholderUtil {

    public CompletableFuture<String> applyPlaceholders(String string, Player player) {
        PlaceholderAPI papi = PlaceholderAPI.createInstance();
        return papi.formatPlaceholders(string, player.getUniqueId());
    }
}
