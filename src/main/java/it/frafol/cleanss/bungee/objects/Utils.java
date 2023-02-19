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

                if (BungeeMessages.CONTROLFINISH_USETITLE.get(Boolean.class)) {

                    final Title title = ProxyServer.getInstance().createTitle();

                    title.fadeIn(BungeeMessages.CONTROLFINISH_FADEIN.get(Integer.class) * 20);
                    title.stay(BungeeMessages.CONTROLFINISH_STAY.get(Integer.class) * 20);
                    title.fadeOut(BungeeMessages.CONTROLFINISH_FADEOUT.get(Integer.class) * 20);

                    title.title(new TextComponent(BungeeMessages.CONTROLFINISH_TITLE.color()));
                    title.subTitle(new TextComponent(BungeeMessages.CONTROLFINISH_SUBTITLE.color()));

                    ProxyServer.getInstance().getScheduler().schedule(instance, () ->
                            title.send(suspicious), BungeeMessages.CONTROLFINISH_DELAY.get(Integer.class), TimeUnit.SECONDS);
                }

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

            if (BungeeMessages.CONTROLFINISH_USETITLE.get(Boolean.class)) {

                final Title title = ProxyServer.getInstance().createTitle();

                title.fadeIn(BungeeMessages.CONTROLFINISH_FADEIN.get(Integer.class) * 20);
                title.stay(BungeeMessages.CONTROLFINISH_STAY.get(Integer.class) * 20);
                title.fadeOut(BungeeMessages.CONTROLFINISH_FADEOUT.get(Integer.class) * 20);

                title.title(new TextComponent(BungeeMessages.CONTROLFINISH_TITLE.color()));
                title.subTitle(new TextComponent(BungeeMessages.CONTROLFINISH_SUBTITLE.color()));

                ProxyServer.getInstance().getScheduler().schedule(instance, () ->
                        title.send(suspicious), BungeeMessages.CONTROLFINISH_DELAY.get(Integer.class), TimeUnit.SECONDS);
            }

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
}