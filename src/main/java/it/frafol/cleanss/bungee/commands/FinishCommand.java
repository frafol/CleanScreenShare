package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FinishCommand extends Command implements TabExecutor {

    public final CleanSS instance;

    public FinishCommand(CleanSS instance) {
        super("ssfinish","","screensharefinish","cleanssfinish","cleanscreensharefinish", "controlfinish");
        this.instance = instance;
    }

    @Override
    public void execute(@NotNull CommandSender invocation, String[] args) {

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
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.USAGE.color().replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (args.length == 1) {

            if (instance.getProxy().getPlayers().toString().contains(args[0])) {

                final ProxiedPlayer player = instance.getProxy().getPlayer(args[0]);
                final ServerInfo proxyServer = instance.getProxy().getServerInfo(BungeeConfig.CONTROL_FALLBACK.get(String.class));

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

                if (BungeeMessages.DISCORD_CAPITAL.get(Boolean.class)) {
                    Utils.sendDiscordMessage(player, (ProxiedPlayer) invocation, BungeeMessages.DISCORD_FINISHED.get(String.class).replace("%suspectgroup%", addCapital(suspect_group)).replace("%admingroup%", addCapital(admin_group)), BungeeMessages.CLEAN.get(String.class));
                    return;
                }

                Utils.sendDiscordMessage(player, (ProxiedPlayer) invocation, BungeeMessages.DISCORD_FINISHED.get(String.class).replace("%suspectgroup%", suspect_group).replace("%admingroup%", admin_group), BungeeMessages.CLEAN.get(String.class));

            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String @NotNull [] args) {

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

    private String addCapital(String string) {
        return (string.substring(0, 1).toUpperCase() + string.substring(1));
    }
}
