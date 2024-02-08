package it.frafol.cleanss.velocity;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.VelocityLibraryManager;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.commands.*;
import it.frafol.cleanss.velocity.enums.*;
import it.frafol.cleanss.velocity.listeners.ChatListener;
import it.frafol.cleanss.velocity.listeners.CommandListener;
import it.frafol.cleanss.velocity.listeners.KickListener;
import it.frafol.cleanss.velocity.listeners.ServerListener;
import it.frafol.cleanss.velocity.mysql.MySQLWorker;
import it.frafol.cleanss.velocity.objects.*;
import it.frafol.cleanss.velocity.objects.adapter.ReflectUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;
import ru.vyarus.yaml.updater.YamlUpdater;
import ru.vyarus.yaml.updater.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Getter
@Plugin(
		id = "cleanscreenshare",
		name = "CleanScreenShare",
		version = "2.4.2",
		description = "Make control hacks on your players.",
		dependencies = {@Dependency(id = "luckperms", optional = true), @Dependency(id = "mysqlandconfigurateforvelocity", optional = true), @Dependency(id = "limboapi", optional = true), @Dependency(id = "ajqueue", optional = true), @Dependency(id = "premiumvanish", optional = true), @Dependency(id = "velocityvanish", optional = true)},
		authors = { "frafol" })

public class CleanSS {

	public static final ChannelIdentifier channel_join = MinecraftChannelIdentifier.create("cleanss", "join");

	public boolean mysql_installation = false;
	public boolean updated = false;

	private final Logger logger;
	private final ProxyServer server;
	private final Path path;
	private final Metrics.Factory metricsFactory;

	private final JdaBuilder jda = new JdaBuilder();

    private TextFile messagesTextFile;
	private TextFile configTextFile;
	private TextFile aliasesTextFile;
	private TextFile limboTextFile;
	private TextFile versionTextFile;

	public boolean useLimbo = false;

	@Getter
	@Setter
	private boolean ajQueue = false;

	@Getter
	private static CleanSS instance;

	@Getter
	private MySQLWorker data;

	@Inject
	public CleanSS(Logger logger, ProxyServer server, @DataDirectory Path path, Metrics.Factory metricsFactory) {
		this.server = server;
		this.logger = logger;
		this.path = path;
		this.metricsFactory = metricsFactory;
	}

	@Inject
	public PluginContainer container;

	@SneakyThrows
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {

		instance = this;

		loadLibraries();

		if (mysql_installation) {
			return;
		}

		logger.info("\n   ___  __    ____    __    _  _   ___  ___\n" +
				"  / __)(  )  ( ___)  /__\\  ( \\( ) / __)/ __)\n" +
				" ( (__  )(__  )__)  /(__)\\  )  (  \\__ \\\\__ \\\n" +
				"  \\___)(____)(____)(__)(__)(_)\\_) (___/(___/\n");

		logger.info("Server version: " + getServerBrand() + " - " + getServerVersion());

		logger.info("Loading configuration...");
		loadFiles();
		updateConfig();

		logger.info("Loading plugin...");
		loadChannelRegistrar();
		loadListeners();
		loadCommands();
		loadDiscord();

		if (VelocityLimbo.USE.get(Boolean.class)) {
			if (instance.getServer().getPluginManager().getPlugin("limboapi").isPresent() &&
					instance.getServer().getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).isPresent()) {
				LimboUtils.loadLimbo();
			} else {
				logger.error("LimboAPI not found! Please install it to use the Limbo feature.");
			}
		}

		if (getPremiumVanish() && VelocityConfig.PREMIUMVANISH.get(Boolean.class)) {
			logger.info("PremiumVanish hooked successfully!");
		}

		if (instance.getServer().getPluginManager().getPlugin("ajqueue").isPresent() &&
				instance.getServer().getPluginManager().getPlugin("ajqueue").flatMap(PluginContainer::getInstance).isPresent()) {
			ajQueue = true;
		}

		if (VelocityConfig.MYSQL.get(Boolean.class)) {

			loadLibrariesSQL();

			if (mysql_installation) {
				server.shutdown();
				return;
			}

			if (ReflectUtil.getClass("com.mysql.cj.jdbc.Driver") == null) {
				return;
			}

			data = new MySQLWorker();

			if (mysql_installation) {
				server.shutdown();
				return;
			}

			ControlTask();
		}

		if (VelocityConfig.STATS.get(Boolean.class)) {
			metricsFactory.make(this, 16951);
			logger.info("Metrics loaded successfully!");
		}

		UpdateChecker();
		startTasks();
		logger.info("Plugin successfully loaded!");
	}

	@Subscribe
	public void onProxyShutdown(ProxyShutdownEvent event) {

		stopTasks();

		if (getConfigTextFile() == null || VelocityConfig.MYSQL.get(Boolean.class)) {

			logger.info("Closing database...");
			for (Player players : server.getAllPlayers()) {
				if (data != null) {
					data.setInControl(players.getUniqueId(), 0);
					data.setControls(players.getUniqueId(), PlayerCache.getControls().get(players.getUniqueId()));
				}
			}

			if (data != null) {
				data.close();
			}

		}


		logger.info("§7Clearing §dinstances§7...");
		instance = null;

		logger.info("§7Plugin successfully §ddisabled§7!");
	}

	private void startTasks() {
		List<Optional<RegisteredServer>> servers = Utils.getServerList(VelocityConfig.CONTROL.getStringList());
		List<Optional<RegisteredServer>> fallbacks = Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList());

		for (Optional<RegisteredServer> server : servers) {
            server.ifPresent(Utils::startTask);
		}

		for (Optional<RegisteredServer> fallback : fallbacks) {
			fallback.ifPresent(Utils::startTask);
		}
	}

	private void stopTasks() {
		List<Optional<RegisteredServer>> servers = Utils.getServerList(VelocityConfig.CONTROL.getStringList());
		List<Optional<RegisteredServer>> fallbacks = Utils.getServerList(VelocityConfig.CONTROL_FALLBACK.getStringList());

		for (Optional<RegisteredServer> server : servers) {
			server.ifPresent(Utils::stopTask);
		}

		for (Optional<RegisteredServer> fallback : fallbacks) {
			fallback.ifPresent(Utils::stopTask);
		}
	}

	public void setData() {

		loadLibrariesSQL();

		if (mysql_installation) {
			server.shutdown();
			return;
		}

		if (ReflectUtil.getClass("com.mysql.cj.jdbc.Driver") == null) {
			return;
		}

		data = new MySQLWorker();

		if (mysql_installation) {
			server.shutdown();
		}

	}

	public void loadLibrariesSQL() {
		try {
			String fileUrl = "https://github.com/frafol/CleanScreenShare/releases/download/release/MySQL-And-Configurate-For-Velocity-1.0.1-RELEASE.jar";
			String destination = "./plugins/";

			String fileName = getFileNameFromUrl(fileUrl);
			File outputFile = new File(destination, fileName);

			if (!outputFile.exists()) {
				downloadFile(fileUrl, outputFile);
				mysql_installation = true;
				logger.warn("MySQL drivers (" + fileName + ") are now successfully installed. A restart is required.");
			}

		} catch (IOException ignored) {
			logger.error("An error occurred while downloading MySQL drivers, plugin may not work properly.");
		}
	}

	private String getServerBrand() {
		return server.getVersion().getName();
	}

	private String getServerVersion() {
		return server.getVersion().getVersion();
	}

	public void autoUpdate() {

		if (isWindows()) {
			logger.warn("Auto update is not supported on Windows.");
			return;
		}

		new UpdateCheck(this).getVersion(version -> {
			String fileUrl = "https://github.com/frafol/CleanScreenShare/releases/download/release/cleanscreenshare-"+ version + " .jar";
			String destination = "./plugins/";

			String fileName = getFileNameFromUrl(fileUrl);
			File outputFile = new File(destination, fileName);

			try {
				downloadFile(fileUrl, outputFile);
			} catch (IOException ignored) {
				logger.warn("An error occurred while downloading the update, please download it manually from SpigotMC.");
			}

			updated = true;
			logger.warn("CleanScreenShare successfully updated, a restart is required.");
		});
	}

	private void loadLibraries() {
		VelocityLibraryManager<CleanSS> velocityLibraryManager = new VelocityLibraryManager<>(this, getLogger(), path, getServer().getPluginManager());

		final Relocation yamlrelocation = new Relocation("yaml", "it{}frafol{}libs{}yaml");
		Library yaml = Library.builder()
				.groupId("me{}carleslc{}Simple-YAML")
				.artifactId("Simple-Yaml")
				.version("1.8.4")
				.relocate(yamlrelocation)
				.build();

		final Relocation updaterrelocation = new Relocation("updater", "it{}frafol{}libs{}updater");
		Library updater = Library.builder()
				.groupId("ru{}vyarus")
				.artifactId("yaml-config-updater")
				.version("1.4.2")
				.relocate(updaterrelocation)
				.build();

		final Relocation kotlin = new Relocation("kotlin", "it{}frafol{}libs{}kotlin");
		Library discord = Library.builder()
				.groupId("net{}dv8tion")
				.artifactId("JDA")
				.version("5.0.0-beta.13")
				.relocate(kotlin)
				.url("https://github.com/DV8FromTheWorld/JDA/releases/download/v5.0.0-beta.13/JDA-5.0.0-beta.13-withDependencies-min.jar")
				.build();

		velocityLibraryManager.addMavenCentral();
		velocityLibraryManager.addJitPack();

		try {
			velocityLibraryManager.loadLibrary(yaml);
		} catch (RuntimeException ignored) {
			logger.error("Failed to load Simple-YAML library. Trying to download it from GitHub...");
			yaml = Library.builder()
					.groupId("me{}carleslc{}Simple-YAML")
					.artifactId("Simple-Yaml")
					.version("1.8.4")
					.relocate(yamlrelocation)
					.url("https://github.com/Carleslc/Simple-YAML/releases/download/1.8.4/Simple-Yaml-1.8.4.jar")
					.build();
		}

		velocityLibraryManager.loadLibrary(yaml);
		velocityLibraryManager.loadLibrary(updater);
		velocityLibraryManager.loadLibrary(discord);
	}

	public boolean isWindows() {
		String os = System.getProperty("os.name");
		return os.startsWith("Windows");
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
			deleteFile(outputFile.getParent(), "cleanscreenshare-");
			Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	@SneakyThrows
	public void deleteFile(String directoryPath, String file_start) {
		File directory = new File(directoryPath);

		if (!directory.isDirectory()) {
			throw new IllegalArgumentException();
		}

		File[] files = directory.listFiles();
		if (files == null) {
			throw new IOException();
		}

		for (File file : files) {
			if (file.isFile() && file.getName().startsWith(file_start)) {
				logger.warn("Found an old plugin file: " + file.getName());
				if (file.delete()) {
					logger.warn("Deleted old file: " + file.getName());
				}
			}
		}
	}

	private void loadFiles() {
		configTextFile = new TextFile(path, "config.yml");
		messagesTextFile = new TextFile(path, "messages.yml");
		aliasesTextFile = new TextFile(path, "aliases.yml");
		limboTextFile = new TextFile(path, "limboapi.yml");
		versionTextFile = new TextFile(path, "version.yml");
	}

	@SneakyThrows
	private void updateConfig() {

		if (container.getDescription().getVersion().isPresent() && (!container.getDescription().getVersion().get().equals(VelocityVersion.VERSION.get(String.class)))) {

			logger.info("Creating new configurations...");
			YamlUpdater.create(new File(path + "/config.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanScreenShare/main/src/main/resources/config.yml"))
					.backup(true)
					.update();
			YamlUpdater.create(new File(path + "/messages.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanScreenShare/main/src/main/resources/messages.yml"))
					.backup(true)
					.update();
			YamlUpdater.create(new File(path + "/aliases.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanScreenShare/main/src/main/resources/aliases.yml"))
					.backup(true)
					.update();
			YamlUpdater.create(new File(path + "/limboapi.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanScreenShare/main/src/main/resources/limboapi.yml"))
					.backup(true)
					.update();
			versionTextFile.getConfig().set("version", container.getDescription().getVersion().get());
			versionTextFile.getConfig().save();
			loadFiles();
		}
	}

	private void loadCommands() {

		getServer().getCommandManager().register
				(server.getCommandManager().metaBuilder("ssdebug").aliases("cleanssdebug", "controldebug")
						.build(), new DebugCommand(this));

		final String[] aliases_ss = VelocityCommandsConfig.SS_PLAYER.getStringList().toArray(new String[0]);
		server.getCommandManager().register(server.getCommandManager()
				.metaBuilder(VelocityCommandsConfig.SS_PLAYER.getStringList().get(0))
				.aliases(aliases_ss)
				.build(), new ControlCommand(this));

		final String[] aliases_ssfinish = VelocityCommandsConfig.SS_FINISH.getStringList().toArray(new String[0]);
		server.getCommandManager().register(server.getCommandManager()
				.metaBuilder(VelocityCommandsConfig.SS_FINISH.getStringList().get(0))
				.aliases(aliases_ssfinish)
				.build(), new FinishCommand(this));

		if (VelocityConfig.ENABLE_SPECTATING.get(Boolean.class)) {
			final String[] aliases_spectate = VelocityCommandsConfig.SS_SPECTATE.getStringList().toArray(new String[0]);
			server.getCommandManager().register(server.getCommandManager()
					.metaBuilder(VelocityCommandsConfig.SS_SPECTATE.getStringList().get(0))
					.aliases(aliases_spectate)
					.build(), new SpectateCommand(this));
		}

		final String[] aliases_info = VelocityCommandsConfig.SS_INFO.getStringList().toArray(new String[0]);
		server.getCommandManager().register(server.getCommandManager()
				.metaBuilder(VelocityCommandsConfig.SS_INFO.getStringList().get(0))
				.aliases(aliases_info)
				.build(), new InfoCommand(this));

		getServer().getCommandManager().register
				(server.getCommandManager().metaBuilder("ssreload").aliases("cleanssreload", "controlreload")
						.build(), new ReloadCommand(this));
	}

	private void loadChannelRegistrar() {
		server.getChannelRegistrar().register(channel_join);
	}

	private void loadListeners() {

		server.getEventManager().register(this, new ServerListener(this));
		server.getEventManager().register(this, new CommandListener(this));

		if (VelocityMessages.CONTROL_CHAT.get(Boolean.class)) {
			server.getEventManager().register(this, new ChatListener(this));
		}

		server.getEventManager().register(this, new KickListener(this));

	}

	private void loadDiscord() {
		if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class)) {
			jda.startJDA();
			updateTaskJDA();
			getLogger().info("Hooked into Discord successfully!");
		}
	}

	private void UpdateChecker() {

		if (!VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
			return;
		}

		if (!container.getDescription().getVersion().isPresent()) {
			return;
		}

		new UpdateCheck(this).getVersion(version -> {

			if (Integer.parseInt(container.getDescription().getVersion().get().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {

				if (VelocityConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
					autoUpdate();
					return;
				}

				if (!updated) {
					logger.warn("There is a new update available, download it on SpigotMC!");
				}
			}

			if (Integer.parseInt(container.getDescription().getVersion().get().replace(".", "")) > Integer.parseInt(version.replace(".", ""))) {
				logger.warn("You are using a development version, please report any bugs!");
			}

		});
	}

	public void ControlTask() {

		instance.getServer().getScheduler().buildTask(this, () -> {

			for (Player players : server.getAllPlayers()) {
				PlayerCache.getIn_control().put(players.getUniqueId(), data.getStats(players.getUniqueId(), "incontrol"));
				PlayerCache.getControls().put(players.getUniqueId(), data.getStats(players.getUniqueId(), "controls"));
				PlayerCache.getControls_suffered().put(players.getUniqueId(), data.getStats(players.getUniqueId(), "suffered"));
			}

		}).repeat(1, TimeUnit.SECONDS).schedule();

	}

	public void UpdateChecker(Player player) {

		if (!VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
			return;
		}

		if (!container.getDescription().getVersion().isPresent()) {
			return;
		}

		new UpdateCheck(this).getVersion(version -> {

			if (!(Integer.parseInt(container.getDescription().getVersion().get().replace(".", ""))
					< Integer.parseInt(version.replace(".", "")))) {
				return;
			}

			if (VelocityConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
				autoUpdate();
				return;
			}

			if (!updated) {
				player.sendMessage(LegacyComponentSerializer.legacy('§')
						.deserialize("§e[CleanScreenShare] There is a new update available, download it on SpigotMC!"));
			}

		});
	}

	private void updateTaskJDA() {
		getServer().getScheduler().buildTask(this, this::UpdateJDA).repeat(30, TimeUnit.SECONDS).schedule();
	}

	@SneakyThrows
	public void UpdateJDA() {

		if (!VelocityConfig.DISCORD_ENABLED.get(Boolean.class)) {
			return;
		}

		if (jda.getJda() == null) {
			logger.error("Fatal error while updating JDA, please report this error on https://discord.com/invite/sTSwaGBCdC.");
			return;
		}

		if (getVelocityVanish() && VelocityConfig.VELOCITYVANISH.get(Boolean.class)) {
			jda.getJda().getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.of(net.dv8tion.jda.api.entities.Activity.ActivityType.valueOf
							(VelocityConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
					VelocityConfig.DISCORD_ACTIVITY.get(String.class)
							.replace("%players%", String.valueOf(VelocityVanishUtils.getOnlinePlayers(this)))
							.replace("%suspiciouses%", String.valueOf(PlayerCache.getSuspicious().size()))));
			return;
		}

		if (getPremiumVanish() && VelocityConfig.PREMIUMVANISH.get(Boolean.class)) {
			jda.getJda().getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.of(net.dv8tion.jda.api.entities.Activity.ActivityType.valueOf
							(VelocityConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
					VelocityConfig.DISCORD_ACTIVITY.get(String.class)
							.replace("%players%", String.valueOf(PremiumVanishUtils.getOnlinePlayers(this)))
							.replace("%suspiciouses%", String.valueOf(PlayerCache.getSuspicious().size()))));
			return;
		}

		jda.getJda().getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.of(net.dv8tion.jda.api.entities.Activity.ActivityType.valueOf
						(VelocityConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
				VelocityConfig.DISCORD_ACTIVITY.get(String.class)
						.replace("%players%", String.valueOf(server.getAllPlayers().size()))
						.replace("%suspiciouses%", String.valueOf(PlayerCache.getSuspicious().size()))));

	}

	public <K, V> K getKey(Map<K, V> map, V value) {

		for (Map.Entry<K, V> entry : map.entrySet()) {

			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}

		}
		return null;
	}

	public <K, V> V getValue(Map<K, V> map, K key) {

		for (Map.Entry<K, V> entry : map.entrySet()) {

			if (entry.getKey().equals(key)) {
				return entry.getValue();
			}

		}
		return null;
	}

	@SuppressWarnings("ALL")
	public boolean getUnsignedVelocityAddon() {
		return getServer().getPluginManager().isLoaded("unsignedvelocity");
	}

	@SuppressWarnings("ALL")
	public boolean getPremiumVanish() {
		return getServer().getPluginManager().isLoaded("premiumvanish");
	}

	@SuppressWarnings("ALL")
	public boolean getVelocityVanish() {
		return getServer().getPluginManager().isLoaded("velocityvanish");
	}
}