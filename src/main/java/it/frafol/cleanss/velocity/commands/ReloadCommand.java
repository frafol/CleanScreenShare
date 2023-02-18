package it.frafol.cleanss.velocity.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.TextFile;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements SimpleCommand {

    public final CleanSS PLUGIN;

    public ReloadCommand(CleanSS plugin) {
        this.PLUGIN = plugin;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void execute(@NotNull Invocation invocation) {

        final CommandSource source = invocation.source();

        if (!(source instanceof Player)) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ONLY_PLAYERS.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        final Player sender = (Player) source;

        if (!source.hasPermission(VelocityConfig.RELOAD_PERMISSION.get(String.class))) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        TextFile.reloadAll();
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.RELOADED.color()
                .replace("%prefix%", VelocityMessages.PREFIX.color())));

        ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF("RELOAD");

        sender.getCurrentServer().ifPresent(sv ->
                sv.sendPluginMessage(CleanSS.channel_join, buf.toByteArray()));

    }
}
