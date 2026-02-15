package it.frafol.cleanss.bungee;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class UpdateCheck {

    public final CleanSS PLUGIN;

    public UpdateCheck(CleanSS plugin) {
        this.PLUGIN = plugin;
    }

    public void getLatestUpdate(final BiConsumer<String, String> consumer) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://api.spiget.org/v2/resources/107548/updates/latest");
                InputStreamReader reader = new InputStreamReader(url.openStream());
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String title = json.get("title").getAsString();
                URL versionUrl = new URL("https://api.spigotmc.org/legacy/update.php?resource=107548");
                try (java.util.Scanner s = new java.util.Scanner(versionUrl.openStream())) {
                    String version = s.hasNext() ? s.next() : "Unknown";
                    consumer.accept(version, title);
                }
            } catch (IOException e) {
                PLUGIN.getLogger().severe("Unable to check for updates: " + e.getMessage());
            }
        });
    }
}