package it.frafol.cleanss.bungee.objects;

import it.frafol.cleanss.bungee.CleanSS;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class LogUtils {

    private final CleanSS plugin = CleanSS.getInstance();
    private final Map<String, List<String>> logs = new ConcurrentHashMap<>();

    private String stripColorCodes(String input) {
        if (input == null) return null;
        return input.replaceAll("(?i)§[0-9A-FK-OR]", "");
    }

    public void addLine(String staffName, String line) {
        line = stripColorCodes(line);
        logs.computeIfAbsent(staffName, key -> new ArrayList<>()).add(line);
    }

    @SneakyThrows
    public String writeLogFile(String playerName) {
        List<String> lines = logs.get(playerName);
        if (lines == null || lines.isEmpty()) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String fileName = playerName + "-" + timestamp + ".log";
        Path folder = plugin.getDataFolder().toPath().resolve("chat-logs");
        if (Files.notExists(folder)) Files.createDirectories(folder);
        Path filePath = folder.resolve(fileName);
        Files.write(filePath, lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        logs.remove(playerName);
        return fileName;
    }
}
