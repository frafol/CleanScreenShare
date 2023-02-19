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
import it.frafol.cleanss.velocity.objects.TextFile;
import lombok.Getter;
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
		version = "1.1.1",
		description = "Make control hacks on your players.",
		authors = { "frafol" })

public class CleanSS {

	public static final ChannelIdentifier channel_join = MinecraftChannelIdentifier.create("cleanss", "join");

	private final Logger logger;
	private final ProxyServer server;
	private final Path path;
	private final Metrics.Factory metricsFactory;

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

		if (VelocityConfig.STATS.get(Boolean.class)) {

			metricsFactory.make(this, 16951);
			logger.info("§7Metrics loaded §dsuccessfully§7!");

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
				.version("1.8.3")
				.build();

		velocityLibraryManager.addJitPack();
		velocityLibraryManager.loadLibrary(yaml);
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

	private void UpdateChecker() {

		if (!VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
			return;
		}

		if (!container.getDescription().getVersion().isPresent()) {
			return;
		}

		new UpdateCheck(this).getVersion(version -> {

			if (Double.parseDouble(container.getDescription().getVersion().get()) < Double.parseDouble(version)) {
				logger.warn("There is a new update available, download it on SpigotMC!");
			}

			if (Double.parseDouble(container.getDescription().getVersion().get()) > Double.parseDouble(version)) {
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

			if (!(Double.parseDouble(container.getDescription().getVersion().get()) < Double.parseDouble(version))) {
				return;
			}

			player.sendMessage(LegacyComponentSerializer.legacy('§')
					.deserialize("§e[CleanScreenShare] There is a new update available, download it on SpigotMC!"));

		});
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