package it.frafol.cleanss.bungee.objects;

import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.handlers.DataHandler;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@UtilityClass
public class Utils {

    private static final CleanSS instance = CleanSS.getInstance();
    public HashMap<ServerInfo, ScheduledTask> task = new HashMap<>();

    public void startControl(ProxiedPlayer suspicious, ProxiedPlayer administrator, ServerInfo proxyServer) {
        
        if (!Objects.equals(administrator.getServer().getInfo(), proxyServer)) {
            connect(administrator, proxyServer);
        } else {
            MessageUtil.sendChannelAdvancedMessage(administrator, suspicious,"ADMIN");
        }
        
        if (!Objects.equals(suspicious.getServer().getInfo(), proxyServer)) {
            connect(suspicious, proxyServer);
        } else {
            MessageUtil.sendChannelMessage(suspicious, "SUSPECT");
        }
        
        PlayerCache.getAdministrator().add(administrator.getUniqueId());
        PlayerCache.getSuspicious().add(suspicious.getUniqueId());
        PlayerCache.getCouples().put(administrator, suspicious);
        
        if (BungeeConfig.MYSQL.get(Boolean.class)) {
            instance.getData().setInControl(suspicious.getUniqueId(), 1);
            instance.getData().setInControl(administrator.getUniqueId(), 1);
            if (instance.getData().getStats(administrator.getUniqueId(), "controls") != -1) {
                instance.getData().setControls(administrator.getUniqueId(), instance.getData().getStats(administrator.getUniqueId(), "controls") + 1);
            }
            if (instance.getData().getStats(suspicious.getUniqueId(), "suffered") != -1) {
                instance.getData().setControlsSuffered(suspicious.getUniqueId(), instance.getData().getStats(suspicious.getUniqueId(), "suffered") + 1);
            }
        } else {
            PlayerCache.getIn_control().put(suspicious.getUniqueId(), 1);
            PlayerCache.getIn_control().put(administrator.getUniqueId(), 1);
            DataHandler.incrementDone(administrator.getUniqueId());
            DataHandler.incrementSuffered(suspicious.getUniqueId());
        }

        TitleUtil.sendStartTitle(suspicious);
        TitleUtil.sendAdminStartTitle(administrator, suspicious);

        if (BungeeConfig.CHECK_FOR_PROBLEMS.get(Boolean.class)) Utils.checkForErrors(suspicious, administrator, proxyServer);
        String admin_prefix;
        String admin_suffix;
        String sus_prefix;
        String sus_suffix;

        boolean luckperms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;
        if (luckperms) {
            sus_suffix = getSuffix(suspicious);
            sus_prefix = getPrefix(suspicious);
            admin_prefix = getPrefix(administrator);
            admin_suffix = getSuffix(administrator);
        } else {
            sus_suffix = "";
            sus_prefix = "";
            admin_suffix = "";
            admin_prefix = "";
        }

        if (BungeeConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
            instance.getProxy().getPlayers().stream()
                    .filter(players -> players.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class)))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMIN_NOTIFY.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("%admin%", administrator.getName())
                            .replace("%suspect%", suspicious.getName())
                            .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                            .replace("%adminsuffix%", ChatUtil.color(admin_suffix))
                            .replace("%suspectprefix%", ChatUtil.color(sus_prefix))
                            .replace("%suspectsuffix%", ChatUtil.color(sus_suffix)))));
        }

        suspicious.sendMessage(TextComponent.fromLegacy(BungeeMessages.MAINSUS.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())
                .replace("%administrator%", administrator.getName())
                .replace("%suspect%", suspicious.getName())
                .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                .replace("%adminsuffix%", ChatUtil.color(admin_suffix))
                .replace("%suspectprefix%", ChatUtil.color(sus_prefix))
                .replace("%suspectsuffix%", ChatUtil.color(sus_suffix))));

        MessageUtil.sendButtons(administrator, suspicious, admin_prefix, admin_suffix, sus_prefix, sus_suffix);
    }

    public void finishControl(ProxiedPlayer suspicious, ProxiedPlayer administrator, ServerInfo proxyServer) {

        if (administrator == null || suspicious == null) return;
        if (administrator.isConnected() && suspicious.isConnected()) {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());
            PlayerCache.getCouples().remove(administrator, suspicious);

            if (BungeeConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious.getUniqueId(), 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious.getUniqueId(), 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }

            if (administrator.getServer() == null) return;
            if (isInControlServer(administrator.getServer().getInfo())) {

                if (proxyServer == null) return;
                if (!BungeeConfig.USE_DISCONNECT.get(Boolean.class)) {
                    connect(administrator, proxyServer);
                } else {
                    MessageUtil.sendChannelMessage(administrator, "DISCONNECT_NOW");
                }

                TitleUtil.sendEndTitle(suspicious);
                TitleUtil.sendAdminEndTitle(administrator, suspicious);

                suspicious.sendMessage(TextComponent.fromLegacy(BungeeMessages.FINISHSUS.color().replace("%prefix%", BungeeMessages.PREFIX.color())));

                if (suspicious.getServer() == null) {
                    return;
                }

                if (isInControlServer(suspicious.getServer().getInfo())) {
                    connect(suspicious, proxyServer);
                }
            }

        } else if (suspicious.isConnected()) {

            if (instance.getValue(PlayerCache.getCouples(), administrator) == null) {
                return;
            }

            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());
            PlayerCache.getAdministrator().remove(administrator.getUniqueId());

            if (!BungeeConfig.USE_DISCONNECT.get(Boolean.class)) {
                connect(suspicious, proxyServer);
            } else {
                MessageUtil.sendChannelMessage(suspicious, "DISCONNECT_NOW");
            }

            TitleUtil.sendEndTitle(suspicious);
            TitleUtil.sendAdminEndTitle(administrator, suspicious);

            suspicious.sendMessage(TextComponent.fromLegacy(BungeeMessages.FINISHSUS.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));

            PlayerCache.getCouples().remove(administrator);

            if (BungeeConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious.getUniqueId(), 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious.getUniqueId(), 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }

        } else if (administrator.isConnected()) {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());

            if (!BungeeConfig.USE_DISCONNECT.get(Boolean.class)) {
                    connect(administrator, proxyServer);
            } else {
                MessageUtil.sendChannelMessage(administrator, "DISCONNECT_NOW");
            }

            administrator.sendMessage(TextComponent.fromLegacy(BungeeMessages.LEAVESUS.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                    .replace("%player%", suspicious.getName())));

            PlayerCache.getCouples().remove(administrator);

            if (BungeeConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious.getUniqueId(), 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious.getUniqueId(), 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }

        } else {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());
            PlayerCache.getCouples().remove(administrator);

            if (BungeeConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious.getUniqueId(), 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious.getUniqueId(), 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }
        }
    }

    public void punishPlayer(UUID administrator, String suspicious, ProxiedPlayer administrator_player, ProxiedPlayer suspect) {

        boolean luckperms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;

        String admin_group = "";
        String suspect_group = "";

        if (luckperms) {

            final LuckPerms api = LuckPermsProvider.get();

            final User admin = api.getUserManager().getUser(administrator_player.getUniqueId());
            final User suspect2 = api.getUserManager().getUser(suspect.getUniqueId());

            if (admin == null || suspect2 == null) {
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

            final Group suspectgroup = api.getGroupManager().getGroup(suspect2.getPrimaryGroup());

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

        if (PlayerCache.getBan_execution().contains(administrator)) {

            MessageUtil.sendDiscordMessage(
                    suspect,
                    administrator_player,
                    BungeeMessages.DISCORD_FINISHED.get(String.class).replace("%admingroup%", admin_group).replace("%suspectgroup%", suspect_group),
                    BungeeMessages.CHEATER.get(String.class),
                    BungeeMessages.DISCORD_FINISHED_THUMBNAIL.get(String.class));

            String admin_prefix;
            String admin_suffix;
            String sus_prefix;
            String sus_suffix;

            if (luckperms) {
                sus_suffix = getSuffix(suspect);
                sus_prefix = getPrefix(suspect);
                admin_prefix = getPrefix(administrator_player);
                admin_suffix = getSuffix(administrator_player);
            } else {
                sus_suffix = "";
                sus_prefix = "";
                admin_suffix = "";
                admin_prefix = "";
            }

            if (BungeeConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
                instance.getProxy().getPlayers().stream()
                        .filter(players -> players.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMIN_NOTIFY_FINISH.color()
                                .replace("%prefix%", BungeeMessages.PREFIX.color())
                                .replace("%admin%", administrator_player.getName())
                                .replace("%suspect%", suspect.getName())
                                .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                                .replace("%adminsuffix%", ChatUtil.color(admin_suffix))
                                .replace("%suspectprefix%", ChatUtil.color(sus_prefix))
                                .replace("%suspectsuffix%", ChatUtil.color(sus_suffix))
                                .replace("%result%", BungeeMessages.CHEATER.color()))));
            }
            return;
        }

        MessageUtil.sendDiscordMessage(
                suspect,
                administrator_player,
                BungeeMessages.DISCORD_QUIT.get(String.class).replace("%admingroup%", admin_group).replace("%suspectgroup%", suspect_group),
                BungeeMessages.LEFT.get(String.class),
                BungeeMessages.DISCORD_QUIT_THUMBNAIL.get(String.class));

        String admin_prefix;
        String admin_suffix;
        String sus_prefix;
        String sus_suffix;

        if (luckperms) {
            sus_suffix = getSuffix(suspect);
            sus_prefix = getPrefix(suspect);
            admin_prefix = getPrefix(administrator_player);
            admin_suffix = getSuffix(administrator_player);
        } else {
            sus_suffix = "";
            sus_prefix = "";
            admin_suffix = "";
            admin_prefix = "";
        }

        if (BungeeConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
            instance.getProxy().getPlayers().stream()
                    .filter(players -> players.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class)))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMIN_NOTIFY_FINISH.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("%admin%", administrator_player.getName())
                            .replace("%suspect%", suspect.getName())
                            .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                            .replace("%adminsuffix%", ChatUtil.color(admin_suffix))
                            .replace("%suspectprefix%", ChatUtil.color(sus_prefix))
                            .replace("%suspectsuffix%", ChatUtil.color(sus_suffix))
                            .replace("%result%", BungeeMessages.LEFT.color()))));
        }

        if (!BungeeConfig.SLOG_PUNISH.get(Boolean.class)) return;
        instance.getProxy().getPluginManager().dispatchCommand(
                instance.getProxy().getConsole(),
                BungeeConfig.SLOG_COMMAND.get(String.class)
                        .replace("%player%", suspicious)
                        .replace("%admin%", administrator_player.getName()));
    }

    public boolean isInControlServer(ServerInfo server) {
        for (String string : BungeeConfig.CONTROL.getStringList()) if (string.equals(server.getName())) return true;
        return false;
    }

    public String getPrefix(ProxiedPlayer player) {
        if (!isLuckPerms) return null;
        final LuckPerms api = LuckPermsProvider.get();
        final User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;
        String prefix = user.getCachedData().getMetaData().getPrefix();
        if (prefix == null) prefix = "";
        return prefix;
    }

    public String getSuffix(ProxiedPlayer player) {
        if (!isLuckPerms) return null;
        final LuckPerms api = LuckPermsProvider.get();
        final User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;
        String suffix = user.getCachedData().getMetaData().getSuffix();
        if (suffix == null) suffix = "";
        return suffix;
    }

    public String getGroup(ProxiedPlayer player) {
        if (!isLuckPerms) return null;
        final LuckPerms api = LuckPermsProvider.get();
        final User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;
        String group = user.getCachedData().getMetaData().getPrimaryGroup();
        if (group == null) group = "";
        return group;
    }

    public void sendAdmit(ProxiedPlayer suspect, ProxiedPlayer administrator) {
        suspect.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMITSUS.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())));
        administrator.sendMessage(TextComponent.fromLegacy(BungeeMessages.CONTROL_ADMIT_MESSAGE.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())
                .replace("%suspect%", suspect.getName())));
        if (VelocityMessages.CONTROL_ADMIT_RESENDBUTTONS.get(Boolean.class)) {
            String admin_prefix, admin_suffix, sus_prefix, sus_suffix, sus_group, admin_group;
            boolean luckperms = instance.getProxy().getPluginManager().getPlugin("luckperms") != null;
            if (luckperms) {
                sus_suffix = getSuffix(suspect);
                sus_prefix = getPrefix(suspect);
                sus_group = getGroup(suspect);
                admin_prefix = getPrefix(administrator);
                admin_suffix = getSuffix(administrator);
                admin_group = getGroup(administrator);
            } else {
                sus_suffix = "";
                sus_prefix = "";
                sus_group = "";
                admin_suffix = "";
                admin_group = "";
                admin_prefix = "";
            }
            MessageUtil.sendButtons(administrator, suspect, admin_prefix, admin_suffix, sus_prefix, sus_suffix);
            MessageUtil.sendDiscordMessage(
                    suspect,
                    administrator,
                    BungeeMessages.DISCORD_ADMIT.get(String.class).replace("%suspectgroup%", sus_group).replace("%admingroup%", admin_group),
                    BungeeMessages.DISCORD_ADMIT_THUMBNAIL.get(String.class));
        }
    }

    private void checkForErrors(ProxiedPlayer suspicious, ProxiedPlayer administrator, ServerInfo proxyServer) {
        instance.getProxy().getScheduler().schedule(instance, () -> {
            if (!(PlayerCache.getSuspicious().contains(suspicious.getUniqueId()) && PlayerCache.getAdministrator().contains(administrator.getUniqueId()))) return;
            if (suspicious.getServer().getInfo().equals(proxyServer) || administrator.getServer().getInfo().equals(proxyServer)) return;
            List<ServerInfo> servers = Utils.getServerList(BungeeConfig.CONTROL_FALLBACK.getStringList());
            if (!BungeeConfig.DISABLE_PING.get(Boolean.class)) servers = Utils.getOnlineServers(servers);
            final ServerInfo fallbackServer = Utils.getBestServer(servers);
            if (fallbackServer == null) {
                administrator.disconnect(TextComponent.fromLegacy("Your control server is not configured correctly or is crashed, please check the configuration file. " +
                        "The Control cannot be handled!"));
                suspicious.disconnect(TextComponent.fromLegacy("Your control server is not configured correctly or is crashed, please check the configuration file. " +
                        "The Control cannot be handled!"));
                return;
            }
            Utils.finishControl(suspicious, administrator, fallbackServer);
            administrator.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_EXIST.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            instance.getLogger().severe("Your control server is not configured correctly or is crashed, please check the configuration file. " +
                    "The Control cannot be handled!");
        }, 2L, TimeUnit.SECONDS);
    }

    public List<ServerInfo> getServerList(List<String> stringList) {
        List<ServerInfo> servers = new ArrayList<>();
        for (String server : stringList) {
            if (!instance.getProxy().getServers().containsKey(server)) {
                if (BungeeConfig.USE_DISCONNECT.get(Boolean.class)) continue;
                instance.getLogger().severe(BungeeMessages.NOT_VALID.color().replace("%server%", server));
                continue;
            }
            servers.add(instance.getProxy().getServerInfo(server));
        }
        return servers;
    }

    public ServerInfo getBestServer(List<ServerInfo> list) {
        if (list.isEmpty()) return null;
        switch (BungeeConfig.STRATEGY.get(String.class)) {
            case "RANDOM":
                return getRandomServer(list);
            case "LEAST_PLAYERS":
                return getLeastPlayersServer(list);
            case "MOST_PLAYERS":
                return getMostPlayersServer(list);
            default:
                instance.getLogger().severe("The strategy '" + BungeeConfig.STRATEGY.get(String.class) + "' is not valid, using 'RANDOM' instead.");
                return getRandomServer(list);
        }
    }

    public List<ServerInfo> getOnlineServers(List<ServerInfo> list) {
        List<ServerInfo> servers = new ArrayList<>();
        list.forEach(server -> {
            if (PlayerCache.getOnlineServers().contains(server)) {
                servers.add(server);
            }
        });
        return servers;
    }

    public void startTask(ServerInfo serverInfo) {
        taskServer(serverInfo);
    }

    public void stopTask(ServerInfo serverInfo) {
        if (task.get(serverInfo) != null) task.get(serverInfo).cancel();
        task.remove(serverInfo);
        PlayerCache.getOnlineServers().remove(serverInfo);
    }

    private void taskServer(ServerInfo server) {
        if (BungeeConfig.DISABLE_PING.get(Boolean.class)) return;
        task.put(server, instance.getProxy().getScheduler().schedule(instance, () -> server.ping((result, error) -> {
            if (CleanSS.getInstance() == null || instance.getConfigTextFile() == null) return;
            if (BungeeConfig.CONTROL_FALLBACK.getStringList().contains(server.getName())
                    && BungeeConfig.USE_DISCONNECT.get(Boolean.class)) {
                PlayerCache.getOnlineServers().add(server);
                return;
            }
            if (error == null && result != null) {
                PlayerCache.getOnlineServers().add(server);
                return;
            }
            PlayerCache.getOnlineServers().remove(server);
        }), 0L, (long) BungeeConfig.PING_DELAY.get(Integer.class), TimeUnit.SECONDS));
    }

    private ServerInfo getLeastPlayersServer(List<ServerInfo> list) {
        ServerInfo server = null;
        for (ServerInfo serverInfo : list) {
            if (server == null) {
                server = serverInfo;
            } else if (server.getPlayers().size() > serverInfo.getPlayers().size()) {
                server = serverInfo;
            }
        }
        return server;
    }

    private ServerInfo getMostPlayersServer(List<ServerInfo> list) {
        AtomicReference<ServerInfo> server = new AtomicReference<>(null);
        for (ServerInfo serverInfo : list) {
            if (server.get() == null) {
                server.set(serverInfo);
                continue;
            }
            if (server.get().getPlayers().size() < serverInfo.getPlayers().size()) {
                serverInfo.ping((result, error) -> {
                    if (error != null || result == null) return;
                    if (result.getPlayers().getMax() == result.getPlayers().getOnline()) return;
                    server.set(serverInfo);
                });
            }
        }
        return server.get();
    }

    private ServerInfo getRandomServer(List<ServerInfo> list) {
        return list.get(new Random().nextInt(list.size()));
    }

    private void connect(ProxiedPlayer player, ServerInfo server) {
        player.connect(server, ServerConnectEvent.Reason.PLUGIN);
    }

    public final boolean isLuckPerms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;
}
