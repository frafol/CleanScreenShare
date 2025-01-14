package it.frafol.cleanss.bungee.listeners;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CommandListener implements Listener {

    private final CleanSS instance = CleanSS.getInstance();
    private final ConcurrentHashMap<UUID, String> command = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ScheduledTask> commandRemove = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerCommand(ChatEvent event) {

        if (!event.isCommand()) {
            return;
        }

        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        if (event.getMessage().equalsIgnoreCase("/spawn")) {
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(TextComponent.fromLegacy(BungeeMessages.COMMAND_BLOCKED.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (PlayerCache.getIn_control().get(player.getUniqueId()) == null) {
            return;
        }

        if (!PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            return;
        }

        for (String command : BungeeConfig.BAN_COMMANDS.getStringList()) {
            if (event.getMessage().startsWith("/" + command + " ") && event.getMessage().contains(instance.getValue(PlayerCache.getCouples(), player).getName())) {
                PlayerCache.getBan_execution().add(player.getUniqueId());
                instance.getProxy().getScheduler().schedule(instance, () -> PlayerCache.getBan_execution().remove(player.getUniqueId()), 2L, TimeUnit.SECONDS);
            }
        }
    }

    @EventHandler
    public void onStaffCommand(ChatEvent event) {

        if (!event.isCommand()) return;
        if (!(event.getSender() instanceof ProxiedPlayer)) return;
        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        if (!PlayerCache.getAdministrator().contains(player.getUniqueId())) return;
        if (!BungeeConfig.COMMAND_REQUEST.get(Boolean.class)) return;

        if (command.get(player.getUniqueId()) != null) {
            if (command.get(player.getUniqueId()).equals(event.getMessage())) {
                unTask(player);
                return;
            }
        }

        for (String allowed : BungeeConfig.ALLOWED_COMMANDS.getStringList()) {
            if (event.getMessage().startsWith(allowed)) {
                return;
            }
        }

        event.setCancelled(true);
        command.put(player.getUniqueId(), event.getMessage());
        BaseComponent clickableMessageString = TextComponent.fromLegacy(BungeeMessages.COMMAND_REQUEST.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color()));
        TextComponent clickableMessage = new TextComponent(clickableMessageString);
        clickableMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, event.getMessage()));
        player.sendMessage(clickableMessage);
        task(player);
    }

    private void task(ProxiedPlayer player) {
        ScheduledTask task = instance.getProxy().getScheduler().schedule(instance, () -> {
            command.remove(player.getUniqueId());
            commandRemove.remove(player.getUniqueId());
        }, BungeeConfig.COMMAND_TIME.get(Integer.class), TimeUnit.SECONDS);
        commandRemove.put(player.getUniqueId(), task);
    }

    private void unTask(ProxiedPlayer player) {
        command.remove(player.getUniqueId());
        commandRemove.get(player.getUniqueId()).cancel();
        commandRemove.remove(player.getUniqueId());
    }
}
