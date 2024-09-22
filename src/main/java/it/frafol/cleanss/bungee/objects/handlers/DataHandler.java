package it.frafol.cleanss.bungee.objects.handlers;

import it.frafol.cleanss.bungee.CleanSS;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class DataHandler {

    private final CleanSS instance = CleanSS.getInstance();

    @SneakyThrows
    public static void saveData() {
        instance.getDataTextFile().save();
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

    public static void incrementStat(UUID playerId, String statType) {
        int current = instance.getDataTextFile().getInt("stats." + playerId + "." + statType);
        instance.getDataTextFile().set("stats." + playerId + "." + statType, current + 1);
        saveData();
    }

    public static void decrementStat(UUID playerId, String statType) {
        int current = instance.getDataTextFile().getInt("stats." + playerId + "." + statType);
        instance.getDataTextFile().set("stats." + playerId + "." + statType, current - 1);
        saveData();
    }

    public static int getStat(UUID playerId, String statType) {
        return instance.getDataTextFile().getInt("stats." + playerId + "." + statType);
    }
}
