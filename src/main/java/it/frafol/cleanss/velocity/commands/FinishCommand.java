package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FinishCommand implements SimpleCommand {

    public final CleanSS instance;

    public FinishCommand(CleanSS instance) {
        this.instance = instance;
    }

    @Override
    public void execute(SimpleCommand.@NotNull Invocation invocation) {

        final CommandSource source = invocation.source();
        boolean luckperms = instance.getServer().getPluginManager().isLoaded("luckperms");

        if (Utils.isConsole(source)) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ONLY_PLAYERS.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (!source.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class))) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (invocation.arguments().length == 0) {

            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.USAGE.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;

        }

        if (invocation.arguments().length == 1) {

            if (instance.getServer().getAllPlayers().toString().contains(invocation.arguments()[0])) {

                final Optional<Player> player = instance.getServer().getPlayer(invocation.arguments()[0]);

                final Optional<RegisteredServer> proxyServer;

                if (VelocityConfig.USE_DISCONNECT.get(Boolean.class)) {
                    proxyServer = instance.getServer().getServer(VelocityConfig.CONTROL.get(String.class));
                } else {
                    proxyServer = instance.getServer().getServer(VelocityConfig.CONTROL_FALLBACK.get(String.class));
                }

                final Player sender = (Player) invocation.source();

                if (!player.isPresent()) {
                    source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.get(String.class)
                            .replace("%prefix%", VelocityMessages.PREFIX.color())));
                    return;
                }

                if (!PlayerCache.getSuspicious().contains(player.get().getUniqueId())) {
                    source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_CONTROL.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
                    return;
                }

                if (instance.getValue(PlayerCache.getCouples(), sender) == null || instance.getValue(PlayerCache.getCouples(), sender) != player.get()) {
                    source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_CONTROL.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
                    return;
                }

                if (!proxyServer.isPresent()) {
                    return;
                }

                Utils.finishControl(player.get(), sender, proxyServer.get());

                String admin_group = "";
                String suspect_group = "";

                if (luckperms) {

                    final LuckPerms api = LuckPermsProvider.get();

                    final User admin = api.getUserManager().getUser(sender.getUniqueId());
                    final User suspect = api.getUserManager().getUser(player.get().getUniqueId());

                    if (admin == null || suspect == null) {
                        return;
                    }

                    final String admingroup = admin.getCachedData().getMetaData().getPrimaryGroup();
                    admin_group = admingroup == null ? "" : admingroup;

                    final String suspectgroup = suspect.getCachedData().getMetaData().getPrimaryGroup();
                    suspect_group = suspectgroup == null ? "" : suspectgroup;

                }

                Utils.sendDiscordMessage(player.get(), sender, VelocityMessages.DISCORD_FINISHED.get(String.class).replace("%suspectgroup%", suspect_group).replace("%admingroup%", admin_group), VelocityMessages.CLEAN.get(String.class));

            } else {

                source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", invocation.arguments()[0])));

            }
        }
    }
}
