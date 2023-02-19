package it.frafol.cleanss.bungee.objects;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public void finishControl(@NotNull ProxiedPlayer suspicious, @NotNull ProxiedPlayer administrator, ServerInfo proxyServer) {

        if (administrator.isConnected() && suspicious.isConnected()) {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());
            PlayerCache.getCouples().remove(administrator, suspicious);

            if (administrator.getServer() == null) {
                return;
            }

            if (administrator.getServer().getInfo().getName().equals(BungeeConfig.CONTROL.get(String.class))) {

                if (proxyServer == null) {
                    return;
                }

                administrator.connect(proxyServer);

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

            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());
            PlayerCache.getAdministrator().remove(administrator.getUniqueId());

            suspicious.connect(proxyServer);

            Utils.sendEndTitle(suspicious);

            suspicious.sendMessage(TextComponent.fromLegacyText(BungeeMessages.FINISHSUS.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));

            PlayerCache.getCouples().remove(administrator);

        } else if (administrator.isConnected()) {

            PlayerCache.getAdministrator().remove(administrator.getUniqueId());
            PlayerCache.getSuspicious().remove(suspicious.getUniqueId());

            administrator.connect(proxyServer);

            administrator.sendMessage(TextComponent.fromLegacyText(BungeeMessages.LEAVESUS.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                    .replace("%player%", suspicious.getName())));

            PlayerCache.getCouples().remove(administrator);

        }
    }

    public void startControl(@NotNull ProxiedPlayer suspicious, @NotNull ProxiedPlayer administrator, ServerInfo proxyServer) {

        PlayerCache.getAdministrator().add(administrator.getUniqueId());
        PlayerCache.getSuspicious().add(suspicious.getUniqueId());
        PlayerCache.getCouples().put(administrator, suspicious);

        if (!Objects.equals(administrator.getServer().getInfo(), proxyServer)) {
            administrator.connect(proxyServer);
        }

        if (!Objects.equals(suspicious.getServer().getInfo(), proxyServer)) {
            suspicious.connect(proxyServer);
        }

        Utils.sendStartTitle(suspicious);
        Utils.checkForErrors(suspicious, administrator, proxyServer);

        suspicious.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MAINSUS.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())));

        BungeeMessages.CONTROL_FORMAT.sendList(administrator, suspicious,
                new Placeholder("cleanname", BungeeMessages.CONTROL_CLEAN_NAME.color()),
                new Placeholder("hackername", BungeeMessages.CONTROL_CHEATER_NAME.color()),
                new Placeholder("admitname", BungeeMessages.CONTROL_ADMIT_NAME.color()),
                new Placeholder("refusename", BungeeMessages.CONTROL_REFUSE_NAME.color()));

    }

    private void checkForErrors(ProxiedPlayer suspicious, ProxiedPlayer administrator, ServerInfo proxyServer) {

        instance.getProxy().getScheduler().schedule(instance, () -> {

            if (suspicious.getServer().getInfo().equals(proxyServer) && administrator.getServer().getInfo().equals(proxyServer)) {
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