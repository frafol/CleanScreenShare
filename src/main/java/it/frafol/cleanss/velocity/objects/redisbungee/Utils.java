package it.frafol.cleanss.velocity.objects.redisbungee;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.ChatUtil;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.ServerUtils;
import it.frafol.cleanss.velocity.objects.TitleUtil;
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

    public void punishPlayer(UUID administrator, String suspicious, Player administrator_user, UUID suspect, RedisBungeeAPI redisBungeeAPI) {

        boolean luckperms = instance.getServer().getPluginManager().isLoaded("luckperms");
        String admin_group = "";
        String suspect_group = "";

        if (luckperms) {

            final LuckPerms api = LuckPermsProvider.get();

            final User admin = api.getUserManager().getUser(administrator_user.getUniqueId());
            final User suspect2 = api.getUserManager().getUser(suspect);

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
                    VelocityMessages.DISCORD_FINISHED_THUMBNAIL.get(String.class),
                    redisBungeeAPI);

            String admin_prefix;
            String admin_suffix;
            String sus_prefix;
            String sus_suffix;

            if (luckperms) {
                admin_suffix = getSuffix(administrator);
                admin_prefix = getPrefix(administrator);
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
                                .replace("%suspect%", redisBungeeAPI.getNameFromUuid(suspect))
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
                VelocityMessages.DISCORD_LEAVE_DURING_CONTROL_THUMBNAIL.get(String.class),
                redisBungeeAPI);

        String admin_prefix;
        String admin_suffix;
        String sus_prefix;
        String sus_suffix;

        if (luckperms) {
            admin_suffix = getSuffix(administrator);
            admin_prefix = getPrefix(administrator);
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
                            .replace("%suspect%", redisBungeeAPI.getNameFromUuid(suspect))
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

    public boolean isInControlServer(String server) {
        for (String string : VelocityConfig.CONTROL.getStringList()) {
            if (string.equals(server)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInControlServer(ServerInfo server) {
        for (String string : VelocityConfig.CONTROL.getStringList()) {
            if (string.equals(server.getName())) {
                return true;
            }
        }
        return false;
    }

    public String getPrefix(UUID uuid) {

        final LuckPerms api = LuckPermsProvider.get();
        final User user = api.getUserManager().getUser(uuid);

        if (user == null) {
            return null;
        }

        if (user.getCachedData().getMetaData().getPrefix() == null) {
            return "";
        }

        return ChatUtil.color(user.getCachedData().getMetaData().getPrefix());
    }

    public String getSuffix(UUID uuid) {

        final LuckPerms api = LuckPermsProvider.get();
        final User user = api.getUserManager().getUser(uuid);

        if (user == null) {
            return null;
        }

        if (user.getCachedData().getMetaData().getSuffix() == null) {
            return "";
        }

        return ChatUtil.color(user.getCachedData().getMetaData().getSuffix());
    }

    public void finishControl(UUID suspicious, Player administrator, RegisteredServer proxyServer, RedisBungeeAPI redisBungeeAPI) {

        if (suspicious == null || administrator == null) {
            return;
        }

        if (administrator.isActive()) {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious);
            PlayerCache.getRedisCouples().remove(administrator.getUniqueId(), suspicious);

            if (VelocityConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious, 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious, 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }

            if (isInControlServer(redisBungeeAPI.getServerNameFor(suspicious))) {

                if (!VelocityConfig.USE_DISCONNECT.get(Boolean.class)) {
                    // TODO: Send plugin message to send suspicious to lobby.
                } else {
                    MessageUtil.sendChannelMessage(suspicious, "DISCONNECT_NOW", redisBungeeAPI);
                }

                // TODO: Send plugin message to send end titles to sospicious & finish message.
                //TitleUtil.sendEndTitle(suspicious);
                //TitleUtil.sendAdminEndTitle(administrator, suspicious);
                //suspicious.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.FINISHSUS.color()
                //        .replace("%prefix%", VelocityMessages.PREFIX.color())));

                if (!administrator.getCurrentServer().isPresent()) {
                    if (!instance.useLimbo) {
                        return;
                    }
                }

                if (isInControlServer(administrator.getCurrentServer().get().getServer().getServerInfo())) {
                    if (!VelocityConfig.USE_DISCONNECT.get(Boolean.class) || instance.useLimbo) {
                        ServerUtils.connect(administrator, proxyServer);
                    } else {
                        MessageUtil.sendChannelMessage(administrator, "DISCONNECT_NOW");
                    }
                }
            }

        } else if (redisBungeeAPI.getPlayersOnline().contains(suspicious)) {

            PlayerCache.getSuspicious().remove(suspicious);
            PlayerCache.getAdministrator().remove(administrator.getUniqueId());

            if (VelocityConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious, 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious, 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }

            if (!VelocityConfig.USE_DISCONNECT.get(Boolean.class)) {
                // TODO: Send plugin message to send suspicious to lobby.
            } else {
                MessageUtil.sendChannelMessage(suspicious, "DISCONNECT_NOW", redisBungeeAPI);
            }

            // TODO: Send plugin message to send end titles to sospicious & finish message.
            //TitleUtil.sendEndTitle(suspicious);
            //TitleUtil.sendAdminEndTitle(administrator, suspicious);
            //suspicious.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.FINISHSUS.color()
            //        .replace("%prefix%", VelocityMessages.PREFIX.color())));
            PlayerCache.getRedisCouples().remove(administrator.getUniqueId());

        } else if (administrator.isActive()) {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious);

            if (VelocityConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious, 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious, 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }

            if (!VelocityConfig.USE_DISCONNECT.get(Boolean.class)) {
                ServerUtils.connect(administrator, proxyServer);
            } else {
                MessageUtil.sendChannelMessage(administrator, "DISCONNECT_NOW");
            }

            administrator.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.LEAVESUS.color()
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("%player%", redisBungeeAPI.getNameFromUuid(suspicious))));

            PlayerCache.getCouples().remove(administrator);

        } else {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious);
            PlayerCache.getCouples().remove(administrator);

            if (VelocityConfig.MYSQL.get(Boolean.class)) {
                instance.getData().setInControl(suspicious, 0);
                instance.getData().setInControl(administrator.getUniqueId(), 0);
            } else {
                PlayerCache.getIn_control().put(suspicious, 0);
                PlayerCache.getIn_control().put(administrator.getUniqueId(), 0);
            }
        }
    }

    public void startControl(UUID suspicious, Player administrator, RegisteredServer proxyServer, RedisBungeeAPI redisBungeeAPI) {

        String admin_prefix;
        String admin_suffix;
        String sus_prefix;
        String sus_suffix;

        boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();
        if (luckperms) {
            admin_suffix = getSuffix(administrator.getUniqueId());
            admin_prefix = getPrefix(administrator.getUniqueId());
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

        if (!administrator.getCurrentServer().isPresent()) {
            return;
        }

        if (administrator.getCurrentServer().get().getServer() != proxyServer) {
            ServerUtils.connect(administrator, proxyServer);

        } else {
            MessageUtil.sendChannelAdvancedMessage(administrator, suspicious, "ADMIN", redisBungeeAPI);

            if (administrator.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()) {
                MessageUtil.sendChannelMessage(administrator, "NO_CHAT");
            }
        }

        if (!Objects.equals(redisBungeeAPI.getServerNameFor(suspicious), proxyServer.getServerInfo().getName())) {
            // TODO: Send player to the control server with RedisBungee messages.
            //ServerUtils.connect(suspicious, proxyServer);
        } else {
            MessageUtil.sendChannelMessage(suspicious, "SUSPECT", redisBungeeAPI);

            // TODO: Send message to check if 1.19+ between RedisBungee's proxies and than send other message to spigot server
            //if (suspicious.getProtocolVersion().getProtocol() >= ProtocolVersion.getProtocolVersion(759).getProtocol()) {
            //    MessageUtil.sendChannelMessage(suspicious, "NO_CHAT", redisBungeeAPI);
            //}
        }

        PlayerCache.getAdministrator().add(administrator.getUniqueId());
        PlayerCache.getSuspicious().add(suspicious);
        PlayerCache.getRedisCouples().put(administrator.getUniqueId(), suspicious);

        if (VelocityConfig.MYSQL.get(Boolean.class)) {

            instance.getData().setInControl(suspicious, 1);
            instance.getData().setInControl(administrator.getUniqueId(), 1);

            if (instance.getData().getStats(administrator.getUniqueId(), "controls") != -1) {
                instance.getData().setControls(administrator.getUniqueId(), instance.getData().getStats(administrator.getUniqueId(), "controls") + 1);
            }

            if (instance.getData().getStats(suspicious, "suffered") != -1) {
                instance.getData().setControlsSuffered(suspicious, instance.getData().getStats(suspicious, "suffered") + 1);
            }

        } else {

            PlayerCache.getIn_control().put(suspicious, 1);
            PlayerCache.getIn_control().put(administrator.getUniqueId(), 1);

            if (PlayerCache.getControls().get(administrator.getUniqueId()) != null) {
                PlayerCache.getControls().put(administrator.getUniqueId(), PlayerCache.getControls().get(administrator.getUniqueId()) + 1);
            } else {
                PlayerCache.getControls().put(administrator.getUniqueId(), 1);
            }

            PlayerCache.getControls_suffered().merge(suspicious, 1, Integer::sum);
        }

        // TODO: Send RedisBungee message to send start titles to suspicious
        // TitleUtil.sendStartTitle(suspicious);
        // TitleUtil.sendAdminStartTitle(administrator, suspicious);

        if (VelocityConfig.CHECK_FOR_PROBLEMS.get(Boolean.class)) {
            Utils.checkForErrors(suspicious, administrator, proxyServer, redisBungeeAPI);
        }

        // TODO: Send admin message through RedisBungee messages.
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
                            .replace("%suspect%", redisBungeeAPI.getNameFromUuid(suspicious))
                            .replace("%adminprefix%", ChatUtil.color(finalAdmin_prefix1))
                            .replace("%adminsuffix%", ChatUtil.color(finalAdmin_suffix1))
                            .replace("%suspectprefix%", ChatUtil.color(finalSus_prefix1))
                            .replace("%suspectsuffix%", ChatUtil.color(finalSus_suffix1)))));
        }

        // TODO: Send start message through RedisBungee messages.
        //suspicious.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.MAINSUS.color()
        //        .replace("%prefix%", VelocityMessages.PREFIX.color())
        //        .replace("%administrator%", administrator.getUsername())
        //        .replace("%suspect%", suspicious.getUsername())
        //        .replace("%adminprefix%", ChatUtil.color(admin_prefix))
        //        .replace("%adminsuffix%", ChatUtil.color(admin_suffix))
        //        .replace("%suspectprefix%", ChatUtil.color(sus_prefix))
        //        .replace("%suspectsuffix%", ChatUtil.color(sus_suffix))));

        MessageUtil.sendButtons(administrator, suspicious, admin_prefix, admin_suffix, sus_prefix, sus_suffix, redisBungeeAPI);
    }

    private void checkForErrors(UUID suspicious, Player administrator, RegisteredServer proxyServer, RedisBungeeAPI redisBungeeAPI) {

        instance.getServer().getScheduler().buildTask(instance, () -> {

            if (instance.useLimbo) {
                PlayerCache.getNow_started_sus().remove(suspicious);
                return;
            }

            if (!(PlayerCache.getSuspicious().contains(suspicious) && PlayerCache.getAdministrator().contains(administrator.getUniqueId()))) {
                return;
            }

            if (!administrator.getCurrentServer().isPresent() || redisBungeeAPI.getServerNameFor(suspicious) == null) {
                return;
            }

            if (Objects.equals(redisBungeeAPI.getServerNameFor(suspicious), proxyServer.getServerInfo().getName()) && administrator.getCurrentServer().get().getServer().equals(proxyServer)) {
                return;
            }

            List<Optional<RegisteredServer>> servers = Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList());

            if (!VelocityConfig.DISABLE_PING.get(Boolean.class)) {
                servers = Utils.getOnlineServers(servers);
            }

            Optional<RegisteredServer> fallbackServer = Utils.getBestServer(servers);

            if (!fallbackServer.isPresent()) {
                // TODO: send disconnect packet to RedisBunngee proxies
                //suspicious.disconnect(LegacyComponentSerializer.legacy('§').deserialize("Your control server is not configured correctly or is crashed, please check the configuration file. " +
                //        "The Control cannot be handled!"));
                administrator.disconnect(LegacyComponentSerializer.legacy('§').deserialize("Your control server is not configured correctly or is crashed, please check the configuration file. " +
                        "The Control cannot be handled!"));
                return;
            }

            finishControl(suspicious, administrator, fallbackServer.get(), redisBungeeAPI);
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
