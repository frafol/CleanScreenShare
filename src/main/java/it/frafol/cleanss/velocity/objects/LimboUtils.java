package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityLimbo;
import it.frafol.cleanss.velocity.handlers.LimboHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.file.BuiltInWorldFileType;
import net.elytrium.limboapi.api.file.WorldFile;
import net.elytrium.limboapi.api.player.GameMode;
import net.elytrium.limboapi.api.player.LimboPlayer;

@UtilityClass
public class LimboUtils {

    private static final CleanSS instance = CleanSS.getInstance();

    @Getter
    private Limbo limbo;

    public void disconnect(Player player, RegisteredServer proxyServer) {
        LimboPlayer limboPlayer = LimboHandler.limbo_players.get(player);
        limboPlayer.getScheduledExecutor().execute(() -> limboPlayer.disconnect(proxyServer));
    }

    @SneakyThrows
    public void loadLimbo() {
        instance.useLimbo = true;
        if (instance.getServer().getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).isPresent()) {

            LimboFactory factory = (LimboFactory) instance.getServer().getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).get();
            VirtualWorld world = factory.createVirtualWorld(
                    Dimension.valueOf("minecraft:" + VelocityLimbo.DIMENSION.get(String.class)),
                    VelocityLimbo.X.get(Integer.class),
                    VelocityLimbo.Y.get(Integer.class),
                    VelocityLimbo.Z.get(Integer.class),
                    (float) VelocityLimbo.YAW.get(Integer.class),
                    (float) VelocityLimbo.PITCH.get(Integer.class));

            if (VelocityLimbo.SCHEMATIC_USE.get(Boolean.class)) {
                WorldFile schematic;
                switch (VelocityLimbo.SCHEMATIC_TYPE.get(String.class)) {
                    case "SCHEMATIC":
                         schematic = factory.openWorldFile(
                                BuiltInWorldFileType.SCHEMATIC,
                                instance.getPath().resolve(VelocityLimbo.SCHEMATIC_FILE.get(String.class)));
                         break;
                    case "WORLDEDIT_SCHEM":
                        schematic = factory.openWorldFile(
                                BuiltInWorldFileType.WORLDEDIT_SCHEM,
                                instance.getPath().resolve(VelocityLimbo.SCHEMATIC_FILE.get(String.class)));
                        break;
                    case "STRUCTURE":
                        schematic = factory.openWorldFile(
                                BuiltInWorldFileType.STRUCTURE,
                                instance.getPath().resolve(VelocityLimbo.SCHEMATIC_FILE.get(String.class)));
                        break;
                    default:
                        schematic = null;
                }

                if (schematic != null) {
                    schematic.toWorld(
                            factory,
                            world,
                            VelocityLimbo.X.get(Integer.class), VelocityLimbo.Y.get(Integer.class), VelocityLimbo.Z.get(Integer.class));
                }
            }

            GameMode gameMode;
            switch (VelocityLimbo.GAMEMODE.get(String.class)) {
                case "survival":
                    gameMode = GameMode.SURVIVAL;
                    break;
                case "creative":
                    gameMode = GameMode.CREATIVE;
                    break;
                case "adventure":
                    gameMode = GameMode.ADVENTURE;
                    break;
                case "spectator":
                    gameMode = GameMode.SPECTATOR;
                    break;
                default:
                    instance.getLogger().warn("Invalid Limbo GameMode specified in limboapi.yml. Defaulting to survival.");
                    gameMode = GameMode.SURVIVAL;
            }

            limbo = factory.createLimbo(world)
                    .setName("CleanScreenShare")
                    .setShouldRejoin(true)
                    .setShouldRespawn(true)
                    .setGameMode(gameMode);
            instance.getLogger().info("LimboAPI hooked successfully!");
        }
    }

    public void spawnPlayerLimbo(Player player) {
        getLimbo().spawnPlayer(player, new LimboHandler(player, instance));
    }
}
