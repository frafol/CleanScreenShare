package it.frafol.cleanss.velocity.handlers;

import it.frafol.cleanss.velocity.CleanSS;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class DataHandler {

    private final CleanSS instance = CleanSS.getInstance();

    @SneakyThrows
    public static void saveData() {
        instance.getDataTextFile().getConfig().save();
    }

    public static void incrementDone(UUID playerId) {
        incrementStat(playerId, "done");
    }

    public static void incrementSuffered(UUID playerId) {
        incrementStat(playerId, "suffered");
    }

    public static void decrementDone(UUID playerId) {
        decrementStat(playerId, "done");
    }

    public static void decrementSuffered(UUID playerId) {
        decrementStat(playerId, "suffered");
    }

    private static void incrementStat(UUID playerId, String statType) {
        String playerIdStr = playerId.toString();
        Map<String, Object> stats = (Map<String, Object>) instance.getDataTextFile().getConfig().get("stats");

        if (stats == null) {
            stats = new HashMap<>();
        }

        Map<String, Integer> playerStats = (Map<String, Integer>) stats.getOrDefault(playerIdStr, new HashMap<>());
        int current = playerStats.getOrDefault(statType, 0);
        playerStats.put(statType, current + 1);

        stats.put(playerIdStr, playerStats);
        instance.getDataTextFile().getConfig().set("stats", stats);
        saveData();
    }

    private static void decrementStat(UUID playerId, String statType) {
        String playerIdStr = playerId.toString();
        Map<String, Object> stats = (Map<String, Object>) instance.getDataTextFile().getConfig().get("stats");

        if (stats == null) {
            stats = new HashMap<>();
        }

        Map<String, Integer> playerStats = (Map<String, Integer>) stats.getOrDefault(playerIdStr, new HashMap<>());
        int current = playerStats.getOrDefault(statType, 0);
        playerStats.put(statType, Math.max(0, current - 1));

        stats.put(playerIdStr, playerStats);
        instance.getDataTextFile().getConfig().set("stats", stats);
        saveData();
    }

    public static int getStat(UUID playerId, String statType) {
        String playerIdStr = playerId.toString();
        Map<String, Object> stats = (Map<String, Object>) instance.getDataTextFile().getConfig().get("stats");

        if (stats == null) {
            return 0;
        }

        Map<String, Integer> playerStats = (Map<String, Integer>) stats.getOrDefault(playerIdStr, new HashMap<>());
        return playerStats.getOrDefault(statType, 0);
    }
}
