package it.frafol.cleanss.bungee.objects;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {

    private final CleanSS instance = CleanSS.getInstance();

    public List<String> getStringList(BungeeMessages velocityMessages) {
        return instance.getMessagesTextFile().getStringList(velocityMessages.getPath());
    }

    public List<String> getStringList(BungeeMessages velocityMessages, Placeholder... placeholders) {
        List<String> newList = new ArrayList<>();

        for (String s : getStringList(velocityMessages)) {
            s = applyPlaceholder(s, placeholders);
            newList.add(s);
        }

        return newList;
    }

    public String applyPlaceholder(String s, Placeholder ... placeholders) {
            for (Placeholder placeholder : placeholders) {
                if (placeholder != null) {
                    s = s.replace(placeholder.getKey(), placeholder.getValue());
                }
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
            str = str.replace(color, ChatColor.of(color.replace("&","")) + "");
            match = pattern.matcher(str);
        }
        return ChatColor.translateAlternateColorCodes('&',str);
    }

    public List<String> color(List<String> list) {
        return list.stream().map(ChatUtil::color).collect(Collectors.toList());
    }

    private boolean hasButton(List<String> stringList) {
        for (String string : stringList) {
            if (string.contains("%buttons%")) {
                return true;
            }
        }
        return false;
    }

    public void sendList(CommandSender commandSource, List<String> stringList) {
        for (String message : stringList) {
            if (!containsCommand(message).equals("none")) {
                TextComponent suggestMessage = new TextComponent(message);
                suggestMessage.setClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/" + containsCommand(message) + " "));
                commandSource.sendMessage(suggestMessage);
                continue;
            }
            commandSource.sendMessage(TextComponent.fromLegacy(message));
        }
    }

    private void sendButtonList(CommandSender commandSource, List<String> stringList, ProxiedPlayer player_name) {

        if (!BungeeMessages.CONTROL_USEVERTICALFORMAT.get(Boolean.class) && hasButton(stringList)) {
            sendHorizontalButtons(commandSource, stringList, player_name);
            return;
        }

        for (String message : stringList) {

            if (getButton(message) == null) {
                commandSource.sendMessage(TextComponent.fromLegacy(message));
                continue;
            }

            String button = getButton(message);
            TextComponent suggestMessage = new TextComponent(message.replace("%" + button + "name%", color(instance.getMessagesTextFile().getString("messages.staff_message.buttons." + button + ".name"))));
            if (!BungeeMessages.BUTTON_EXECUTION.get(Boolean.class)) {
                suggestMessage.setClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        instance.getMessagesTextFile().getString("messages.staff_message.buttons." + button + ".command")
                                .replace("%player%", player_name.getName())));
            } else {
                suggestMessage.setClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        instance.getMessagesTextFile().getString("messages.staff_message.buttons." + button + ".command")
                                .replace("%player%", player_name.getName())));
            }
            commandSource.sendMessage(suggestMessage);
        }
    }


    private void sendHorizontalButtons(CommandSender commandSource, List<String> stringList, ProxiedPlayer player_name) {

        List<TextComponent> buttons = new ArrayList<>();
        for (String message : stringList) {
            if (message.contains("%buttons%")) {
                for (String key : getButtons(player_name).keySet()) {
                    TextComponent button = new TextComponent(key + " ");
                    button.setClickEvent(new ClickEvent(
                            ClickEvent.Action.SUGGEST_COMMAND,
                            getButtons(player_name).get(key).replace("%player%", player_name.getName())));
                    buttons.add(button);
                }

                ComponentBuilder builder = new ComponentBuilder();
                for (TextComponent component : buttons) {
                    builder.append(component);
                }

                commandSource.sendMessage(builder.create());
                continue;
            }

            commandSource.sendMessage(TextComponent.fromLegacy(message));
        }
    }

    private String getButton(String message) {
        for (String buttons : instance.getMessagesTextFile().getStringList("messages.staff_message.buttons")) {
            if (message.contains("%" + buttons + "name%")) {
                return buttons;
            }
        }
        return null;
    }

    private HashMap<String, String> getButtons(ProxiedPlayer suspect) {
        HashMap<String, String> buttons = new HashMap<>();
        for (String button : instance.getMessagesTextFile().getStringList("messages.staff_message.buttons")) {
            buttons.put(
                    instance.getMessagesTextFile().getString("messages.staff_message.buttons." + button + ".name"),
                    instance.getMessagesTextFile().getString("messages.staff_message.buttons." + button + ".command")
                            .replace("%player%", suspect.getName()));
        }
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
        for (String command : BungeeCommandsConfig.SS_PLAYER.getStringList()) {
            if (foundCommand.equalsIgnoreCase(command)) {
                return command;
            }
        }
        for (String command : BungeeCommandsConfig.SS_SPECTATE.getStringList()) {
            if (foundCommand.equalsIgnoreCase(command)) {
                return command;
            }
        }
        for (String command : BungeeCommandsConfig.SS_FINISH.getStringList()) {
            if (foundCommand.equalsIgnoreCase(command)) {
                return command;
            }
        }
        for (String command : BungeeCommandsConfig.SS_INFO.getStringList()) {
            if (foundCommand.equalsIgnoreCase(command)) {
                return command;
            }
        }
        if (foundCommand.equalsIgnoreCase("ssreload")) {
            return foundCommand;
        }
        return "none";
    }

    public void sendFormattedList(BungeeMessages velocityMessages, CommandSender commandSender, ProxiedPlayer player_name, boolean start, Placeholder... placeholders) {
        if (start) {
            sendButtonList(commandSender, color(getStringList(velocityMessages, placeholders)), player_name);
            return;
        }
        sendList(commandSender, color(getStringList(velocityMessages, placeholders)));
    }
}
