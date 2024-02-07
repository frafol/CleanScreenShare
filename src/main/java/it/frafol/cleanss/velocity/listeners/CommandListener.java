package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.concurrent.TimeUnit;

public class CommandListener {

    public final CleanSS instance;

    public CommandListener(CleanSS instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPlayerCommand(CommandExecuteEvent event) {

        if (!(event.getCommandSource() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getCommandSource();

        if (!PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            return;
        }

        if (event.getCommand().equalsIgnoreCase("spawn")) {
            return;
        }

        if (!(player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol() && !instance.getUnsignedVelocityAddon())) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
        }

        player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.COMMAND_BLOCKED.color()
                .replace("%prefix%", VelocityMessages.PREFIX.color())));
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

        for (String command : VelocityConfig.BAN_COMMANDS.getStringList()) {
            if ((event.getCommand().startsWith(command + " ")) && event.getCommand().contains(instance.getValue(PlayerCache.getCouples(), player).getUsername())) {
                PlayerCache.getBan_execution().add(player.getUniqueId());
                instance.getServer().getScheduler().buildTask(instance, () -> PlayerCache.getBan_execution().remove(player.getUniqueId())).delay(2L, TimeUnit.SECONDS).schedule();
            }
        }
    }
}
