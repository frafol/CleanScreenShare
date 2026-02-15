package it.frafol.cleanss.velocity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.event.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UpdateCheck {

    public CleanSS PLUGIN;

    public UpdateCheck(CleanSS plugin) {
        this.PLUGIN = plugin;
    }

    public void getLatestUpdate(final BiConsumer<String, String> consumer) {
        PLUGIN.getServer().getScheduler().buildTask(PLUGIN, () -> {
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
                PLUGIN.getLogger().error("Unable to check for updates: " + e.getMessage());
            }
        }).schedule();
    }
}