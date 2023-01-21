package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import org.jetbrains.annotations.NotNull;

public class CommandListener {

    @Subscribe
    public void onPlayerCommand(@NotNull CommandExecuteEvent event) {

        if (!(event.getCommandSource() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getCommandSource();

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {

            if (!(player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol())) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());
            }

        }

    }
}
