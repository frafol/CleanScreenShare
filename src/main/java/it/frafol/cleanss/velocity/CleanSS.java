package it.frafol.cleanss.velocity;

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
import it.frafol.cleanss.velocity.commands.ControlCommand;
import it.frafol.cleanss.velocity.commands.FinishCommand;
import it.frafol.cleanss.velocity.commands.InfoCommand;
import it.frafol.cleanss.velocity.commands.ReloadCommand;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.listeners.ChatListener;
import it.frafol.cleanss.velocity.listeners.CommandListener;
import it.frafol.cleanss.velocity.listeners.KickListener;
import it.frafol.cleanss.velocity.listeners.ServerListener;
import it.frafol.cleanss.velocity.mysql.MySQLWorker;
import it.frafol.cleanss.velocity.objects.JdaBuilder;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.TextFile;
import it.frafol.cleanss.velocity.objects.adapter.ReflectUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
@Plugin(
		id = "cleanscreenshare",
		name = "CleanScreenShare",
		version = "1.2",
		description = "Make control hacks on your players.",
		dependencies = {@Dependency(id = "mysqlandconfigurateforvelocity", optional = true), @Dependency(id = "litebans", optional = true)},
		authors = { "frafol" })

public class CleanSS {

	public static final ChannelIdentifier channel_join = MinecraftChannelIdentifier.create("cleanss", "join");

	public boolean mysql_installation = false;

	private final Logger logger;
	private final ProxyServer server;
	private final Path path;
	private final Metrics.Factory metricsFactory;

	private final JdaBuilder jda = new JdaBuilder();

    private TextFile messagesTextFile;
	private TextFile configTextFile;

	private static CleanSS instance;

	private MySQLWorker data;

	public static CleanSS getInstance() {
		return instance;
	}

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

		logger.info("\n§d   ___  __    ____    __    _  _   ___  ___\n" +
				"  / __)(  )  ( ___)  /__\\  ( \\( ) / __)/ __)\n" +
				" ( (__  )(__  )__)  /(__)\\  )  (  \\__ \\\\__ \\\n" +
				"  \\___)(____)(____)(__)(__)(_)\\_) (___/(___/\n");

		logger.info("§7Loading §dconfiguration§7...");
		loadFiles();

		logger.info("§7Loading §dplugin§7...");
		loadChannelRegistrar();
		loadListeners();
		loadCommands();
		loadDiscord();

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
			logger.info("§7Metrics loaded §dsuccessfully§7!");

		}

		if (!getUnsignedVelocityAddon()) {
			logger.warn("To get the full functionality of CleanScreenShare for versions 1.19.1 and later on Velocity, " +
					"consider downloading https://github.com/4drian3d/UnSignedVelocity/releases/latest");
		} else {
			logger.info("§7UnsignedVelocity hooked §dsuccessfully§7!");
		}

		if (isLiteBans()) {
			logger.info("§7LiteBans hooked §dsuccessfully§7!");
		}

		UpdateChecker();
		logger.info("§7Plugin §dsuccessfully §7loaded!");

	}

	@Subscribe
	public void onProxyShutdown(ProxyShutdownEvent event) {

		if (getConfigTextFile() == null || VelocityConfig.MYSQL.get(Boolean.class)) {

			logger.info("§7Closing §ddatabase§7...");
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

	public MySQLWorker getData() {
		return data;
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
			String fileUrl = "https://simonsator.de/repo/de/simonsator/MySQL-And-Configurate-For-Velocity/1.0.1-RELEASE/MySQL-And-Configurate-For-Velocity-1.0.1-RELEASE.jar";
			String destination = "./plugins/";

			String fileName = getFileNameFromUrl(fileUrl);
			File outputFile = new File(destination, fileName);

			if (!outputFile.exists()) {
				downloadFile(fileUrl, outputFile);
				mysql_installation = true;
				logger.warn("MySQL drivers (" + fileName + ") are now successfully installed. A restart is required.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadLibraries() {
		VelocityLibraryManager<CleanSS> velocityLibraryManager = new VelocityLibraryManager<>(getLogger(), path, getServer().getPluginManager(), this);

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

		velocityLibraryManager.addMavenCentral();
		velocityLibraryManager.addJitPack();
		velocityLibraryManager.loadLibrary(yaml);
		velocityLibraryManager.loadLibrary(discord);
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

	private void loadFiles() {
		configTextFile = new TextFile(path, "config.yml");
		messagesTextFile = new TextFile(path, "messages.yml");
	}

	private void loadCommands() {

		getInstance().getServer().getCommandManager().register
				(server.getCommandManager().metaBuilder("ss").aliases("cleanss", "control")
						.build(), new ControlCommand(this));

		getInstance().getServer().getCommandManager().register
				(server.getCommandManager().metaBuilder("ssfinish").aliases("cleanssfinish", "controlfinish")
						.build(), new FinishCommand(this));

		getInstance().getServer().getCommandManager().register
				(server.getCommandManager().metaBuilder("ssinfo").aliases("cleanssinfo", "controlinfo")
						.build(), new InfoCommand(this));

		getInstance().getServer().getCommandManager().register
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
			UpdateJDA();
			getLogger().info("§7Hooked into Discord §dsuccessfully§7!");
		}
	}

	public boolean isLiteBans() {
		return server.getPluginManager().isLoaded("litebans");
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
				logger.warn("There is a new update available, download it on SpigotMC!");
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

			player.sendMessage(LegacyComponentSerializer.legacy('§')
					.deserialize("§e[CleanScreenShare] There is a new update available, download it on SpigotMC!"));

		});
	}

	@SneakyThrows
	public void UpdateJDA() {

		if (!VelocityConfig.DISCORD_ENABLED.get(Boolean.class)) {
			return;
		}

		if (jda.getJda() == null) {
			logger.error("Fatal error while updating JDA, please report this error on discord.io/futuredevelopment.");
			return;
		}

		jda.getJda().getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.of(net.dv8tion.jda.api.entities.Activity.ActivityType.valueOf
						(VelocityConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
				VelocityConfig.DISCORD_ACTIVITY.get(String.class)
						.replace("%players%", String.valueOf(server.getAllPlayers().size()))
						.replace("%suspiciouses%", String.valueOf(PlayerCache.getSuspicious().size()))));

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

	@SuppressWarnings("ALL")
	public boolean getUnsignedVelocityAddon() {
		return getServer().getPluginManager().isLoaded("unsignedvelocity");
	}

}