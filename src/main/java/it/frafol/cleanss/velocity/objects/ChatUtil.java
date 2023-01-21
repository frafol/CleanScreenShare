package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {

    private static final CleanSS instance = CleanSS.getInstance();

    public List<String> getStringList(@NotNull VelocityMessages velocityMessages) {
        return instance.getMessagesTextFile().getConfig().getStringList(velocityMessages.getPath());
    }

    public List<String> getStringList(VelocityMessages velocityMessages, Placeholder... placeholders) {
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

    public void sendList(CommandSource commandSource, @NotNull List<String> stringList, Player player_name) {

        for (String message : stringList) {

            if (message.contains(VelocityMessages.CONTROL_CLEAN_NAME.get(String.class))) {
                commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message).clickEvent(ClickEvent
                        .clickEvent(ClickEvent.Action.SUGGEST_COMMAND, VelocityMessages.CONTROL_CLEAN_COMMAND.get(String.class)
                                .replace("%player%", player_name.getUsername()))));
            } else if (message.contains(VelocityMessages.CONTROL_CHEATER_NAME.get(String.class))) {
                commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message).clickEvent(ClickEvent
                        .clickEvent(ClickEvent.Action.SUGGEST_COMMAND, VelocityMessages.CONTROL_CHEATER_COMMAND.get(String.class)
                                .replace("%player%", player_name.getUsername()))));
            } else if (message.contains(VelocityMessages.CONTROL_ADMIT_NAME.get(String.class))) {
                commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message)
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, VelocityMessages.CONTROL_ADMIT_COMMAND.get(String.class)
                                .replace("%player%", player_name.getUsername()))));
            } else {
                commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message));
            }

        }
    }

    public void sendFormattedList(VelocityMessages velocityMessages, CommandSource commandSource, Player player_name, Placeholder... placeholders) {
        sendList(commandSource, color(getStringList(velocityMessages, placeholders)), player_name);
    }

}