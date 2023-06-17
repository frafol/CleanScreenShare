package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class CommandListener {

    public final CleanSS instance;

    public CommandListener(CleanSS instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPlayerCommand(@NotNull CommandExecuteEvent event) {

        if (!(event.getCommandSource() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getCommandSource();

        if (!PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            return;
        }

        if (!(player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()
                && !instance.getUnsignedVelocityAddon())) {

            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        instance.getLogger().error("Unable to delete command for " + player.getUsername() + ". " +
                "This is a Velocity issue affecting Minecraft 1.19.1+ clients. " +
                "To fix this, please install https://github.com/4drian3d/UnSignedVelocity/releases/latest.");

    }

    @Subscribe
    public void onPlayerBanExecution(CommandExecuteEvent event) {

        if (!(event.getCommandSource() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getCommandSource();

        if (!PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            return;
        }

        if (PlayerCache.getIn_control().get(player.getUniqueId()) == null) {
            return;
        }

        for (String command : instance.getConfigTextFile().getConfig().getStringList("settings.slog.ban_commands")) {
            if ((event.getCommand().startsWith(command + " ")) && event.getCommand().contains(instance.getValue(PlayerCache.getCouples(), player).getUsername())) {
                PlayerCache.getBan_execution().add(player.getUniqueId());
                instance.getServer().getScheduler().buildTask(instance, () -> PlayerCache.getBan_execution().remove(player.getUniqueId())).delay(2L, TimeUnit.SECONDS).schedule();
            }
        }
    }
}
