package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.Placeholder;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class DebugCommand implements SimpleCommand {

    private final CleanSS instance;

    public DebugCommand(CleanSS instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {

        final CommandSource source = invocation.source();

        if (invocation.arguments().length != 0) {
            return;
        }

        if (instance.getContainer().getDescription().getVersion().isEmpty()) {
            return;
        }

        if (!(invocation instanceof Player)) {
            sendConsole(source);
            return;
        }

        VelocityMessages.DEBUG.sendList(source,
                new Placeholder("server_version", instance.getServer().getVersion().getVersion()),
                new Placeholder("plugin_version", instance.getContainer().getDescription().getVersion().get()),
                new Placeholder("mysql", getMySQL()),
                new Placeholder("discord", getDiscord()),
                new Placeholder("update_notifier", String.valueOf(VelocityConfig.UPDATE_CHECK.get(Boolean.class))));
    }

    private void sendConsole(CommandSource source) {
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7CleanScreenShare Informations"));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Version: §d" + instance.getContainer().getDescription().getVersion().get()));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Velocity: §d" + instance.getServer().getVersion().getVersion()));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7MySQL: §d" + getMySQL()));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Discord: §d" + getDiscord()));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Update Notifier: §d" + VelocityConfig.UPDATE_CHECK.get(Boolean.class)));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Control servers: "));

        Utils.getServerList(VelocityConfig.CONTROL.getStringList()).forEach(server -> {

            if (server.isEmpty()) {
                return;
            }

            if (Utils.getOnlineServers(Utils.getServerList(VelocityConfig.CONTROL.getStringList())).contains(server)) {
                source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7- §a" + server.get().getServerInfo().getName()));
                return;
            }

            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7- §c" + server.get().getServerInfo().getName()));
        });

        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Fallback servers: "));

        Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList()).forEach(server -> {

            if (server.isEmpty()) {
                return;
            }

            if (Utils.getOnlineServers(Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList())).contains(server)) {
                source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7- §a" + server.get().getServerInfo().getName()));
                return;
            }

            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7- §c" + server.get().getServerInfo().getName()));
        });

        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
    }

    private String getMySQL() {
        if (instance.getData() == null) {
            return "Not connected";
        } else {
            return "Connected";
        }
    }

    private String getDiscord() {
        if (instance.getJda().getJda() == null) {
            return "Not connected";
        } else {
            return "Connected";
        }
    }
}
