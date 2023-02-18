package it.frafol.cleanss.bungee;

import it.frafol.cleanss.bungee.commands.ControlCommand;
import it.frafol.cleanss.bungee.commands.FinishCommand;
import it.frafol.cleanss.bungee.commands.ReloadCommand;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.listeners.ChatListener;
import it.frafol.cleanss.bungee.listeners.CommandListener;
import it.frafol.cleanss.bungee.listeners.KickListener;
import it.frafol.cleanss.bungee.listeners.ServerListener;
import it.frafol.cleanss.bungee.objects.TextFile;
import net.byteflux.libby.BungeeLibraryManager;
import net.byteflux.libby.Library;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Map;

public class CleanSS extends Plugin {

    private TextFile messagesTextFile;
	private TextFile configTextFile;
	private static CleanSS instance;

	public static CleanSS getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {

		instance = this;

		loadLibraries();

		getLogger().info("\n§d   ___  __    ____    __    _  _    ___  ___\n" +
				"  / __)(  )  ( ___)  /__\\  ( \\( )  / __)/ __)\n" +
				" ( (__  )(__  )__)  /(__)\\  )  (   \\__ \\\\__ \\\n" +
				"  \\___)(____)(____)(__)(__)(_)\\_)  (___/(___/\n");

		getLogger().info("§7Loading §dconfiguration§7...");
		loadFiles();

		getLogger().info("§7Loading §dplugin§7...");

		getProxy().registerChannel("cleanss:join");
		registerCommands();
		registerListeners();

		if (BungeeConfig.STATS.get(Boolean.class) && !getDescription().getVersion().contains("alpha")) {

			new Metrics(this, 17063);

			getLogger().info("§7Metrics loaded §dsuccessfully§7!");
		}

		UpdateChecker();

		getLogger().info("§7Plugin §dsuccessfully §7loaded!");
	}

	public YamlFile getConfigTextFile() {
		return getInstance().configTextFile.getConfig();
	}

	public YamlFile getMessagesTextFile() {
		return getInstance().messagesTextFile.getConfig();
	}

	private void registerCommands() {

		getProxy().getPluginManager().registerCommand(this, new ControlCommand(this));
		getProxy().getPluginManager().registerCommand(this, new FinishCommand(this));
		getProxy().getPluginManager().registerCommand(this, new ReloadCommand());

	}

	private void loadFiles() {

		configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
		messagesTextFile = new TextFile(getDataFolder().toPath(), "messages.yml");

	}

	private void registerListeners() {

		getProxy().getPluginManager().registerListener(this, new ServerListener());
		getProxy().getPluginManager().registerListener(this, new CommandListener());

		if (BungeeMessages.CONTROL_CHAT.get(Boolean.class)) {
			getProxy().getPluginManager().registerListener(this, new ChatListener(this));
		}

		getProxy().getPluginManager().registerListener(this, new KickListener(this));
	}

	private void UpdateChecker() {
		if (BungeeConfig.UPDATE_CHECK.get(Boolean.class)) {
			new UpdateCheck(this).getVersion(version -> {
				if (!this.getDescription().getVersion().equals(version)) {
					getLogger().warning("§eThere is a new update available, download it on SpigotMC!");
				}
			});
		}
	}

	public void UpdateChecker(ProxiedPlayer player) {
		if (BungeeConfig.UPDATE_CHECK.get(Boolean.class)) {
			new UpdateCheck(this).getVersion(version -> {
				if (!this.getDescription().getVersion().equals(version)) {
					player.sendMessage(TextComponent.fromLegacyText("§e[CleanScreenShare] There is a new update available, download it on SpigotMC!"));
				}
			});
		}
	}

	private void loadLibraries() {

		BungeeLibraryManager bungeeLibraryManager = new BungeeLibraryManager(this);

		Library yaml = Library.builder()
				.groupId("me{}carleslc{}Simple-YAML")
				.artifactId("Simple-Yaml")
				.version("1.8.3")
				.build();

		bungeeLibraryManager.addJitPack();
		bungeeLibraryManager.loadLibrary(yaml);

	}

	@Override
	public void onDisable() {

		getLogger().info("§7Clearing §dinstances§7...");
		instance = null;
		getProxy().unregisterChannel("cleanss:join");

		getLogger().info("§7Plugin successfully §ddisabled§7!");
	}

	public <K, V> K getKey(@NotNull Map<K, V> map, V value) {

		for (Map.Entry<K, V> entry : map.entrySet()) {

			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}

		}
		return null;
	}

	public <K, V> V getValue(@NotNull Map<K, V> map, K key) {

		for (Map.Entry<K, V> entry : map.entrySet()) {

			if (entry.getKey().equals(key)) {
				return entry.getValue();
			}

		}
		return null;
	}
}