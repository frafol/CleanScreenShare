package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.ChatUtil;
import it.frafol.cleanss.velocity.objects.MessageUtil;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FinishCommand implements SimpleCommand {

    public final CleanSS instance;

    public FinishCommand(CleanSS instance) {
        this.instance = instance;
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {

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

                List<Optional<RegisteredServer>> servers = Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList());

                if (!VelocityConfig.DISABLE_PING.get(Boolean.class)) {
                    servers = Utils.getOnlineServers(servers);
                }

                Optional<RegisteredServer> proxyServer = Utils.getBestServer(servers);
                final Player sender = (Player) invocation.source();

                if (VelocityConfig.USE_DISCONNECT.get(Boolean.class)) {
                    proxyServer = Optional.empty();
                }

                if (!player.isPresent()) {
                    source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("%player%", invocation.arguments()[0])));
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

                    final Group admingroup = api.getGroupManager().getGroup(admin.getPrimaryGroup());

                    String admingroup_displayname;
                    if (admingroup != null) {
                        admingroup_displayname = admingroup.getFriendlyName();

                        if (admingroup_displayname.equalsIgnoreCase("default")) {
                            admingroup_displayname = VelocityMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
                        }

                    } else {
                        admingroup_displayname = "";
                    }

                    admin_group = admingroup == null ? "" : admingroup_displayname;

                    final Group suspectgroup = api.getGroupManager().getGroup(suspect.getPrimaryGroup());

                    String suspectroup_displayname;
                    if (suspectgroup != null) {
                        suspectroup_displayname = suspectgroup.getFriendlyName();

                        if (suspectroup_displayname.equalsIgnoreCase("default")) {
                            suspectroup_displayname = VelocityMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
                        }

                    } else {
                        suspectroup_displayname = "";
                    }

                    suspect_group = suspectgroup == null ? "" : suspectroup_displayname;

                }

                String admin_prefix;
                String admin_suffix;
                String sus_prefix;
                String sus_suffix;

                if (luckperms) {

                    final LuckPerms api = LuckPermsProvider.get();

                    final User admin = api.getUserManager().getUser(((Player) invocation.source()).getUniqueId());
                    final User suspect = api.getUserManager().getUser(player.get().getUniqueId());

                    if (admin == null) {
                        return;
                    }

                    if (suspect == null) {
                        return;
                    }

                    final String prefix1 = admin.getCachedData().getMetaData().getPrefix();
                    final String suffix1 = admin.getCachedData().getMetaData().getSuffix();

                    final String prefix2 = suspect.getCachedData().getMetaData().getPrefix();
                    final String suffix2 = suspect.getCachedData().getMetaData().getSuffix();

                    admin_prefix = prefix1 == null ? "" : prefix1;
                    admin_suffix = suffix1 == null ? "" : suffix1;

                    sus_prefix = prefix2 == null ? "" : prefix2;
                    sus_suffix = suffix2 == null ? "" : suffix2;

                } else {
                    admin_prefix = "";
                    admin_suffix = "";
                    sus_prefix = "";
                    sus_suffix = "";
                }

                if (VelocityConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
                    instance.getServer().getAllPlayers().stream()
                            .filter(players -> players.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class)))
                            .forEach(players -> players.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ADMIN_NOTIFY_FINISH.color()
                                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                                    .replace("%admin%", ((Player) invocation.source()).getUsername())
                                    .replace("%suspect%", player.get().getUsername())
                                    .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                                    .replace("%adminsuffix%", ChatUtil.color(admin_suffix))
                                    .replace("%suspectprefix%", ChatUtil.color(sus_prefix))
                                    .replace("%suspectsuffix%", ChatUtil.color(sus_suffix))
                                    .replace("%result%", VelocityMessages.CLEAN.color()))));
                }

                MessageUtil.sendDiscordMessage(
                        player.get(),
                        sender,
                        VelocityMessages.DISCORD_FINISHED.get(String.class)
                                .replace("%suspectgroup%", suspect_group)
                                .replace("%admingroup%", admin_group),
                        VelocityMessages.CLEAN.get(String.class),
                        VelocityMessages.DISCORD_FINISHED_THUMBNAIL.get(String.class));

            } else {

                source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", invocation.arguments()[0])));

            }
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        final String[] strings = invocation.arguments();

        if (Utils.isConsole(invocation.source())) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        final List<String> players = instance.getServer().getAllPlayers().stream()
                .map(Player::getUsername)
                .filter(player -> strings.length != 1 || strings[0].isEmpty()
                        || player.toLowerCase().startsWith(strings[0].toLowerCase()))
                .collect(Collectors.toList());

        return CompletableFuture.completedFuture(players);
    }
}
