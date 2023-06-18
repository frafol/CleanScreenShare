package it.frafol.cleanss.bukkit;

import it.frafol.cleanss.bukkit.commands.MainCommand;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.enums.SpigotVersion;
import it.frafol.cleanss.bukkit.listeners.PlayerListener;
import it.frafol.cleanss.bukkit.listeners.PluginMessageReceiver;
import it.frafol.cleanss.bukkit.listeners.WorldListener;
import it.frafol.cleanss.bukkit.objects.TextFile;
import lombok.SneakyThrows;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.vyarus.yaml.updater.YamlUpdater;
import ru.vyarus.yaml.updater.util.FileUtils;

import java.io.File;

public class CleanSS extends JavaPlugin {

	private TextFile configTextFile;
	private TextFile cacheTextFile;
	private TextFile versionTextFile;
	public static CleanSS instance;

	public static CleanSS getInstance() {
		return instance;
	}

	@SneakyThrows
	@Override
	public void onEnable() {

		instance = this;

		BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);

		Library yaml = Library.builder()
				.groupId("me{}carleslc{}Simple-YAML")
				.artifactId("Simple-Yaml")
				.version("1.8.4")
				.build();

		Library updater = Library.builder()
				.groupId("ru{}vyarus")
				.artifactId("yaml-config-updater")
				.version("1.4.2")
				.build();

		bukkitLibraryManager.addJitPack();
		bukkitLibraryManager.addMavenCentral();
		bukkitLibraryManager.loadLibrary(updater);
		bukkitLibraryManager.loadLibrary(yaml);

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

		if (isFolia()) {
			getLogger().warning("Support for Folia has not been tested and is only for experimental purposes.");
		}

		getLogger().info("Loading configuration...");
		configTextFile = new TextFile(getDataFolder().toPath(), "settings.yml");
		versionTextFile = new TextFile(getDataFolder().toPath(), "version.yml");

		if (!getDescription().getVersion().equals(SpigotVersion.VERSION.get(String.class))) {

			getLogger().info("Creating new configurations...");
			YamlUpdater.create(new File(getDataFolder().toPath() + "/settings.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanScreenShare/main/src/main/resources/settings.yml"))
					.backup(true)
					.update();
			versionTextFile.getConfig().set("version", getDescription().getVersion());
			versionTextFile.getConfig().save();
			configTextFile = new TextFile(getDataFolder().toPath(), "settings.yml");

		}

		cacheTextFile = new TextFile(getDataFolder().toPath(), "cache_do_not_touch.yml");

		getLogger().info("Loading channels...");
		getServer().getMessenger().registerIncomingPluginChannel(this, "cleanss:join", new PluginMessageReceiver());

		getLogger().info("Loading listeners...");
		Bukkit.getServer().getPluginManager().registerEvents(new MainCommand(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new WorldListener(), this);

		getLogger().info("Successfully loaded!");

		if (SpigotConfig.DAY_CYCLE.get(Boolean.class)) {

			if (!isFolia()) {
				for (World worlds : getServer().getWorlds()) {
					worlds.setGameRuleValue("doDaylightCycle", "false");
				}
			} else {
				getLogger().severe("Cannot set 'doDaylightCycle' to 'false' in Folia.");
			}

		}
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
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}


	public TextFile getConfigTextFile() {
		return configTextFile;
	}

	public TextFile getCacheTextFile() {
		return cacheTextFile;
	}

	public TextFile getVersionTextFile() {
		return versionTextFile;
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
}