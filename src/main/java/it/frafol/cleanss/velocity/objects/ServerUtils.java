package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerUtils {
    public void connect(Player player, RegisteredServer proxyServer) {
            player.createConnectionRequest(proxyServer).fireAndForget();
    }
}
