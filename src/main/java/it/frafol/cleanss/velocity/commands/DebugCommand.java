package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanss.velocity.CleanSS;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class DebugCommand implements SimpleCommand {

    private final CleanSS instance;

    public DebugCommand(CleanSS instance) {
        this.instance = instance;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {

        final CommandSource source = invocation.source();

        if (invocation.arguments().length != 0) {
            return;
        }

        if (!instance.getContainer().getDescription().getVersion().isPresent()) {
            return;
        }

        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7CleanScreenShare Informations"));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Version: §d" + instance.getContainer().getDescription().getVersion().get()));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Velocity: §d" + instance.getServer().getVersion().getVersion()));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7MySQL: §d" + getMySQL()));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));

    }

    private String getMySQL() {
        if (instance.getData() == null) {
            return "Not connected";
        } else {
            return "Connected";
        }
    }
}
