package it.frafol.cleanss.bukkit;

import it.frafol.cleanss.bukkit.listeners.PlayerListener;
import it.frafol.cleanss.bukkit.objects.TextFile;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CleanSS extends JavaPlugin {

	private TextFile configTextFile;
	public static CleanSS instance;

	public static CleanSS getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {

		instance = this;

		BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);

		Library yaml = Library.builder()
				.groupId("me{}carleslc{}Simple-YAML")
				.artifactId("Simple-Yaml")
				.version("1.8.3")
				.build();

		bukkitLibraryManager.addJitPack();
		bukkitLibraryManager.loadLibrary(yaml);

		getLogger().info("\n   ___  __    ____    __    _  _   ___  ___\n" +
				"  / __)(  )  ( ___)  /__\\  ( \\( ) / __)/ __)\n" +
				" ( (__  )(__  )__)  /(__)\\  )  (  \\__ \\\\__ \\\n" +
				"  \\___)(____)(____)(__)(__)(_)\\_) (___/(___/\n");

		getLogger().info("Server version: " + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".");

		if (getSuperLegacy()) {
			getLogger().severe("Support for your version was declined.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		getLogger().info("Loading configuration...");
		configTextFile = new TextFile(getDataFolder().toPath(), "settings.yml");

		getLogger().info("Loading listeners...");
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

		getLogger().info("Successfully loaded!");

	}

	public void onDisable() {

		instance = null;
		getLogger().info("Successfully disabled!");

	}

	public TextFile getConfigTextFile() {
		return configTextFile;
	}

	public boolean getSuperLegacy() {
		return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_6_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_5_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_4_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_3_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_2_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_1_R")
				|| Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_0_R");
	}
}