package it.frafol.cleanss.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import it.frafol.cleanss.velocity.commands.ControlCommand;
import it.frafol.cleanss.velocity.commands.FinishCommand;
import it.frafol.cleanss.velocity.commands.ReloadCommand;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.listeners.ChatListener;
import it.frafol.cleanss.velocity.listeners.CommandListener;
import it.frafol.cleanss.velocity.listeners.KickListener;
import it.frafol.cleanss.velocity.listeners.ServerListener;
import it.frafol.cleanss.velocity.objects.JdaBuilder;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.TextFile;
import lombok.Getter;
import lombok.SneakyThrows;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;

@Getter
@Plugin(
		id = "cleanscreenshare",
		name = "CleanScreenShare",
		version = "1.2",
		description = "Make control hacks on your players.",
		authors = { "frafol" })

public class CleanSS {

	public static final ChannelIdentifier channel_join = MinecraftChannelIdentifier.create("cleanss", "join");

	private final Logger logger;
	private final ProxyServer server;
	private final Path path;
	private final Metrics.Factory metricsFactory;

	private final JdaBuilder jda = new JdaBuilder();

    private TextFile messagesTextFile;
	private TextFile configTextFile;

	private static CleanSS instance;

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

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {

		instance = this;

		loadLibraries();

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

		if (VelocityConfig.STATS.get(Boolean.class)) {

			metricsFactory.make(this, 16951);
			logger.info("§7Metrics loaded §dsuccessfully§7!");

		}

		if (!getUnsignedVelocityAddon()) {
			getLogger().warn("To get the full functionality of CleanScreenShare for versions 1.19.1 and later on Velocity, " +
					"consider downloading https://github.com/4drian3d/UnSignedVelocity/releases/latest");
		}

		UpdateChecker();
		logger.info("§7Plugin §dsuccessfully §7loaded!");

	}

	@Subscribe
	public void onProxyShutdown(ProxyShutdownEvent event) {

		logger.info("§7Clearing §dinstances§7...");
		instance = null;

		logger.info("§7Plugin successfully §ddisabled§7!");
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
				.version("5.0.0-beta.5")
				.url("https://github.com/DV8FromTheWorld/JDA/releases/download/v5.0.0-beta.5/JDA-5.0.0-beta.5-withDependencies-min.jar")
				.build();

		velocityLibraryManager.addMavenCentral();
		velocityLibraryManager.addJitPack();
		velocityLibraryManager.loadLibrary(yaml);
		velocityLibraryManager.loadLibrary(discord);
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