package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityCommandsConfig;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CommandListener {

    public final CleanSS instance;

    public CommandListener(CleanSS instance) {
        this.instance = instance;
    }

    private final ConcurrentHashMap<UUID, String> command = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ScheduledTask> commandRemove = new ConcurrentHashMap<>();

    @Subscribe
    public void onPlayerCommand(CommandExecuteEvent event) {

        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }

        if (!PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            return;
        }

        if (event.getCommand().equalsIgnoreCase("spawn")) {
            return;
        }

        for (String admit_command : VelocityCommandsConfig.SS_ADMIT.getStringList()) {
            if (event.getCommand().equalsIgnoreCase(admit_command)) return;
        }

        if (!(player.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol() && !instance.getUnsignedVelocityAddon())) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
        }

        player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.COMMAND_BLOCKED.color()
                .replace("%prefix%", VelocityMessages.PREFIX.color())));
    }

    @Subscribe
    public void onStaffCommand(CommandExecuteEvent event) {

        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }

        if (!PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            return;
        }

        if (!VelocityConfig.COMMAND_REQUEST.get(Boolean.class)) {
            return;
        }

        if (command.get(player.getUniqueId()) != null) {
            if (command.get(player.getUniqueId()).equals(event.getCommand())) {
                unTask(player);
                return;
            }
        }

        for (String allowed : VelocityConfig.ALLOWED_COMMANDS.getStringList()) {
            if (event.getCommand().startsWith(allowed)) {
                return;
            }
        }

        event.setResult(CommandExecuteEvent.CommandResult.denied());
        command.put(player.getUniqueId(), event.getCommand());
        player.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.COMMAND_REQUEST.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color()))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + event.getCommand())));
        task(player);
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

    private void task(Player player) {
        ScheduledTask task = instance.getServer().getScheduler().buildTask(instance, () -> {
            command.remove(player.getUniqueId());
            commandRemove.remove(player.getUniqueId());
        }).delay(VelocityConfig.COMMAND_TIME.get(Integer.class), TimeUnit.SECONDS).schedule();
        commandRemove.put(player.getUniqueId(), task);
    }

    private void unTask(Player player) {
        if (command.get(player.getUniqueId()) != null) {
            command.remove(player.getUniqueId());
            commandRemove.get(player.getUniqueId()).cancel();
            commandRemove.remove(player.getUniqueId());
        }
    }
}
