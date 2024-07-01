package it.frafol.cleanss.velocity.commands.rediscommands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.TextFile;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.Optional;

public class ReloadCommand implements SimpleCommand {

    public final CleanSS PLUGIN;

    public ReloadCommand(CleanSS plugin) {
        this.PLUGIN = plugin;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void execute(Invocation invocation) {

        final CommandSource source = invocation.source();

        if (!source.hasPermission(VelocityConfig.RELOAD_PERMISSION.get(String.class))) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        stopTasks();
        TextFile.reloadAll();
        restartBot();
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.RELOADED.color()
                .replace("%prefix%", VelocityMessages.PREFIX.color())));

        startTasks();

        if (!(source instanceof Player)) {
            return;
        }

        final Player sender = (Player) source;

        ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF("RELOAD");

        sender.getCurrentServer().ifPresent(sv ->
                sv.sendPluginMessage(CleanSS.channel_join, buf.toByteArray()));

    }

    private void startTasks() {
        List<Optional<RegisteredServer>> servers = Utils.getServerList(VelocityConfig.CONTROL.getStringList());
        List<Optional<RegisteredServer>> fallbacks = Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList());

        for (Optional<RegisteredServer> server : servers) {
            server.ifPresent(Utils::startTask);
        }

        for (Optional<RegisteredServer> fallback : fallbacks) {
            fallback.ifPresent(Utils::startTask);
        }
    }

    private void stopTasks() {
        List<Optional<RegisteredServer>> servers = Utils.getServerList(VelocityConfig.CONTROL.getStringList());
        List<Optional<RegisteredServer>> fallbacks = Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList());

        for (Optional<RegisteredServer> server : servers) {
            server.ifPresent(Utils::stopTask);
        }

        for (Optional<RegisteredServer> fallback : fallbacks) {
            fallback.ifPresent(Utils::stopTask);
        }
    }

    private void restartBot() {
        if (PLUGIN.getJda().getJda() != null) {
            PLUGIN.getJda().getJda().shutdown();
            PLUGIN.reloadDiscord();
        }
    }
}
