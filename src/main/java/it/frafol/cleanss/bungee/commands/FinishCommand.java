package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.*;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FinishCommand extends Command implements TabExecutor {

    public final CleanSS instance;

    public FinishCommand(CleanSS instance) {
        super(BungeeCommandsConfig.SS_FINISH.getStringList().get(0),"", BungeeCommandsConfig.SS_FINISH.getStringList().toArray(new String[0]));
        this.instance = instance;
    }

    @Override
    public void execute(CommandSender invocation, String[] args) {

        boolean luckperms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;

        if (!invocation.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class))) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (!(invocation instanceof ProxiedPlayer)) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.ONLY_PLAYERS.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (args.length == 0) {
            BungeeMessages.USAGE.sendList(invocation,
					new Placeholder("%prefix%", BungeeMessages.PREFIX.color()));
            return;
        }

        if (args.length == 1) {

            if (instance.getProxy().getPlayers().toString().contains(args[0])) {

                final ProxiedPlayer player = instance.getProxy().getPlayer(args[0]);

                List<ServerInfo> servers = Utils.getServerList(BungeeConfig.CONTROL_FALLBACK.getStringList());

                if (!BungeeConfig.DISABLE_PING.get(Boolean.class)) {
                    servers = Utils.getOnlineServers(servers);
                }

                final ServerInfo proxyServer = Utils.getBestServer(servers);

                if (player == null) {
                    invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_ONLINE.get(String.class)
                            .replace("%prefix%", BungeeMessages.PREFIX.color())));
                    return;
                }

                if (!player.isConnected()) {
                    invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_ONLINE.get(String.class)
                            .replace("%prefix%", BungeeMessages.PREFIX.color())));
                    return;
                }

                if (!PlayerCache.getSuspicious().contains(player.getUniqueId())) {
                    invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_CONTROL.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())));
                    return;
                }

                if (instance.getValue(PlayerCache.getCouples(), ((ProxiedPlayer) invocation)) == null || instance.getValue(PlayerCache.getCouples(), ((ProxiedPlayer) invocation)) != player) {
                    invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_CONTROL.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())));
                    return;
                }

                Utils.finishControl(player, (ProxiedPlayer) invocation, proxyServer);

                String admin_prefix;
                String admin_suffix;
                String sus_prefix;
                String sus_suffix;

                if (luckperms) {

                    final LuckPerms api = LuckPermsProvider.get();

                    final User admin = api.getUserManager().getUser(((ProxiedPlayer) invocation).getUniqueId());
                    final User suspect = api.getUserManager().getUser(player.getUniqueId());

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
                    sus_suffix = "";
                    sus_prefix = "";
                    admin_suffix = "";
                    admin_prefix = "";
                }

                if (BungeeConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
                    instance.getProxy().getPlayers().stream()
                            .filter(players -> players.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class)))
                            .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.ADMIN_NOTIFY_FINISH.color()
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("%admin%", invocation.getName())
                                    .replace("%suspect%", player.getName())
                                    .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                                    .replace("%adminsuffix%", ChatUtil.color(admin_suffix))
                                    .replace("%suspectprefix%", ChatUtil.color(sus_prefix))
                                    .replace("%suspectsuffix%", ChatUtil.color(sus_suffix))
                                    .replace("%result%", BungeeMessages.CLEAN.color()))));
                }

                String admin_group = "";
                String suspect_group = "";

                if (luckperms) {

                    final LuckPerms api = LuckPermsProvider.get();

                    final User admin = api.getUserManager().getUser(((ProxiedPlayer) invocation).getUniqueId());
                    final User suspect = api.getUserManager().getUser(player.getUniqueId());

                    if (admin == null || suspect == null) {
                        return;
                    }

                    final Group admingroup = api.getGroupManager().getGroup(admin.getPrimaryGroup());

                    String admingroup_displayname;
                    if (admingroup != null) {
                        admingroup_displayname = admingroup.getFriendlyName();

                        if (admingroup_displayname.equalsIgnoreCase("default")) {
                            admingroup_displayname = BungeeMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
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
                            suspectroup_displayname = BungeeMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
                        }

                    } else {
                        suspectroup_displayname = "";
                    }

                    suspect_group = suspectgroup == null ? "" : suspectroup_displayname;

                }

                MessageUtil.sendDiscordMessage(
                        player,
                        (ProxiedPlayer) invocation,
                        BungeeMessages.DISCORD_FINISHED.get(String.class).replace("%suspectgroup%", suspect_group).replace("%admingroup%", admin_group),
                        BungeeMessages.CLEAN.get(String.class),
                        BungeeMessages.DISCORD_FINISHED_THUMBNAIL.get(String.class));
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String [] args) {

        if (args.length != 1) {
            return Collections.emptyList();
        }

        String partialName = args[0].toLowerCase();

        List<String> completions = new ArrayList<>();
        for (ProxiedPlayer player : instance.getProxy().getPlayers()) {
            if (player.getName().toLowerCase().startsWith(partialName)) {
                completions.add(player.getName());
            }
        }

        return completions;
    }
}
