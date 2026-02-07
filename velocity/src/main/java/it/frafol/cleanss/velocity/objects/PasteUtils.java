package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.intellij.lang.annotations.RegExp;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class PasteUtils {

    private final CleanSS instance = CleanSS.getInstance();
    private static final String DPASTE_API_URL = "https://dpaste.com/api/v2/";

    @SneakyThrows
    public static String uploadLogFile(Path logFilePath) {
        if (!Files.exists(logFilePath) || !Files.isRegularFile(logFilePath)) {
            throw new IllegalArgumentException("File not found: " + logFilePath);
        }

        String content = Files.readString(logFilePath, StandardCharsets.UTF_8);

        String postData = "content=" + URLEncoder.encode(content, StandardCharsets.UTF_8)
                + "&title=" + URLEncoder.encode(logFilePath.getFileName().toString(), StandardCharsets.UTF_8)
                + "&expiry_days=7";

        URL url = new URL(DPASTE_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("User-Agent", "CleanScreenShare/1.0");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        conn.getOutputStream().write(postData.getBytes(StandardCharsets.UTF_8));

        int responseCode = conn.getResponseCode();
        if (responseCode != 201 && responseCode != 200) {
            throw new IOException("Error uploading file on dpaste.com: codice HTTP " + responseCode);
        }

        String pasteUrl;
        try (InputStream is = conn.getInputStream()) {
            pasteUrl = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
        }

        if (!pasteUrl.endsWith(".txt")) {
            pasteUrl += ".txt";
        }

        return pasteUrl;
    }

    @SneakyThrows
    public void uploadLogByFileName(String fileName, Player targetMessage) {
        Path logsFolder = instance.getPath().resolve("chat-logs");
        try {
            logsFolder.resolve(fileName);
        } catch (NullPointerException ignored) {
            return;
        }
        Path logFile = logsFolder.resolve(fileName);
        if (!Files.exists(logFile) || !Files.isRegularFile(logFile)) return;
        CompletableFuture.runAsync(() -> {
            @RegExp String link = uploadLogFile(logFile);
            String raw = VelocityMessages.CONTROL_FINISH_LINK.color()
                    .replace("%link%", link)
                    .replace("%prefix%", VelocityMessages.PREFIX.color());
            TextComponent linkComponent = Component.text(link).clickEvent(ClickEvent.openUrl(link));
            Component messageComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
            Component finalMessage = messageComponent.replaceText(r -> r.match(link).replacement(linkComponent));
            targetMessage.sendMessage(finalMessage);
        });
    }
}

