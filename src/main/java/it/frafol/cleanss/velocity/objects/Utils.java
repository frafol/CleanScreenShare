package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.handlers.DataHandler;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.*;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class Utils {

    private static final CleanSS instance = CleanSS.getInstance();
    private final HashMap<RegisteredServer, ScheduledTask> task = new HashMap<>();

    public void punishPlayer(UUID administrator, String suspicious, Player administrator_user, Player suspect) {

        boolean luckperms = instance.getServer().getPluginManager().isLoaded("luckperms");
        String admin_group = "";
        String suspect_group = "";

        if (luckperms) {

            final LuckPerms api = LuckPermsProvider.get();

            final User admin = api.getUserManager().getUser(administrator_user.getUniqueId());
            final User suspect2 = api.getUserManager().getUser(suspect.getUniqueId());

            if (admin == null || suspect2 == null) {
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

            final Group suspectgroup = api.getGroupManager().getGroup(suspect2.getPrimaryGroup());

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

        if (PlayerCache.getBan_execution().contains(administrator)) {

            MessageUtil.sendDiscordMessage(
                    suspect,
                    administrator_user,
                    VelocityMessages.DISCORD_FINISHED.get(String.class)
                            .replace("%suspectgroup%", suspect_group)
                            .replace("%admingroup%", admin_group),
                    VelocityMessages.CHEATER.get(String.class),
                    VelocityMessages.DISCORD_FINISHED_THUMBNAIL.get(String.class));

            String admin_prefix;
            String admin_suffix;
            String sus_prefix;
            String sus_suffix;

            if (luckperms) {
                admin_suffix = getSuffix(administrator_user);
                admin_prefix = getPrefix(administrator_user);
                sus_prefix = getPrefix(suspect);
                sus_suffix = getSuffix(suspect);
            } else {
                admin_prefix = "";
                admin_suffix = "";
                sus_prefix = "";
                sus_suffix = "";
            }

            if (VelocityConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
                instance.getServer().getAllPlayers().stream()
                        .filter(players -> players.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ADMIN_NOTIFY.color()
                                .replace("%prefix%", VelocityMessages.PREFIX.color())
                                .replace("%admin%", administrator_user.getUsername())
                                .replace("%suspect%", suspect.getUsername())
                                .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                                .replace("%adminsuffix%", ChatUtil.color(admin_suffix))
                                .replace("%suspectprefix%", ChatUtil.color(sus_prefix))
                                .replace("%suspectsuffix%", ChatUtil.color(sus_suffix))
                                .replace("%result%", VelocityMessages.CHEATER.color()))));
            }
            return;
        }

        MessageUtil.sendDiscordMessage(
                suspect,
                administrator_user,
                VelocityMessages.DISCORD_QUIT.get(String.class)
                        .replace("%suspectgroup%", suspect_group)
                        .replace("%admingroup%", admin_group),
                VelocityMessages.LEFT.get(String.class),
                VelocityMessages.DISCORD_LEAVE_DURING_CONTROL_THUMBNAIL.get(String.class));

        String admin_prefix;
        String admin_suffix;
        String sus_prefix;
        String sus_suffix;

        if (luckperms) {
            admin_suffix = getSuffix(administrator_user);
            admin_prefix = getPrefix(administrator_user);
            sus_prefix = getPrefix(suspect);
            sus_suffix = getSuffix(suspect);
        } else {
            admin_prefix = "";
            admin_suffix = "";
            sus_prefix = "";
            sus_suffix = "";
        }

        if (VelocityConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
            instance.getServer().getAllPlayers().stream()
                    .filter(players -> players.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class)))
                    .forEach(players -> players.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ADMIN_NOTIFY.color()
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("%admin%", administrator_user.getUsername())
                            .replace("%suspect%", suspect.getUsername())
                            .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                            .replace("%adminsuffix%", ChatUtil.color(admin_suffix))
                            .replace("%suspectprefix%", ChatUtil.color(sus_prefix))
                            .replace("%suspectsuffix%", ChatUtil.color(sus_suffix))
                            .replace("%result%", VelocityMessages.LEFT.color()))));
        }

        if (!VelocityConfig.SLOG_PUNISH.get(Boolean.class)) {
            return;
        }

        instance.getServer().getCommandManager().executeAsync(
                instance.getServer().getConsoleCommandSource(), 
                VelocityConfig.SLOG_COMMAND.get(String.class)
                        .replace("%player%", suspicious)
                        .replace("%admin%", administrator_user.getUsername()));

    }

    public boolean isInControlServer(RegisteredServer server) {
        for (String string : VelocityConfig.CONTROL.getStringList()) {
            if (string.equals(server.getServerInfo().getName())) {
                return true;
            }
        }
        return false;
    }

    public String getPrefix(Player player) {

        final LuckPerms api = LuckPermsProvider.get();
        final User user = api.getUserManager().getUser(player.getUniqueId());

        if (user == null) {
            return null;
        }

        if (user.getCachedData().getMetaData().getPrefix() == null) {
            return "";
        }

        return ChatUtil.color(user.getCachedData().getMetaData().getPrefix());
    }

    public String getSuffix(Player player) {

        final LuckPerms api = LuckPermsProvider.get();
        final User user = api.getUserManager().getUser(player.getUniqueId());

        if (user == null) {
            return null;
        }

        if (user.getCachedData().getMetaData().getSuffix() == null) {
            return "";
        }

        return ChatUtil.color(user.getCachedData().getMetaData().getSuffix());
    }

    public void finishControl(Player suspicious, Player administrator, RegisteredServer proxyServer) {

        if (suspicious == null || administrator == null) {
            return;
        }

        if (suspicious.isActive() && administrator.isActive()) {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());
            PlayerCache.getCouples().remove(administrator, suspicious);

            if (VelocityConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious.getUniqueId(), 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious.getUniqueId(), 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }

            if (!suspicious.getCurrentServer().isPresent()) {
                if (!instance.useLimbo) {
                    return;
                }
            }

            if (instance.useLimbo || isInControlServer(suspicious.getCurrentServer().get().getServer())) {

                TitleUtil.sendEndTitle(suspicious);
                TitleUtil.sendAdminEndTitle(administrator, suspicious);

                suspicious.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.FINISHSUS.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())));

                if (!VelocityConfig.USE_DISCONNECT.get(Boolean.class) || instance.useLimbo) {

                    if (instance.useLimbo) {
                        LimboUtils.disconnect(suspicious, proxyServer);
                    } else {
                        ServerUtils.connect(suspicious, proxyServer);
                    }

                } else {
                    MessageUtil.sendChannelMessage(suspicious, "DISCONNECT_NOW");
                }

                if (!administrator.getCurrentServer().isPresent()) {
                    if (!instance.useLimbo) {
                        return;
                    }
                }

                if (instance.useLimbo || isInControlServer(administrator.getCurrentServer().get().getServer())) {
                    if (!VelocityConfig.USE_DISCONNECT.get(Boolean.class) || instance.useLimbo) {

                        if (instance.useLimbo) {
                            LimboUtils.disconnect(administrator, proxyServer);
                        } else {
                            ServerUtils.connect(administrator, proxyServer);
                        }

                    } else {
                        MessageUtil.sendChannelMessage(administrator, "DISCONNECT_NOW");
                    }
                }
            }

        } else if (suspicious.isActive()) {

            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());
            PlayerCache.getAdministrator().remove(administrator.getUniqueId());

            if (VelocityConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious.getUniqueId(), 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious.getUniqueId(), 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }

            TitleUtil.sendEndTitle(suspicious);
            TitleUtil.sendAdminEndTitle(administrator, suspicious);

            suspicious.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.FINISHSUS.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));

            if (!VelocityConfig.USE_DISCONNECT.get(Boolean.class) || instance.useLimbo) {

                if (instance.useLimbo) {
                    LimboUtils.disconnect(suspicious, proxyServer);
                } else {
                    ServerUtils.connect(suspicious, proxyServer);
                }

            } else {
                MessageUtil.sendChannelMessage(suspicious, "DISCONNECT_NOW");
            }

            PlayerCache.getCouples().remove(administrator);

        } else if (administrator.isActive()) {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());

            if (VelocityConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious.getUniqueId(), 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious.getUniqueId(), 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }

            administrator.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.LEAVESUS.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", suspicious.getUsername())));

            if (!VelocityConfig.USE_DISCONNECT.get(Boolean.class) || instance.useLimbo) {

                if (instance.useLimbo) {
                    LimboUtils.disconnect(administrator, proxyServer);
                } else {
                    ServerUtils.connect(administrator, proxyServer);
                }

            } else {
                MessageUtil.sendChannelMessage(administrator, "DISCONNECT_NOW");
            }

            PlayerCache.getCouples().remove(administrator);

        } else {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());
            PlayerCache.getCouples().remove(administrator);

            if (VelocityConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious.getUniqueId(), 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious.getUniqueId(), 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }
        }
    }

    public void startControl(Player suspicious, Player administrator, RegisteredServer proxyServer) {

        String admin_prefix;
        String admin_suffix;
        String sus_prefix;
        String sus_suffix;

        boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();
        if (luckperms) {
            admin_suffix = getSuffix(administrator);
            admin_prefix = getPrefix(administrator);
            sus_suffix = getSuffix(suspicious);
            sus_prefix = getPrefix(suspicious);
        } else {
            admin_prefix = "";
            admin_suffix = "";
            sus_prefix = "";
            sus_suffix = "";
        }

        if (admin_prefix == null) {
            admin_prefix = "";
        }

        if (admin_suffix == null) {
            admin_suffix = "";
        }

        if (sus_prefix == null) {
            sus_prefix = "";
        }

        if (sus_suffix == null) {
            sus_suffix = "";
        }

        if (instance.useLimbo) {

            if (VelocityConfig.CHECK_FOR_PROBLEMS.get(Boolean.class)) {
                PlayerCache.getNow_started_sus().add(suspicious.getUniqueId());
            }

            PlayerCache.getAdministrator().add(administrator.getUniqueId());
            PlayerCache.getSuspicious().add(suspicious.getUniqueId());
            PlayerCache.getCouples().put(administrator, suspicious);

            if (VelocityConfig.MYSQL.get(Boolean.class)) {
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

            if (VelocityConfig.CHECK_FOR_PROBLEMS.get(Boolean.class)) {
                Utils.checkForErrors(suspicious, administrator, proxyServer);
            }

            if (VelocityConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
                String finalAdmin_prefix = admin_prefix;
                String finalAdmin_suffix = admin_suffix;
                String finalSus_prefix = sus_prefix;
                String finalSus_suffix = sus_suffix;
                instance.getServer().getAllPlayers().stream()
                        .filter(players -> players.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ADMIN_NOTIFY.color()
                                .replace("%prefix%", VelocityMessages.PREFIX.color())
                                .replace("%admin%", administrator.getUsername())
                                .replace("%suspect%", suspicious.getUsername())
                                .replace("%adminprefix%", ChatUtil.color(finalAdmin_prefix))
                                .replace("%adminsuffix%", ChatUtil.color(finalAdmin_suffix))
                                .replace("%suspectprefix%", ChatUtil.color(finalSus_prefix))
                                .replace("%suspectsuffix%", ChatUtil.color(finalSus_suffix)))));
            }

            suspicious.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.MAINSUS.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%administrator%", administrator.getUsername())
                    .replace("%suspect%", suspicious.getUsername())
                    .replace("%adminprefix%", admin_prefix)
                    .replace("%adminsuffix%", admin_suffix)
                    .replace("%suspectprefix%", sus_prefix)
                    .replace("%suspectsuffix%", sus_suffix)));

            MessageUtil.sendButtons(administrator, suspicious, admin_prefix, admin_suffix, sus_prefix, sus_suffix);
            return;
        }

        if (!administrator.getCurrentServer().isPresent()) {
            return;
        }

        if (!suspicious.getCurrentServer().isPresent()) {
            return;
        }

        if (administrator.getCurrentServer().get().getServer() != proxyServer) {
            ServerUtils.connect(administrator, proxyServer);

        } else {
            MessageUtil.sendChannelAdvancedMessage(administrator, suspicious, "ADMIN");

            if (administrator.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()) {
                MessageUtil.sendChannelMessage(administrator, "NO_CHAT");
            }
        }

        if (suspicious.getCurrentServer().get().getServer() != proxyServer) {
            ServerUtils.connect(suspicious, proxyServer);

        } else {
            MessageUtil.sendChannelMessage(suspicious, "SUSPECT");

            if (suspicious.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()) {
                MessageUtil.sendChannelMessage(suspicious, "NO_CHAT");
            }
        }

        PlayerCache.getAdministrator().add(administrator.getUniqueId());
        PlayerCache.getSuspicious().add(suspicious.getUniqueId());
        PlayerCache.getCouples().put(administrator, suspicious);

        if (VelocityConfig.MYSQL.get(Boolean.class)) {
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

        if (VelocityConfig.CHECK_FOR_PROBLEMS.get(Boolean.class)) {
            Utils.checkForErrors(suspicious, administrator, proxyServer);
        }

        if (VelocityConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
            String finalAdmin_prefix1 = admin_prefix;
            String finalAdmin_suffix1 = admin_suffix;
            String finalSus_prefix1 = sus_prefix;
            String finalSus_suffix1 = sus_suffix;
            instance.getServer().getAllPlayers().stream()
                    .filter(players -> players.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class)))
                    .forEach(players -> players.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ADMIN_NOTIFY.color()
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("%admin%", administrator.getUsername())
                            .replace("%suspect%", suspicious.getUsername())
                            .replace("%adminprefix%", ChatUtil.color(finalAdmin_prefix1))
                            .replace("%adminsuffix%", ChatUtil.color(finalAdmin_suffix1))
                            .replace("%suspectprefix%", ChatUtil.color(finalSus_prefix1))
                            .replace("%suspectsuffix%", ChatUtil.color(finalSus_suffix1)))));
        }

        suspicious.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.MAINSUS.color()
                .replace("%prefix%", VelocityMessages.PREFIX.color())
                .replace("%administrator%", administrator.getUsername())
                .replace("%suspect%", suspicious.getUsername())
                .replace("%adminprefix%", ChatUtil.color(admin_prefix))
                .replace("%adminsuffix%", ChatUtil.color(admin_suffix))
                .replace("%suspectprefix%", ChatUtil.color(sus_prefix))
                .replace("%suspectsuffix%", ChatUtil.color(sus_suffix))));

        MessageUtil.sendButtons(administrator, suspicious, admin_prefix, admin_suffix, sus_prefix, sus_suffix);
    }

    private void checkForErrors(Player suspicious, Player administrator, RegisteredServer proxyServer) {

        instance.getServer().getScheduler().buildTask(instance, () -> {

            if (instance.useLimbo) {
                PlayerCache.getNow_started_sus().remove(suspicious.getUniqueId());
                return;
            }

            if (!(PlayerCache.getSuspicious().contains(suspicious.getUniqueId()) && PlayerCache.getAdministrator().contains(administrator.getUniqueId()))) {
                return;
            }

            if (!(suspicious.getCurrentServer().isPresent() || administrator.getCurrentServer().isPresent())) {
                return;
            }

            if (suspicious.getCurrentServer().get().getServer().equals(proxyServer) && administrator.getCurrentServer().get().getServer().equals(proxyServer)) {
                return;
            }

            List<Optional<RegisteredServer>> servers = Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList());

            if (!VelocityConfig.DISABLE_PING.get(Boolean.class)) {
                servers = Utils.getOnlineServers(servers);
            }

            Optional<RegisteredServer> fallbackServer = Utils.getBestServer(servers);

            if (!fallbackServer.isPresent()) {
                suspicious.disconnect(LegacyComponentSerializer.legacy('§').deserialize("Your control server is not configured correctly or is crashed, please check the configuration file. " +
                        "The Control cannot be handled!"));
                administrator.disconnect(LegacyComponentSerializer.legacy('§').deserialize("Your control server is not configured correctly or is crashed, please check the configuration file. " +
                        "The Control cannot be handled!"));
                return;
            }

            Utils.finishControl(suspicious, administrator, fallbackServer.get());
            administrator.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_EXIST.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            instance.getLogger().error("Your control server is not configured correctly or is crashed, please check the configuration file. " +
                    "The Control cannot be handled!");

        }).delay(2L, TimeUnit.SECONDS).schedule();
    }

    public boolean isConsole(CommandSource invocation) {
        return !(invocation instanceof Player);
    }

    public List<Optional<RegisteredServer>> getServerList(List<String> stringList) {
        List<Optional<RegisteredServer>> servers = new ArrayList<>();
        for (String server : stringList) {

            if (!instance.getServer().getServer(server).isPresent()) {

                if (VelocityConfig.USE_DISCONNECT.get(Boolean.class)) {
                    continue;
                }

                instance.getLogger().error(VelocityMessages.NOT_VALID.color().replace("%server%", server));
                continue;
            }

            servers.add(instance.getServer().getServer(server));
        }
        return servers;
    }

    public Optional<RegisteredServer> getBestServer(List<Optional<RegisteredServer>> list) {

        if (list.isEmpty()) {
            return Optional.empty();
        }

        switch (VelocityConfig.STRATEGY.get(String.class)) {
            case "RANDOM":
                return getRandomServer(list);
            case "LEAST_PLAYERS":
                return getLeastPlayersServer(list);
            case "MOST_PLAYERS":
                return getMostPlayersServer(list);
            default:
                instance.getLogger().error("The strategy '" + VelocityConfig.STRATEGY.get(String.class) + "' is not valid, using 'RANDOM' instead.");
                return getRandomServer(list);
        }
    }

    public List<Optional<RegisteredServer>> getOnlineServers(List<Optional<RegisteredServer>> list) {
        List<Optional<RegisteredServer>> servers = new ArrayList<>();
        for (Optional<RegisteredServer> server : list) {

            if (!server.isPresent()) {
                return null;
            }

            if (PlayerCache.getOnlineServers().contains(server.get())) {
                servers.add(server);
            }
        }
        return servers;
    }

    public void startTask(RegisteredServer server) {
        taskServer(server);
    }

    public void stopTask(RegisteredServer server) {
        if (task.get(server) != null) {
            task.get(server).cancel();
        }

        task.remove(server);
        PlayerCache.getOnlineServers().remove(server);
    }

    private void taskServer(RegisteredServer server) {

        if (VelocityConfig.DISABLE_PING.get(Boolean.class)) {
            return;
        }

        task.put(server, instance.getServer().getScheduler().buildTask(instance, () ->
                server.ping().whenComplete((result, throwable) -> {

                    if (instance.getConfigTextFile() == null) {
                        return;
                    }

                    if (VelocityConfig.CONTROL_FALLBACK.getStringList().contains(server.getServerInfo().getName())
                            && VelocityConfig.USE_DISCONNECT.get(Boolean.class)) {
                        PlayerCache.getOnlineServers().add(server);
                        return;
                    }

                    if (throwable == null && result != null) {
                        PlayerCache.getOnlineServers().add(server);
                        return;
                    }

                    PlayerCache.getOnlineServers().remove(server);
                })
        ).repeat(VelocityConfig.PING_DELAY.get(Integer.class), TimeUnit.SECONDS).schedule());
    }

    private Optional<RegisteredServer> getLeastPlayersServer(List<Optional<RegisteredServer>> list) {
        Optional<RegisteredServer> server = Optional.empty();
        for (Optional<RegisteredServer> serverInfo : list) {
            if (!server.isPresent() || !serverInfo.isPresent()) {
                server = serverInfo;
            } else if (server.get().getPlayersConnected().size() > serverInfo.get().getPlayersConnected().size()) {
                server = serverInfo;
            }
        }
        return server;
    }

    private Optional<RegisteredServer> getMostPlayersServer(List<Optional<RegisteredServer>> list) {
        Optional<RegisteredServer> server = Optional.empty();
        for (Optional<RegisteredServer> serverInfo : list) {

            if (!server.isPresent()) {
                server = serverInfo;
                continue;
            }

            if (serverInfo.isPresent() && (server.get().getPlayersConnected().size() < serverInfo.get().getPlayersConnected().size())) {

                if (VelocityConfig.DISABLE_PING.get(Boolean.class)) {
                    server = serverInfo;
                    continue;
                }

                if (PlayerCache.getOnlineServers().contains(serverInfo.get())) {
                    server = serverInfo;
                }
            }
        }
        return server;
    }

    private Optional<RegisteredServer> getRandomServer(List<Optional<RegisteredServer>> list) {
        return list.get(new Random().nextInt(list.size()));
    }
}
