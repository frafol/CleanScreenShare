package it.frafol.cleanss.bukkit.objects.utils;

import com.google.common.base.Strings;
import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class TabListUtil {

    private final CleanSS instance = CleanSS.getInstance();

    public static void sendTabList(Player player, String header, String footer) {

        header = Strings.isNullOrEmpty(header) ? "" : color(header);
        footer = Strings.isNullOrEmpty(footer) ? "" : color(footer);

        if (!isLowerThan1_12_2()) {
            player.setPlayerListHeaderFooter(header, footer);

            if (!instance.isPAPI()) {
                player.setPlayerListName(SpigotConfig.TABLIST_FORMAT.color().replace("%player%", player.getName()));
                player.setCustomName(SpigotConfig.TABLIST_FORMAT.color().replace("%player%", player.getName()));
                player.setCustomNameVisible(true);
                return;
            }

            player.setPlayerListName(color(PlaceholderAPI.setPlaceholders(player, SpigotConfig.TABLIST_FORMAT.get(String.class).replace("%player%", player.getName()))));
            player.setCustomName(color(PlaceholderAPI.setPlaceholders(player, SpigotConfig.TABLIST_FORMAT.get(String.class).replace("%player%", player.getName()))));
            player.setCustomNameVisible(true);
            return;
        }

        try {
            Method chatComponentBuilderMethod = Objects.requireNonNull(ReflectionUtils.getNMSClass("IChatBaseComponent")).getDeclaredClasses()[0].getMethod("a", String.class);
            Object tabHeader = chatComponentBuilderMethod.invoke(null, "{\"text\":\"" + header + "\"}");
            Object tabFooter = chatComponentBuilderMethod.invoke(null, "{\"text\":\"" + footer + "\"}");
            Object packet = Objects.requireNonNull(ReflectionUtils.getNMSClass("PacketPlayOutPlayerListHeaderFooter")).getConstructor().newInstance();

            Field aField;
            Field bField;
            try {
                aField = packet.getClass().getDeclaredField("a");
                bField = packet.getClass().getDeclaredField("b");
            } catch (Exception ex) {
                aField = packet.getClass().getDeclaredField("header");
                bField = packet.getClass().getDeclaredField("footer");
            }

            aField.setAccessible(true);
            aField.set(packet, tabHeader);

            bField.setAccessible(true);
            bField.set(packet, tabFooter);

            ReflectionUtils.sendPacket(player, packet);

        } catch (Exception ignored) {
            instance.getLogger().severe("Cannot send tablist to " + player.getName() + ".");
        }
    }

    private static boolean isLowerThan1_12_2() {
        String serverVersion = instance.getServer().getVersion();
        Pattern pattern = Pattern.compile(".*(\\d+\\.\\d+\\.\\d+).*");
        Matcher matcher = pattern.matcher(serverVersion);

        if (matcher.matches()) {
            String serverVersionNumber = matcher.group(1);
            String[] serverVersionParts = serverVersionNumber.split("\\.");
            String[] targetVersionParts = "1.12.2".split("\\.");

            for (int i = 0; i < Math.min(serverVersionParts.length, targetVersionParts.length); i++) {
                int serverPart = Integer.parseInt(serverVersionParts[i]);
                int targetPart = Integer.parseInt(targetVersionParts[i]);

                if (serverPart < targetPart) {
                    return true;
                } else if (serverPart > targetPart) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private String color(String string) {
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
}
