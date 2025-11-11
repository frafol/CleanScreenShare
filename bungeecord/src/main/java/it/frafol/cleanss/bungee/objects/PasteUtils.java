package it.frafol.cleanss.bungee.objects;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UtilityClass
public class PasteUtils {

    private final CleanSS instance = CleanSS.getInstance();
    private static final String DPASTE_API_URL = "https://dpaste.com/api/v2/";
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

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
    public static void uploadLogByFileName(String fileName, ProxiedPlayer targetMessage) {
        Path logsFolder = instance.getDataFolder().toPath().resolve("chat-logs");
        Path logFile = logsFolder.resolve(fileName);
        if (!Files.exists(logFile) || !Files.isRegularFile(logFile)) return;
        CompletableFuture.runAsync(() -> {
            String link = uploadLogFile(logFile);
            targetMessage.sendMessage(TextComponent.fromLegacy(
                    BungeeMessages.CONTROL_FINISH_LINK.color().replace("%link%", link)));
        }, executor);
    }
}

