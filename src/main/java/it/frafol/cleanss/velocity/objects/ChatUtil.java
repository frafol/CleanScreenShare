package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityCommandsConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {

    private final CleanSS instance = CleanSS.getInstance();

    public List<String> getStringList(VelocityMessages velocityMessages) {
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

    public String applyPlaceHolder(String s, Placeholder... placeholders) {
        for (Placeholder placeholder : placeholders) {
            s = s.replace(placeholder.getKey(), placeholder.getValue());
        }

        return s;
    }

    public String color(String string) {
        String hex = convertHexColors(string);
        return hex.replace("&", "§");
    }

    private String convertHexColors(String message) {

        if (!containsHexColor(message)) {
            return message;
        }

        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return message;
    }

    private boolean containsHexColor(String message) {
        String hexColorPattern = "(?i)&#[a-f0-9]{6}";
        return message.matches(".*" + hexColorPattern + ".*");
    }

    public List<String> color(List<String> list) {
        return list.stream().map(ChatUtil::color).collect(Collectors.toList());
    }

    public void sendList(VelocityMessages velocityMessages, CommandSource commandSource, Placeholder... placeholders) {
        sendList(commandSource, ChatUtil.color(ChatUtil.getStringList(velocityMessages, placeholders)));
    }

    public void sendCompiledButtons(VelocityMessages velocityMessages, CommandSource commandSource, Player player_name, Placeholder... placeholders) {
        sendButtons(commandSource, ChatUtil.color(ChatUtil.getStringList(velocityMessages, placeholders)), player_name);
    }

    public void sendList(CommandSource commandSource, List<String> stringList) {
        for (String message : stringList) {
            if (!containsCommand(message).equals("none")) {
                commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message).clickEvent(ClickEvent
                        .clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + containsCommand(message) + " ")));
                continue;
            }
            commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message));
        }
    }

    public void sendButtons(CommandSource commandSource, List<String> stringList, Player player_name) {

        if (!VelocityMessages.CONTROL_USEVERTICALFORMAT.get(Boolean.class)) {
            sendHorizontalButtons(commandSource, stringList, player_name);
            return;
        }

        for (String message : stringList) {

            if (message.contains(VelocityMessages.CONTROL_CLEAN_NAME.color())) {
                if (VelocityMessages.BUTTON_EXECUTION.get(Boolean.class)) {
                    commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message).clickEvent(ClickEvent
                            .clickEvent(ClickEvent.Action.RUN_COMMAND, VelocityMessages.CONTROL_CLEAN_COMMAND.get(String.class)
                                    .replace("%player%", player_name.getUsername()))));
                } else {
                    commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message).clickEvent(ClickEvent
                            .clickEvent(ClickEvent.Action.SUGGEST_COMMAND, VelocityMessages.CONTROL_CLEAN_COMMAND.get(String.class)
                                    .replace("%player%", player_name.getUsername()))));
                }
            } else if (message.contains(VelocityMessages.CONTROL_CHEATER_NAME.color())) {
                if (VelocityMessages.BUTTON_EXECUTION.get(Boolean.class)) {
                    commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message).clickEvent(ClickEvent
                            .clickEvent(ClickEvent.Action.RUN_COMMAND, VelocityMessages.CONTROL_CHEATER_COMMAND.get(String.class)
                                    .replace("%player%", player_name.getUsername()))));
                } else {
                    commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message).clickEvent(ClickEvent
                            .clickEvent(ClickEvent.Action.SUGGEST_COMMAND, VelocityMessages.CONTROL_CHEATER_COMMAND.get(String.class)
                                    .replace("%player%", player_name.getUsername()))));
                }
            } else if (message.contains(VelocityMessages.CONTROL_ADMIT_NAME.color())) {
                if (VelocityMessages.BUTTON_EXECUTION.get(Boolean.class)) {
                    commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message).clickEvent(ClickEvent
                            .clickEvent(ClickEvent.Action.RUN_COMMAND, VelocityMessages.CONTROL_ADMIT_COMMAND.get(String.class)
                                    .replace("%player%", player_name.getUsername()))));
                } else {
                    commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message).clickEvent(ClickEvent
                            .clickEvent(ClickEvent.Action.SUGGEST_COMMAND, VelocityMessages.CONTROL_ADMIT_COMMAND.get(String.class)
                                    .replace("%player%", player_name.getUsername()))));
                }
            } else if (message.contains(VelocityMessages.CONTROL_REFUSE_NAME.color())) {
                if (VelocityMessages.BUTTON_EXECUTION.get(Boolean.class)) {
                    commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message).clickEvent(ClickEvent
                            .clickEvent(ClickEvent.Action.RUN_COMMAND, VelocityMessages.CONTROL_REFUSE_COMMAND.get(String.class)
                                    .replace("%player%", player_name.getUsername()))));
                } else {
                    commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message)
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, VelocityMessages.CONTROL_REFUSE_COMMAND.get(String.class)
                                    .replace("%player%", player_name.getUsername()))));
                }
            } else {
                commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message));
            }
        }
    }

    private void sendHorizontalButtons(CommandSource commandSource, List<String> stringList, Player player_name) {

        List<TextComponent> buttons = new ArrayList<>();

        for (String message : stringList) {
            if (message.contains("%buttons%")) {
                for (String key : getButtons(player_name).keySet()) {
                    TextComponent button = LegacyComponentSerializer.legacy('§').deserialize(key)
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, getButtons(player_name).get(key)
                                    .replace("%player%", player_name.getUsername()))).append(Component.text(" "));
                    buttons.add(button);
                }

                ComponentBuilder<TextComponent, TextComponent.Builder> builder = Component.text();
                for (TextComponent component : buttons) {
                    builder.append(component);
                }

                commandSource.sendMessage(builder.build());
                continue;
            }

            commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(message));
        }
    }

    private HashMap<String, String> getButtons(Player suspect) {
        HashMap<String, String> buttons = new HashMap<>();
        buttons.put(VelocityMessages.CONTROL_CLEAN_NAME.color(), VelocityMessages.CONTROL_CLEAN_COMMAND.get(String.class).replace("%player%", suspect.getUsername()));
        buttons.put(VelocityMessages.CONTROL_CHEATER_NAME.color(), VelocityMessages.CONTROL_CHEATER_COMMAND.get(String.class).replace("%player%", suspect.getUsername()));
        buttons.put(VelocityMessages.CONTROL_ADMIT_NAME.color(), VelocityMessages.CONTROL_ADMIT_COMMAND.get(String.class).replace("%player%", suspect.getUsername()));
        buttons.put(VelocityMessages.CONTROL_REFUSE_NAME.color(), VelocityMessages.CONTROL_REFUSE_COMMAND.get(String.class).replace("%player%", suspect.getUsername()));
        return buttons;
    }

    public static String getCommand(String input) {
        int slashIndex = input.indexOf("/");
        if (slashIndex == -1 || slashIndex == input.length() - 1) {
            return input;
        }
        int spaceIndex = input.indexOf(" ", slashIndex);
        if (spaceIndex == -1) {
            spaceIndex = input.length();
        }
        return input.substring(slashIndex + 1, spaceIndex).trim();
    }

    private String containsCommand(String message) {
        String foundCommand = getCommand(message);
        for (String command : VelocityCommandsConfig.SS_PLAYER.getStringList()) {
            if (foundCommand.equalsIgnoreCase(command)) {
                return command;
            }
        }
        for (String command : VelocityCommandsConfig.SS_SPECTATE.getStringList()) {
            if (foundCommand.equalsIgnoreCase(command)) {
                return command;
            }
        }
        for (String command : VelocityCommandsConfig.SS_FINISH.getStringList()) {
            if (foundCommand.equalsIgnoreCase(command)) {
                return command;
            }
        }
        for (String command : VelocityCommandsConfig.SS_INFO.getStringList()) {
            if (foundCommand.equalsIgnoreCase(command)) {
                return command;
            }
        }
        if (foundCommand.equalsIgnoreCase("ssreload")) {
            return foundCommand;
        }
        return "none";
    }
}
