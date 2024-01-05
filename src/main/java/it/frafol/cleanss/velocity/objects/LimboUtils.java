package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.handlers.LimboHandler;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.player.GameMode;
import net.elytrium.limboapi.api.player.LimboPlayer;

@UtilityClass
public class LimboUtils {

    private static final CleanSS instance = CleanSS.getInstance();

    @Getter
    private Limbo limbo;

    public void disconnect(Player player, RegisteredServer proxyServer) {
        LimboPlayer limboPlayer = LimboHandler.limbo_players.get(player);
        limboPlayer.disconnect(proxyServer);
    }

    public void loadLimbo() {
        instance.useLimbo = true;

        if (!instance.getServer().getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).isPresent()) {
            return;
        }

        LimboFactory factory = (LimboFactory) instance.getServer().getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).get();
        VirtualWorld world = factory.createVirtualWorld(Dimension.OVERWORLD, 0, 100, 0, (float) 90, (float) 0.0);
        limbo = factory.createLimbo(world)
                .setName("CleanScreenShare")
                .setShouldRejoin(true)
                .setShouldRespawn(true)
                .setGameMode(GameMode.ADVENTURE);
        instance.getLogger().info("LimboAPI hooked successfully!");
    }

    public void spawnPlayerLimbo(Player player) {
        getLimbo().spawnPlayer(player, new LimboHandler(player, instance));
    }

}
