package it.frafol.cleanss.bungee.objects;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@UtilityClass
public class Utils {

    private static final CleanSS instance = CleanSS.getInstance();

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

    public String color(@NotNull String s) {
        return s.replace("&", "§");
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
            embed.setFooter("Powered by CleanStaffChat");

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
            embed.setFooter("Powered by CleanStaffChat");

            channel.sendMessageEmbeds(embed.build()).queue();

        }
    }

    private String addCapital(String string) {
		if (string == null || string.isEmpty()) {
			return string;
		}

		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
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

            if (BungeeMessages.DISCORD_CAPITAL.get(Boolean.class)) {
                Utils.sendDiscordMessage(suspect, administrator_player, BungeeMessages.DISCORD_FINISHED.get(String.class).replace("%admingroup%", addCapital(admin_group)).replace("%suspectgroup%", addCapital(suspect_group)), BungeeMessages.CHEATER.get(String.class));
                return;
            }

            Utils.sendDiscordMessage(suspect, administrator_player, BungeeMessages.DISCORD_FINISHED.get(String.class).replace("%admingroup%", admin_group).replace("%suspectgroup%", suspect_group), BungeeMessages.CHEATER.get(String.class));

            String admin_prefix;
            String admin_suffix;
            String sus_prefix;
            String sus_suffix;

            if (luckperms) {

                final LuckPerms api = LuckPermsProvider.get();

                final User admin = api.getUserManager().getUser(administrator_player.getUniqueId());
                final User suspect1 = api.getUserManager().getUser(suspect.getUniqueId());

                if (admin == null) {
                    return;
                }

                if (suspect1 == null) {
                    return;
                }

                final String prefix1 = admin.getCachedData().getMetaData().getPrefix();
                final String suffix1 = admin.getCachedData().getMetaData().getSuffix();

                final String prefix2 = suspect1.getCachedData().getMetaData().getPrefix();
                final String suffix2 = suspect1.getCachedData().getMetaData().getSuffix();

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

        if (BungeeMessages.DISCORD_CAPITAL.get(Boolean.class)) {
            Utils.sendDiscordMessage(suspect, administrator_player, BungeeMessages.DISCORD_QUIT.get(String.class).replace("%admingroup%", addCapital(admin_group)).replace("%suspectgroup%", addCapital(suspect_group)), BungeeMessages.LEFT.get(String.class));
        } else {
            Utils.sendDiscordMessage(suspect, administrator_player, BungeeMessages.DISCORD_QUIT.get(String.class).replace("%admingroup%", admin_group).replace("%suspectgroup%", suspect_group), BungeeMessages.LEFT.get(String.class));
        }

        String admin_prefix;
        String admin_suffix;
        String sus_prefix;
        String sus_suffix;

        if (luckperms) {

            final LuckPerms api = LuckPermsProvider.get();

            final User admin = api.getUserManager().getUser(administrator_player.getUniqueId());
            final User suspect1 = api.getUserManager().getUser(suspect.getUniqueId());

            if (admin == null) {
                return;
            }

            if (suspect1 == null) {
                return;
            }

            final String prefix1 = admin.getCachedData().getMetaData().getPrefix();
            final String suffix1 = admin.getCachedData().getMetaData().getSuffix();

            final String prefix2 = suspect1.getCachedData().getMetaData().getPrefix();
            final String suffix2 = suspect1.getCachedData().getMetaData().getSuffix();

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

            if (administrator.getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class))) {

                if (proxyServer == null) {
                    return;
                }

                if (!BungeeConfig.USE_DISCONNECT.get(Boolean.class)) {
                    administrator.connect(proxyServer);
                } else {
                    Utils.sendChannelMessage(administrator, "DISCONNECT_NOW");
                }

                Utils.sendEndTitle(suspicious);

                administrator.sendMessage(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color().replace("%prefix%", BungeeMessages.PREFIX.color())));

                if (suspicious.getServer() == null) {
                    return;
                }

                if (suspicious.getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class))) {
                    suspicious.connect(proxyServer);
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
                suspicious.connect(proxyServer);
            } else {
                Utils.sendChannelMessage(suspicious, "DISCONNECT_NOW");
            }

            Utils.sendEndTitle(suspicious);

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
                administrator.connect(proxyServer);
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

    public void startControl(@NotNull ProxiedPlayer suspicious, @NotNull ProxiedPlayer administrator, ServerInfo proxyServer) {

        if (!Objects.equals(administrator.getServer().getInfo(), proxyServer)) {
            administrator.connect(proxyServer);
        } else {
            Utils.sendChannelMessage(administrator, "ADMIN");
        }

        if (!Objects.equals(suspicious.getServer().getInfo(), proxyServer)) {
            suspicious.connect(proxyServer);
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

        if (BungeeConfig.CHECK_FOR_PROBLEMS.get(Boolean.class)) {
            Utils.checkForErrors(suspicious, administrator, proxyServer);
        }

        String admin_prefix;
        String admin_suffix;
        String sus_prefix;
        String sus_suffix;

        boolean luckperms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;
        if (luckperms) {

            final LuckPerms api = LuckPermsProvider.get();

            final User admin = api.getUserManager().getUser(administrator.getUniqueId());
            final User suspect = api.getUserManager().getUser(suspicious.getUniqueId());

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
                new Placeholder("suspect", suspicious.getName()),
                new Placeholder("administrator", administrator.getName()),
                new Placeholder("adminprefix", color(admin_prefix)),
                new Placeholder("adminsuffix", color(admin_suffix)),
                new Placeholder("suspectprefix", color(sus_prefix)),
                new Placeholder("suspectsuffix", color(sus_suffix)));

    }

    private void checkForErrors(ProxiedPlayer suspicious, ProxiedPlayer administrator, ServerInfo proxyServer) {

        instance.getProxy().getScheduler().schedule(instance, () -> {

            if (!(PlayerCache.getSuspicious().contains(suspicious.getUniqueId()) && PlayerCache.getAdministrator().contains(administrator.getUniqueId()))) {
                return;
            }

            if (suspicious.getServer().getInfo().equals(proxyServer) || administrator.getServer().getInfo().equals(proxyServer)) {
                return;
            }

            final ServerInfo fallbackServer = instance.getProxy().getServerInfo(BungeeConfig.CONTROL_FALLBACK.get(String.class));

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
}