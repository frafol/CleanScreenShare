package it.frafol.cleanss.bungee.objects;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {

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
        return list.stream().map(ChatUtil::color).collect(Collectors.toList());
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

            } else {

                commandSource.sendMessage(TextComponent.fromLegacyText(message));

            }

        }
    }

    public void sendFormattedList(BungeeMessages velocityMessages, CommandSender commandSender, ProxiedPlayer player_name, Placeholder... placeholders) {
        sendList(commandSender, color(getStringList(velocityMessages, placeholders)), player_name);
    }

}