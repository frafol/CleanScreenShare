package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.*;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SpectateCommand implements SimpleCommand {

    public final CleanSS plugin;

    public SpectateCommand(CleanSS plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {

        final CommandSource source = invocation.source();
        boolean luckperms = plugin.getServer().getPluginManager().getPlugin("luckperms").isPresent();

        if (Utils.isConsole(source)) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ONLY_PLAYERS.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        final Player player = (Player) source;

        if (!source.hasPermission(VelocityConfig.SPEC_PERMISSION.get(String.class))) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (invocation.arguments().length != 1) {
            VelocityMessages.USAGE.sendList(source, null,
					new Placeholder("%prefix%", VelocityMessages.PREFIX.color()));
            return;
        }

        if (plugin.useLimbo) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.LIMBO_ERROR.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        if (!plugin.getServer().getAllServers().toString().contains(invocation.arguments()[0])) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.INVALID_SERVER.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%server%", invocation.arguments()[0])));
            return;
        }

        if (invocation.arguments()[0].equalsIgnoreCase("finish") && PlayerCache.getSpectators().contains(player.getUniqueId())) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_SPECTATING.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            PlayerCache.getSpectators().remove(player.getUniqueId());
            fallback(player);
            return;
        }

        if (!plugin.getServer().getServer(invocation.arguments()[0]).isPresent()) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.INVALID_SERVER.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%server%", invocation.arguments()[0])));
            return;
        }

        final RegisteredServer server = plugin.getServer().getServer(invocation.arguments()[0]).get();

        if (!Utils.isInControlServer(server)) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.INVALID_SERVER.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%server%", invocation.arguments()[0])));
            return;
        }

        if (PlayerCache.getSpectators().contains(player.getUniqueId()) || PlayerCache.getAdministrator().contains(player.getUniqueId()) || PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.IN_CONTROL_ERROR.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.SPECTATING.color()
                .replace("%prefix%", VelocityMessages.PREFIX.color())
                .replace("%server%", invocation.arguments()[0])));
        PlayerCache.getSpectators().add(player.getUniqueId());
        player.createConnectionRequest(server).fireAndForget();

        String admin_prefix;
        String admin_suffix;
        String admin_displayname;

        if (luckperms) {

            final LuckPerms api = LuckPermsProvider.get();

            final User admin = api.getUserManager().getUser(player.getUniqueId());

            if (admin == null) {
                return;
            }

            final String prefix = admin.getCachedData().getMetaData().getPrefix();
            final String suffix = admin.getCachedData().getMetaData().getSuffix();
            final String displayname = admin.getCachedData().getMetaData().getPrimaryGroup();

            admin_prefix = prefix == null ? "" : prefix;
            admin_suffix = suffix == null ? "" : suffix;
            admin_displayname = displayname == null ? "" : displayname;

        } else {
            admin_prefix = "";
            admin_suffix = "";
            admin_displayname = "";
        }

        MessageUtil.sendDiscordSpectatorMessage(player, VelocityMessages.DISCORD_SPECTATOR.color()
                .replace("%staffer%", player.getUsername())
                .replace("%server%", server.getServerInfo().getName())
                .replace("%admingroup%", admin_displayname),
                VelocityMessages.DISCORD_SPECTATOR_THUMBNAIL.color());

        if (VelocityConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
            plugin.getServer().getAllPlayers().stream()
                    .filter(players -> players.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class)))
                    .forEach(players -> players.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.SPECT_ADMIN_NOTIFY.color()
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("%admin%", player.getUsername())
                            .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                            .replace("%adminsuffix%", ChatUtil.color(admin_suffix)))));
        }
    }

    private void fallback(Player player) {
        List<Optional<RegisteredServer>> servers = Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList());

        if (!VelocityConfig.DISABLE_PING.get(Boolean.class)) {
            servers = Utils.getOnlineServers(servers);
        }

        Optional<RegisteredServer> proxyServer = Utils.getBestServer(servers);

        if (!proxyServer.isPresent()) {
            return;
        }

        player.createConnectionRequest(proxyServer.get()).fireAndForget();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        final String[] strings = invocation.arguments();

        if (Utils.isConsole(invocation.source())) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        final List<String> servers = VelocityConfig.CONTROL.getStringList().stream()
                .filter(string -> strings.length != 1 || strings[0].isEmpty()
                        || string.toLowerCase().startsWith(strings[0].toLowerCase()))
                .collect(Collectors.toList());

        return CompletableFuture.completedFuture(servers);
    }
}
