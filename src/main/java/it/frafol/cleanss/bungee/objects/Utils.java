package it.frafol.cleanss.bungee.objects;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class Utils {

    private static final CleanSS instance = CleanSS.getInstance();

    public HashMap<ServerInfo, ScheduledTask> task = new HashMap<>();

    public List<String> getStringList(@NotNull BungeeMessages velocityMessages) {
        return instance.getMessagesTextFile().getStringList(velocityMessages.getPath());
    }

    public List<String> getStringList(BungeeMessages velocityMessages, Placeholder... placeholders) {
        List<String> newList = new ArrayList<>();

        for (String s : getStringList(velocityMessages)) {
            s = applyPlaceHolder(s, placeholders);
            newList.add(s);
        }

        return newList;
    }

    public String applyPlaceHolder(String s, Placeholder @NotNull ... placeholders) {
        for (Placeholder placeholder : placeholders) {
            s = s.replace(placeholder.getKey(), placeholder.getValue());
        }

        return s;
    }

    public String color(String string) {

        if (string == null) {
            return null;
        }

        String hex = convertHexColors(string);
        return hex.replace("&", "§");
    }

    public static String convertHexColors(String str) {
        Pattern unicode = Pattern.compile("\\\\u\\+[a-fA-F0-9]{4}");
        Matcher match = unicode.matcher(str);
        while (match.find()) {
            String code = str.substring(match.start(),match.end());
            str = str.replace(code,Character.toString((char) Integer.parseInt(code.replace("\\u+",""),16)));
            match = unicode.matcher(str);
        }
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        match = pattern.matcher(str);
        while (match.find()) {
            String color = str.substring(match.start(),match.end());
            str = str.replace(color,ChatColor.of(color.replace("&","")) + "");
            match = pattern.matcher(str);
        }
        return ChatColor.translateAlternateColorCodes('&',str);
    }

    public List<String> color(@NotNull List<String> list) {
        return list.stream().map(Utils::color).collect(Collectors.toList());
    }

    public void sendList(CommandSender commandSource, @NotNull List<String> stringList, ProxiedPlayer player_name) {

        for (String message : stringList) {

            TextComponent suggestMessage = new TextComponent(message);

            if (message.contains(BungeeMessages.CONTROL_CLEAN_NAME.get(String.class))) {

                suggestMessage.setClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        BungeeMessages.CONTROL_CLEAN_COMMAND.get(String.class).replace("%player%", player_name.getName())));

                commandSource.sendMessage(suggestMessage);

            } else if (message.contains(BungeeMessages.CONTROL_CHEATER_NAME.get(String.class))) {

                suggestMessage.setClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        BungeeMessages.CONTROL_CHEATER_COMMAND.get(String.class).replace("%player%", player_name.getName())));

                commandSource.sendMessage(suggestMessage);

            } else if (message.contains(BungeeMessages.CONTROL_ADMIT_NAME.get(String.class))) {

                suggestMessage.setClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        BungeeMessages.CONTROL_ADMIT_COMMAND.get(String.class).replace("%player%", player_name.getName())));

                commandSource.sendMessage(suggestMessage);

            } else if (message.contains(BungeeMessages.CONTROL_REFUSE_NAME.get(String.class))) {

                suggestMessage.setClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        BungeeMessages.CONTROL_REFUSE_COMMAND.get(String.class).replace("%player%", player_name.getName())));

                commandSource.sendMessage(suggestMessage);

            } else {

                commandSource.sendMessage(TextComponent.fromLegacyText(message));

            }
        }
    }

    public void sendFormattedList(BungeeMessages velocityMessages, CommandSender commandSender, ProxiedPlayer player_name, Placeholder... placeholders) {
        sendList(commandSender, color(getStringList(velocityMessages, placeholders)), player_name);
    }

    public void sendDiscordSpectatorMessage(ProxiedPlayer player, String message) {

        if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class)) {

            if (instance.getJda() == null) {
                return;
            }

            final TextChannel channel = instance.getJda().getTextChannelById(BungeeConfig.DISCORD_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(BungeeConfig.DISCORD_EMBED_TITLE.get(String.class), null);

            embed.setDescription(message
                    .replace("%staffer%", player.getName()));

            embed.setColor(Color.RED);
            embed.setFooter(BungeeConfig.DISCORD_EMBED_FOOTER.get(String.class));

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    public void sendDiscordMessage(ProxiedPlayer suspect, ProxiedPlayer staffer, String message, String result) {

        if (BungeeConfig.DISCORD_ENABLED.get(Boolean.class)) {

            final TextChannel channel = instance.getJda().getTextChannelById(BungeeConfig.DISCORD_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(BungeeConfig.DISCORD_EMBED_TITLE.get(String.class), null);

            embed.setDescription(message
                    .replace("%suspect%", suspect.getName())
                    .replace("%staffer%", staffer.getName())
                    .replace("%result%", result));

            embed.setColor(Color.RED);
            embed.setFooter(BungeeConfig.DISCORD_EMBED_FOOTER.get(String.class));

            channel.sendMessageEmbeds(embed.build()).queue();

        }
    }

    public void sendDiscordMessage(ProxiedPlayer suspect, ProxiedPlayer staffer, String message) {

        if (BungeeConfig.DISCORD_ENABLED.get(Boolean.class)) {

            if (instance.getJda() == null) {
                return;
            }

            final TextChannel channel = instance.getJda().getTextChannelById(BungeeConfig.DISCORD_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(BungeeConfig.DISCORD_EMBED_TITLE.get(String.class), null);

            embed.setDescription(message
                    .replace("%suspect%", suspect.getName())
                    .replace("%staffer%", staffer.getName()));

            embed.setColor(Color.RED);
            embed.setFooter(BungeeConfig.DISCORD_EMBED_FOOTER.get(String.class));

            channel.sendMessageEmbeds(embed.build()).queue();

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

            Utils.sendDiscordMessage(suspect, administrator_player, BungeeMessages.DISCORD_FINISHED.get(String.class).replace("%admingroup%", admin_group).replace("%suspectgroup%", suspect_group), BungeeMessages.CHEATER.get(String.class));

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
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.ADMIN_NOTIFY_FINISH.color()
                                .replace("%prefix%", BungeeMessages.PREFIX.color())
                                .replace("%admin%", administrator_player.getName())
                                .replace("%suspect%", suspect.getName())
                                .replace("%adminprefix%", Utils.color(admin_prefix))
                                .replace("%adminsuffix%", Utils.color(admin_suffix))
                                .replace("%suspectprefix%", Utils.color(sus_prefix))
                                .replace("%suspectsuffix%", Utils.color(sus_suffix))
                                .replace("%result%", BungeeMessages.CHEATER.color()))));
            }

            return;
        }

        Utils.sendDiscordMessage(suspect, administrator_player, BungeeMessages.DISCORD_QUIT.get(String.class).replace("%admingroup%", admin_group).replace("%suspectgroup%", suspect_group), BungeeMessages.LEFT.get(String.class));

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
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.ADMIN_NOTIFY_FINISH.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("%admin%", administrator_player.getName())
                            .replace("%suspect%", suspect.getName())
                            .replace("%adminprefix%", Utils.color(admin_prefix))
                            .replace("%adminsuffix%", Utils.color(admin_suffix))
                            .replace("%suspectprefix%", Utils.color(sus_prefix))
                            .replace("%suspectsuffix%", Utils.color(sus_suffix))
                            .replace("%result%", BungeeMessages.LEFT.color()))));
        }

        if (!BungeeConfig.SLOG_PUNISH.get(Boolean.class)) {
            return;
        }

        instance.getProxy().getPluginManager().dispatchCommand(instance.getProxy().getConsole(), BungeeConfig.SLOG_COMMAND.get(String.class).replace("%player%", suspicious));
    }

    public void finishControl(@NotNull ProxiedPlayer suspicious, @NotNull ProxiedPlayer administrator, ServerInfo proxyServer) {

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

            if (administrator.getServer() == null) {
                return;
            }

            if (isInControlServer(administrator.getServer().getInfo())) {

                if (proxyServer == null) {
                    return;
                }

                if (!BungeeConfig.USE_DISCONNECT.get(Boolean.class)) {
                    connect(administrator, proxyServer);
                } else {
                    Utils.sendChannelMessage(administrator, "DISCONNECT_NOW");
                }

                Utils.sendEndTitle(suspicious);
                Utils.sendAdminEndTitle(administrator, suspicious);

                suspicious.sendMessage(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color().replace("%prefix%", BungeeMessages.PREFIX.color())));

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

            if (suspicious.getUniqueId() != null) {
                PlayerCache.getSuspicious().remove(suspicious.getUniqueId());
            }

            if (administrator.getUniqueId() != null) {
                PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            }

            if (!BungeeConfig.USE_DISCONNECT.get(Boolean.class)) {
                connect(suspicious, proxyServer);
            } else {
                Utils.sendChannelMessage(suspicious, "DISCONNECT_NOW");
            }

            Utils.sendEndTitle(suspicious);
            Utils.sendAdminEndTitle(administrator, suspicious);

            suspicious.sendMessage(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color()
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
                Utils.sendChannelMessage(administrator, "DISCONNECT_NOW");
            }

            administrator.sendMessage(TextComponent.fromLegacyText(BungeeMessages.LEAVESUS.color()
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

    public boolean isInControlServer(ServerInfo server) {
        for (String string : BungeeConfig.CONTROL.getStringList()) {
            if (string.equals(server.getName())) {
                return true;
            }
        }
        return false;
    }

    public String getPrefix(ProxiedPlayer player) {

        if (!isLuckPerms) {
            return null;
        }

        final LuckPerms api = LuckPermsProvider.get();
        final User user = api.getUserManager().getUser(player.getUniqueId());

        if (user == null) {
            return null;
        }

        String prefix = user.getCachedData().getMetaData().getPrefix();

        if (prefix == null) {
            prefix = "";
        }

        return prefix;
    }

    public String getSuffix(ProxiedPlayer player) {

        if (!isLuckPerms) {
            return null;
        }

        final LuckPerms api = LuckPermsProvider.get();
        final User user = api.getUserManager().getUser(player.getUniqueId());

        if (user == null) {
            return null;
        }

        String suffix = user.getCachedData().getMetaData().getSuffix();

        if (suffix == null) {
            suffix = "";
        }

        return suffix;
    }

    public void startControl(@NotNull ProxiedPlayer suspicious, @NotNull ProxiedPlayer administrator, ServerInfo proxyServer) {

        if (!Objects.equals(administrator.getServer().getInfo(), proxyServer)) {
            connect(administrator, proxyServer);
        } else {
            Utils.sendChannelAdvancedMessage(administrator, suspicious,"ADMIN");
        }

        if (!Objects.equals(suspicious.getServer().getInfo(), proxyServer)) {
            connect(suspicious, proxyServer);

        } else {
            Utils.sendChannelMessage(suspicious, "SUSPECT");
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

            if (PlayerCache.getControls().get(administrator.getUniqueId()) != null) {
                PlayerCache.getControls().put(administrator.getUniqueId(), PlayerCache.getControls().get(administrator.getUniqueId()) + 1);
            } else {
                PlayerCache.getControls().put(administrator.getUniqueId(), 1);
            }

            if (PlayerCache.getControls_suffered().get(suspicious.getUniqueId()) != null) {
                PlayerCache.getControls_suffered().put(suspicious.getUniqueId(), PlayerCache.getControls_suffered().get(suspicious.getUniqueId()) + 1);
            } else {
                PlayerCache.getControls_suffered().put(suspicious.getUniqueId(), 1);
            }

        }

        Utils.sendStartTitle(suspicious);
        Utils.sendAdminStartTitle(administrator, suspicious);

        if (BungeeConfig.CHECK_FOR_PROBLEMS.get(Boolean.class)) {
            Utils.checkForErrors(suspicious, administrator, proxyServer);
        }

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
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.ADMIN_NOTIFY.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("%admin%", administrator.getName())
                            .replace("%suspect%", suspicious.getName())
                            .replace("%adminprefix%", color(admin_prefix))
                            .replace("%adminsuffix%", color(admin_suffix))
                            .replace("%suspectprefix%", color(sus_prefix))
                            .replace("%suspectsuffix%", color(sus_suffix)))));
        }

        suspicious.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MAINSUS.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())
                .replace("%administrator%", administrator.getName())
                .replace("%suspect%", suspicious.getName())
                .replace("%adminprefix%", color(admin_prefix))
                .replace("%adminsuffix%", color(admin_suffix))
                .replace("%suspectprefix%", color(sus_prefix))
                .replace("%suspectsuffix%", color(sus_suffix))));

        BungeeMessages.CONTROL_FORMAT.sendList(administrator, suspicious,
                new Placeholder("cleanname", BungeeMessages.CONTROL_CLEAN_NAME.color()),
                new Placeholder("hackername", BungeeMessages.CONTROL_CHEATER_NAME.color()),
                new Placeholder("admitname", BungeeMessages.CONTROL_ADMIT_NAME.color()),
                new Placeholder("refusename", BungeeMessages.CONTROL_REFUSE_NAME.color()),
                new Placeholder("prefix", BungeeMessages.PREFIX.color()),
                new Placeholder("adminprefix", color(admin_prefix)),
                new Placeholder("adminsuffix", color(admin_suffix)),
                new Placeholder("suspectprefix", color(sus_prefix)),
                new Placeholder("suspectsuffix", color(sus_suffix)),
                new Placeholder("suspect", suspicious.getName()),
                new Placeholder("administrator", administrator.getName()));

    }

    private void checkForErrors(ProxiedPlayer suspicious, ProxiedPlayer administrator, ServerInfo proxyServer) {

        instance.getProxy().getScheduler().schedule(instance, () -> {

            if (!(PlayerCache.getSuspicious().contains(suspicious.getUniqueId()) && PlayerCache.getAdministrator().contains(administrator.getUniqueId()))) {
                return;
            }

            if (suspicious.getServer().getInfo().equals(proxyServer) || administrator.getServer().getInfo().equals(proxyServer)) {
                return;
            }

            List<ServerInfo> servers = Utils.getServerList(BungeeConfig.CONTROL_FALLBACK.getStringList());

            if (!BungeeConfig.DISABLE_PING.get(Boolean.class)) {
                servers = Utils.getOnlineServers(servers);
            }

            final ServerInfo fallbackServer = Utils.getBestServer(servers);

            Utils.finishControl(suspicious, administrator, fallbackServer);
            administrator.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_EXIST.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            instance.getLogger().severe("Your control server is not configured correctly or is crashed, please check the configuration file. " +
                    "The Control cannot be handled!");

        }, 2L, TimeUnit.SECONDS);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendChannelMessage(@NotNull ProxiedPlayer player, String type) {

        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();

        buf.writeUTF(type);
        buf.writeUTF(player.getName());

        if (player.getServer() == null) {
            instance.getLogger().severe("The player " + player.getName() + " has no server, please check your control server if it's working correctly!");
            return;
        }

        player.getServer().sendData("cleanss:join", buf.toByteArray());

    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendChannelAdvancedMessage(@NotNull ProxiedPlayer administrator, ProxiedPlayer suspicious, String type) {

        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();

        buf.writeUTF(type);
        buf.writeUTF(administrator.getName());
        buf.writeUTF(suspicious.getName());

        if (administrator.getServer() == null) {
            instance.getLogger().severe("The player " + administrator.getName() + " has no server, please check your control server if it's working correctly!");
            return;
        }

        administrator.getServer().sendData("cleanss:join", buf.toByteArray());
    }

    private void sendStartTitle(ProxiedPlayer suspicious) {

        if (!BungeeMessages.CONTROL_USETITLE.get(Boolean.class)) {
            return;
        }

        final Title title = ProxyServer.getInstance().createTitle();

        title.fadeIn(BungeeMessages.CONTROL_FADEIN.get(Integer.class) * 20);
        title.stay(BungeeMessages.CONTROL_STAY.get(Integer.class) * 20);
        title.fadeOut(BungeeMessages.CONTROL_FADEOUT.get(Integer.class) * 20);

        title.title(new TextComponent(BungeeMessages.CONTROL_TITLE.color()));
        title.subTitle(new TextComponent(BungeeMessages.CONTROL_SUBTITLE.color()));

        ProxyServer.getInstance().getScheduler().schedule(instance, () ->
                title.send(suspicious), BungeeMessages.CONTROL_DELAY.get(Integer.class), TimeUnit.SECONDS);

    }

    private void sendAdminStartTitle(ProxiedPlayer administrator, ProxiedPlayer suspicious) {

        if (!BungeeMessages.ADMINCONTROL_USETITLE.get(Boolean.class)) {
            return;
        }

        final Title title = ProxyServer.getInstance().createTitle();

        title.fadeIn(BungeeMessages.ADMINCONTROL_FADEIN.get(Integer.class) * 20);
        title.stay(BungeeMessages.ADMINCONTROL_STAY.get(Integer.class) * 20);
        title.fadeOut(BungeeMessages.ADMINCONTROL_FADEOUT.get(Integer.class) * 20);

        String user_prefix = "";
        String user_suffix = "";

        if (isLuckPerms) {

            final LuckPerms api = LuckPermsProvider.get();
            final User user = api.getUserManager().getUser(suspicious.getUniqueId());

            if (user == null) {
                return;
            }

            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            user_prefix = prefix == null ? "" : prefix;
            user_suffix = suffix == null ? "" : suffix;

        }

        title.title(new TextComponent(BungeeMessages.ADMINCONTROL_TITLE.color()
                .replace("%suspect%", suspicious.getName())
                .replace("%suspectprefix%", user_prefix)
                .replace("%suspectsuffix%", user_suffix)));

        title.subTitle(new TextComponent(BungeeMessages.ADMINCONTROL_SUBTITLE.color()
                .replace("%suspect%", suspicious.getName())
                .replace("%suspectprefix%", user_prefix)
                .replace("%suspectsuffix%", user_suffix)));

        ProxyServer.getInstance().getScheduler().schedule(instance, () ->
                title.send(administrator), BungeeMessages.ADMINCONTROL_DELAY.get(Integer.class), TimeUnit.SECONDS);

    }

    private void sendEndTitle(ProxiedPlayer suspicious) {

        if (!BungeeMessages.CONTROLFINISH_USETITLE.get(Boolean.class)) {
            return;
        }

        final Title title = ProxyServer.getInstance().createTitle();

        title.fadeIn(BungeeMessages.CONTROLFINISH_FADEIN.get(Integer.class) * 20);
        title.stay(BungeeMessages.CONTROLFINISH_STAY.get(Integer.class) * 20);
        title.fadeOut(BungeeMessages.CONTROLFINISH_FADEOUT.get(Integer.class) * 20);

        title.title(new TextComponent(BungeeMessages.CONTROLFINISH_TITLE.color()));
        title.subTitle(new TextComponent(BungeeMessages.CONTROLFINISH_SUBTITLE.color()));

        ProxyServer.getInstance().getScheduler().schedule(instance, () ->
                title.send(suspicious), BungeeMessages.CONTROLFINISH_DELAY.get(Integer.class), TimeUnit.SECONDS);

    }

    private void sendAdminEndTitle(ProxiedPlayer administrator, ProxiedPlayer suspicious) {

        if (!BungeeMessages.ADMINCONTROLFINISH_USETITLE.get(Boolean.class)) {
            return;
        }

        final Title title = ProxyServer.getInstance().createTitle();

        title.fadeIn(BungeeMessages.ADMINCONTROLFINISH_FADEIN.get(Integer.class) * 20);
        title.stay(BungeeMessages.ADMINCONTROLFINISH_STAY.get(Integer.class) * 20);
        title.fadeOut(BungeeMessages.ADMINCONTROLFINISH_FADEOUT.get(Integer.class) * 20);

        String user_prefix = "";
        String user_suffix = "";

        if (isLuckPerms) {

            final LuckPerms api = LuckPermsProvider.get();
            final User user = api.getUserManager().getUser(suspicious.getUniqueId());

            if (user == null) {
                return;
            }

            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            user_prefix = prefix == null ? "" : prefix;
            user_suffix = suffix == null ? "" : suffix;

        }

        title.title(new TextComponent(BungeeMessages.ADMINCONTROLFINISH_TITLE.color()
                .replace("%suspect%", suspicious.getName())
                .replace("%suspectprefix%", user_prefix)
                .replace("%suspectsuffix%", user_suffix)));

        title.subTitle(new TextComponent(BungeeMessages.ADMINCONTROLFINISH_SUBTITLE.color()
                .replace("%suspect%", suspicious.getName())
                .replace("%suspectprefix%", user_prefix)
                .replace("%suspectsuffix%", user_suffix)));

        ProxyServer.getInstance().getScheduler().schedule(instance, () ->
                title.send(administrator), BungeeMessages.ADMINCONTROLFINISH_DELAY.get(Integer.class), TimeUnit.SECONDS);
    }

    public List<ServerInfo> getServerList(List<String> stringList) {
        List<ServerInfo> servers = new ArrayList<>();
        for (String server : stringList) {

            if (!instance.getProxy().getServers().containsKey(server)) {
                if (BungeeConfig.USE_DISCONNECT.get(Boolean.class)) {
                    continue;
                }
                instance.getLogger().severe("The server " + server + " is not configured correctly, please check the configuration file.");
                continue;
            }

            servers.add(instance.getProxy().getServerInfo(server));
        }
        return servers;
    }

    public ServerInfo getBestServer(List<ServerInfo> list) {

        if (list.isEmpty()) {
            return null;
        }

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
        if (task.get(serverInfo) != null) {
            task.get(serverInfo).cancel();
        }

        task.remove(serverInfo);
    }

    private void taskServer(ServerInfo server) {

        if (BungeeConfig.DISABLE_PING.get(Boolean.class)) {
            return;
        }

        task.put(server, instance.getProxy().getScheduler().schedule(instance, () -> server.ping((result, error) -> {

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
            } else if (server.get().getPlayers().size() < serverInfo.getPlayers().size()) {
                serverInfo.ping((result, error) -> {
                    if (error == null && result != null) {

                        if (result.getPlayers().getMax() == result.getPlayers().getOnline()) {
                            return;
                        }

                        server.set(serverInfo);
                    }
                });
            }
        }
        return server.get();
    }

    private ServerInfo getRandomServer(List<ServerInfo> list) {
        return list.get(new Random().nextInt(list.size()));
    }

    private void connect(ProxiedPlayer player, ServerInfo server) {
            player.connect(server);
    }

    public final boolean isLuckPerms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;
}