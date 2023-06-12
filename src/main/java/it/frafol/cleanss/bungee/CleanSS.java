package it.frafol.cleanss.bungee;

import it.frafol.cleanss.bungee.commands.ControlCommand;
import it.frafol.cleanss.bungee.commands.FinishCommand;
import it.frafol.cleanss.bungee.commands.InfoCommand;
import it.frafol.cleanss.bungee.commands.ReloadCommand;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.listeners.ChatListener;
import it.frafol.cleanss.bungee.listeners.CommandListener;
import it.frafol.cleanss.bungee.listeners.KickListener;
import it.frafol.cleanss.bungee.listeners.ServerListener;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.TextFile;
import it.frafol.cleanss.bungee.mysql.MySQLWorker;
import net.byteflux.libby.BungeeLibraryManager;
import net.byteflux.libby.Library;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CleanSS extends Plugin {

    private TextFile messagesTextFile;
	private TextFile configTextFile;
	private JDA jda;
	private static CleanSS instance;

	private MySQLWorker data;

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

		if (BungeeConfig.MYSQL.get(Boolean.class)) {
			data = new MySQLWorker();
			ControlTask();
		}

		if (BungeeConfig.DISCORD_ENABLED.get(Boolean.class)) {
			loadDiscord();
		}

		if (BungeeConfig.STATS.get(Boolean.class) && !getDescription().getVersion().contains("alpha")) {
			new Metrics(this, 17063);
			getLogger().info("§7Metrics loaded §dsuccessfully§7!");
		}

		if (isLiteBans()) {
			getLogger().info("§7LiteBans hooked §dsuccessfully§7!");
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
		getProxy().getPluginManager().registerCommand(this, new InfoCommand(this));
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

		if (!BungeeConfig.UPDATE_CHECK.get(Boolean.class)) {
			return;
		}

		new UpdateCheck(this).getVersion(version -> {

			if (Integer.parseInt(getDescription().getVersion().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {
				getLogger().warning("§eThere is a new update available, download it on SpigotMC!");
			}

			if (Integer.parseInt(getDescription().getVersion().replace(".", "")) > Integer.parseInt(version.replace(".", ""))) {
				getLogger().warning("§eYou are using a development version, please report any bugs!");
			}

		});
	}

	public void ControlTask() {

		instance.getProxy().getScheduler().schedule(this, () -> {

			for (ProxiedPlayer players : getProxy().getPlayers()) {
				PlayerCache.getIn_control().put(players.getUniqueId(), data.getStats(players.getUniqueId(), "incontrol"));
				PlayerCache.getControls().put(players.getUniqueId(), data.getStats(players.getUniqueId(), "controls"));
				PlayerCache.getControls_suffered().put(players.getUniqueId(), data.getStats(players.getUniqueId(), "controls"));
			}

		}, 1L, 1L, TimeUnit.SECONDS);

	}

	public void UpdateChecker(ProxiedPlayer player) {

		if (!BungeeConfig.UPDATE_CHECK.get(Boolean.class)) {
			return;
		}

		new UpdateCheck(this).getVersion(version -> {

			if (Integer.parseInt(getDescription().getVersion().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {
				player.sendMessage(TextComponent.fromLegacyText("§e[CleanScreenShare] There is a new update available, download it on SpigotMC!"));
			}

		});
	}

	private void loadDiscord() {
		jda = JDABuilder.createDefault(BungeeConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
		updateJDA();
	}

	public JDA getJda() {
		return jda;
	}

	public boolean isLiteBans() {
		return getProxy().getPluginManager().getPlugin("LiteBans") != null;
	}

	public void updateJDA() {

		if (!BungeeConfig.DISCORD_ENABLED.get(Boolean.class)) {
			return;
		}

		if (jda == null) {
			getLogger().severe("Fatal error while updating JDA. Please report this error to discord.io/futurevelopment.");
			return;
		}

		jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf
						(BungeeConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()), BungeeConfig.DISCORD_ACTIVITY.get(String.class)
				.replace("%players%", String.valueOf(getProxy().getOnlineCount()))
				.replace("%suspiciouses%", String.valueOf(PlayerCache.getSuspicious().size()))));

	}

	private void loadLibraries() {

		BungeeLibraryManager bungeeLibraryManager = new BungeeLibraryManager(this);

		Library yaml = Library.builder()
				.groupId("me{}carleslc{}Simple-YAML")
				.artifactId("Simple-Yaml")
				.version("1.8.4")
				.build();

		Library discord = Library.builder()
				.groupId("net{}dv8tion")
				.artifactId("JDA")
				.version("5.0.0-beta.10")
				.url("https://github.com/DV8FromTheWorld/JDA/releases/download/v5.0.0-beta.10/JDA-5.0.0-beta.10-withDependencies-min.jar")
				.build();

		bungeeLibraryManager.addMavenCentral();
		bungeeLibraryManager.addJitPack();
		bungeeLibraryManager.loadLibrary(yaml);
		bungeeLibraryManager.loadLibrary(discord);

	}

	@Override
	public void onDisable() {

		getLogger().info("§7Clearing §dinstances§7...");
		instance = null;
		getProxy().unregisterChannel("cleanss:join");

		if (BungeeConfig.MYSQL.get(Boolean.class)) {
			getLogger().info("§7Closing §ddatabase§7...");
			data.close();
		}

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

	public MySQLWorker getData() {
		return data;
	}

	public void setData() {
		data = new MySQLWorker();
	}

}