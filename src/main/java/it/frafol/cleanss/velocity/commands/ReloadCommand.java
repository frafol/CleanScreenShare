package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.TextFile;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ReloadCommand implements SimpleCommand {

    public final CleanSS PLUGIN;

    public ReloadCommand(CleanSS plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {

        final CommandSource source = invocation.source();

        if (!source.hasPermission(VelocityConfig.RELOAD_PERMISSION.get(String.class))) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        TextFile.reloadAll();
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.RELOADED.color()
                .replace("%prefix%", VelocityMessages.PREFIX.color())));
    }
}
