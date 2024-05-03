package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class HelpCommand implements SimpleCommand {

    private final CleanSS instance;

    public HelpCommand(CleanSS instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {

        final CommandSource source = invocation.source();
        if (!source.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class))) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        VelocityMessages.USAGE.sendList(source, null,
                new Placeholder("%prefix%", VelocityMessages.PREFIX.color()));
    }
}
