package it.frafol.cleanss.bukkit;

import com.tchristofferson.configupdater.ConfigUpdater;
import it.frafol.cleanss.bukkit.commands.MainCommand;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.enums.SpigotVersion;
import it.frafol.cleanss.bukkit.hooks.PlaceholderHook;
import it.frafol.cleanss.bukkit.listeners.PlayerListener;
import it.frafol.cleanss.bukkit.listeners.PluginMessageReceiver;
import it.frafol.cleanss.bukkit.listeners.WorldListener;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import it.frafol.cleanss.bukkit.objects.TextFile;
import lombok.Getter;
import lombok.SneakyThrows;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class CleanSS extends JavaPlugin {

	public boolean updated = false;

	private final HashMap<UUID, BukkitTask> timerTask = new HashMap<>();
	private final HashMap<UUID, Integer> seconds = new HashMap<>();

	@Getter
	private TextFile configTextFile;

	@Getter
	private TextFile cacheTextFile;

	@Getter
	private TextFile versionTextFile;

	@Getter
	public static CleanSS instance;

	@SneakyThrows
	@Override
	@SuppressWarnings("deprecation")
	public void onEnable() {

		instance = this;

		BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);

		Library yaml = Library.builder()
				.groupId("me{}carleslc{}Simple-YAML")
				.artifactId("Simple-Yaml")
				.version("1.8.4")
				.build();

		Library scoreboard = Library.builder()
				.groupId("me{}Stijn{}ScoreboardAPI")
				.artifactId("ScoreboardAPI")
				.version("0.0.1")
				.url("https://github.com/frafol/CleanScreenShare/raw/main/libs/ScoreboardAPI.jar")
				.build();

		try {
			bukkitLibraryManager.loadLibrary(yaml);
		} catch (RuntimeException ignored) {
			getLogger().severe("Failed to load Simple-YAML library. Trying to download it from GitHub...");
			yaml = Library.builder()
					.groupId("me{}carleslc{}Simple-YAML")
					.artifactId("Simple-Yaml")
					.version("1.8.4")
					.url("https://github.com/Carleslc/Simple-YAML/releases/download/1.8.4/Simple-Yaml-1.8.4.jar")
					.build();
		}

		bukkitLibraryManager.addJitPack();
		bukkitLibraryManager.loadLibrary(yaml);
		bukkitLibraryManager.loadLibrary(scoreboard);

		getLogger().info("\n   ___  __    ____    __    _  _   ___  ___\n" +
				"  / __)(  )  ( ___)  /__\\  ( \\( ) / __)/ __)\n" +
				" ( (__  )(__  )__)  /(__)\\  )  (  \\__ \\\\__ \\\n" +
				"  \\___)(____)(____)(__)(__)(_)\\_) (___/(___/\n");

		getLogger().info("Server version: " + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".");

		if (isSuperLegacy()) {
			getLogger().severe("Support for your version was declined.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		getLogger().info("Loading configuration...");
		configTextFile = new TextFile(getDataFolder().toPath(), "settings.yml");
		versionTextFile = new TextFile(getDataFolder().toPath(), "version.yml");
		File configFile = new File(getDataFolder(), "settings.yml");

		if (!getDescription().getVersion().equals(SpigotVersion.VERSION.get(String.class))) {

			getLogger().info("Creating new configurations...");
			try {
				ConfigUpdater.update(this, "settings.yml", configFile, Collections.emptyList());
			} catch (IOException ignored) {
				getLogger().severe("Unable to update configuration file, please remove the settings.yml!");
			}

			versionTextFile.getConfig().set("version", getDescription().getVersion());
			versionTextFile.getConfig().save();
			configTextFile = new TextFile(getDataFolder().toPath(), "settings.yml");

		}

		cacheTextFile = new TextFile(getDataFolder().toPath(), "cache_do_not_touch.yml");

		getLogger().info("Loading channels...");
		getServer().getMessenger().registerIncomingPluginChannel(this, "cleanss:join", new PluginMessageReceiver());

		getLogger().info("Loading listeners...");
		getServer().getPluginManager().registerEvents(new MainCommand(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new WorldListener(), this);

		if (isPAPI()) {
			new PlaceholderHook(this).register();
			getLogger().info("PlaceholderAPI found, placeholders enabled.");
		}

		UpdateChecker();

		if (SpigotConfig.DAY_CYCLE.get(Boolean.class)) {
			if (!isFolia()) {
				for (World worlds : getServer().getWorlds()) {
					worlds.setGameRuleValue("doDaylightCycle", "false");
				}
			} else {
				getLogger().severe("Cannot disable daylight cycle on Folia, please disable it manually.");
			}
		}

		if (SpigotConfig.SB_STAFF.get(Boolean.class) || SpigotConfig.SB_SUSPECT.get(Boolean.class)) {
			PlayerCache.updateScoreboardTask();
		}

		if (SpigotConfig.TABLIST_STAFF.get(Boolean.class) || SpigotConfig.TABLIST_SUSPECT.get(Boolean.class)) {
			PlayerCache.updateTabListTask();
		}

		getLogger().info("Successfully loaded!");
	}

	public void onDisable() {

		instance = null;
		getServer().getMessenger().unregisterIncomingPluginChannel(this, "cleanss:join");
		getServer().getMessenger().unregisterIncomingPluginChannel(this, "cleanss:reload");

		getLogger().info("Successfully disabled!");
	}

	public static boolean isFolia() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
		} catch (ClassNotFoundException ignored) {
			return false;
		}
		return true;
	}


	public boolean isSuperLegacy() {
		return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_6_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_5_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_4_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_3_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_2_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_1_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_0_R");
	}

	private void UpdateChecker() {

		if (!SpigotConfig.UPDATE_CHECK.get(Boolean.class)) {
			return;
		}

		new UpdateCheck(this).getVersion(version -> {

			if (Integer.parseInt(getDescription().getVersion().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {

				if (SpigotConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
					autoUpdate();
					return;
				}

				if (!updated) {
					getLogger().warning("There is a new update available, download it on SpigotMC!");
				}
			}

			if (Integer.parseInt(getDescription().getVersion().replace(".", "")) > Integer.parseInt(version.replace(".", ""))) {
				getLogger().warning("You are using a development version, please report any bugs!");
			}

		});
	}

	public boolean isWindows() {
		String os = System.getProperty("os.name");
		return os.startsWith("Windows");
	}

	public void autoUpdate() {

		if (isWindows()) {
			getLogger().warning("Auto update is not supported on Windows.");
			return;
		}

		try {
			String fileUrl = "https://github.com/frafol/CleanScreenShare/releases/download/release/CleanScreenShare.jar";
			String destination = "./plugins/";

			String fileName = getFileNameFromUrl(fileUrl);
			File outputFile = new File(destination, fileName);

			downloadFile(fileUrl, outputFile);
			updated = true;
			getLogger().warning("CleanScreenShare successfully updated, a restart is required.");

		} catch (IOException ignored) {
			getLogger().severe("Unable to update CleanScreenShare, please download it manually from SpigotMC.");
		}
	}

	private String getFileNameFromUrl(String fileUrl) {
		int slashIndex = fileUrl.lastIndexOf('/');
		if (slashIndex != -1 && slashIndex < fileUrl.length() - 1) {
			return fileUrl.substring(slashIndex + 1);
		}
		throw new IllegalArgumentException("Invalid file URL");
	}

	private void downloadFile(String fileUrl, File outputFile) throws IOException {
		URL url = new URL(fileUrl);
		try (InputStream inputStream = url.openStream()) {
			Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public boolean isPAPI() {
		return getServer().getPluginManager().getPlugin("PlaceholderAPI") != null && SpigotConfig.PAPI_HOOK.get(Boolean.class);
	}

	public void startTimer(UUID uuid) {
		timerTask.put(uuid, instance.getServer().getScheduler().runTaskTimer(instance, () -> {
			seconds.putIfAbsent(uuid, 1);
			seconds.put(uuid, seconds.get(uuid) + 1);
		}, 20L, 20L));
	}

	public void stopTimer(UUID uuid) {
		if (timerTask.get(uuid) != null) {
			timerTask.get(uuid).cancel();
			seconds.remove(uuid);
			timerTask.remove(uuid);
		}
	}

	public Integer getSeconds(UUID uuid) {
		return seconds.get(uuid);
	}

	public String getFormattedSeconds(UUID uuid) {
		if (seconds.get(uuid) == null) {
			return "00:00";
		}

		if (seconds.get(uuid) > 86400) {
			return DurationFormatUtils.formatDuration(seconds.get(uuid) * 1000L, "dd:HH:mm:ss");
		}

		if (seconds.get(uuid) > 3600) {
			return DurationFormatUtils.formatDuration(seconds.get(uuid) * 1000L, "HH:mm:ss");
		}

		return DurationFormatUtils.formatDuration(seconds.get(uuid) * 1000L, "mm:ss");
	}
}