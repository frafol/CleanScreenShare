package it.frafol.cleanss.bukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import it.frafol.cleanss.bukkit.commands.MainCommand;
import it.frafol.cleanss.bukkit.enums.SpigotCache;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.listeners.PlayerListener;
import it.frafol.cleanss.bukkit.listeners.WorldListener;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import it.frafol.cleanss.bukkit.objects.TextFile;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class CleanSS extends JavaPlugin implements PluginMessageListener {

	private TextFile configTextFile;
	private TextFile cacheTextFile;
	public static CleanSS instance;

	public static CleanSS getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {

		instance = this;

		getServer().getMessenger().registerIncomingPluginChannel(this, "cleanss:join", this);
		getServer().getMessenger().registerIncomingPluginChannel(this, "cleanss:reload", this);

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
		cacheTextFile = new TextFile(getDataFolder().toPath(), "cache_do_not_touch.yml");

		getLogger().info("Loading listeners...");
		Bukkit.getServer().getPluginManager().registerEvents(new MainCommand(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new WorldListener(), this);

		getLogger().info("Successfully loaded!");

		if (SpigotConfig.DAY_CYCLE.get(Boolean.class)) {

			for (World worlds : getServer().getWorlds()) {
				worlds.setGameRuleValue("doDaylightCycle", "false");
			}

		}

	}

	public void onDisable() {

		instance = null;
		getServer().getMessenger().unregisterIncomingPluginChannel(this, "cleanss:join");

		getLogger().info("Successfully disabled!");

	}

	public TextFile getConfigTextFile() {
		return configTextFile;
	}

	public TextFile getCacheTextFile() {
		return cacheTextFile;
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

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public void onPluginMessageReceived(@NotNull String channel, Player player, byte[] message) {

		if (!channel.equals("cleanss")) {
			return;
		}

		ByteArrayDataInput dataInput = ByteStreams.newDataInput(message);
		String subChannel = dataInput.readUTF();

		if (subChannel.equals("join")) {

			if (!SpigotConfig.SPAWN.get(Boolean.class)) {
				return;
			}

			String msg = dataInput.readUTF();

			if (msg.equals("SUSPECT")) {

				String player_found = dataInput.readUTF();

				getLogger().info("Player " + player_found + " was found in the suspect's database.");

				final Player final_player = Bukkit.getPlayer(player_found);
				Bukkit.getScheduler().runTaskLater(CleanSS.getInstance(), () -> final_player.teleport(PlayerCache.StringToLocation(SpigotCache.SUSPECT_SPAWN.get(String.class))), 10L);
				return;

			}

			if (msg.equals("ADMIN")) {

				String player_found = dataInput.readUTF();

				getLogger().info("Player " + player_found + " was found in the administrator's database.");

				final Player final_player = Bukkit.getPlayer(player_found);
				Bukkit.getScheduler().runTaskLater(CleanSS.getInstance(), () -> final_player.teleport(PlayerCache.StringToLocation(SpigotCache.ADMIN_SPAWN.get(String.class))), 10L);

			}
		}

		if (subChannel.equals("reload")) {

			getLogger().warning("CleanScreenShare is reloading on your proxy, running a global reload...");
			TextFile.reloadAll();

		}
	}
}