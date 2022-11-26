package it.frafol.cleanss.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanss.velocity.commands.ControlCommand;
import it.frafol.cleanss.velocity.commands.FinishCommand;
import it.frafol.cleanss.velocity.commands.ReloadCommand;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.listeners.ChatListener;
import it.frafol.cleanss.velocity.listeners.CommandListener;
import it.frafol.cleanss.velocity.listeners.KickListener;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.TextFile;
import lombok.Getter;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import org.slf4j.Logger;

import java.nio.file.Path;

@Getter
@Plugin(
		id = "cleanscreenshare",
		name = "CleanScreenShare",
		version = "1.0-alpha",
		description = "Make control hacks on your players.",
		authors = { "frafol" })

public class CleanSS {

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

		VelocityLibraryManager<CleanSS> velocityLibraryManager = new VelocityLibraryManager<>(getLogger(), path, getServer().getPluginManager(), this);

		Library yaml = Library.builder()
				.groupId("me{}carleslc{}Simple-YAML")
				.artifactId("Simple-Yaml")
				.version("1.7.2")
				.build();

		velocityLibraryManager.addJitPack();
		velocityLibraryManager.loadLibrary(yaml);

		logger.info("\n§d   ___  __    ____    __    _  _   ___  ___\n" +
				"  / __)(  )  ( ___)  /__\\  ( \\( ) / __)/ __)\n" +
				" ( (__  )(__  )__)  /(__)\\  )  (  \\__ \\\\__ \\\n" +
				"  \\___)(____)(____)(__)(__)(_)\\_) (___/(___/\n");

		logger.info("§7Loading §dconfiguration§7...");
		configTextFile = new TextFile(path, "config.yml");
		messagesTextFile = new TextFile(path, "messages.yml");

		logger.info("§7Loading §dplugin§7...");

		getInstance().getServer().getCommandManager().register
				(server.getCommandManager().metaBuilder("ss").aliases("cleanss", "control")
						.build(), new ControlCommand(this));

		getInstance().getServer().getCommandManager().register
				(server.getCommandManager().metaBuilder("ssfinish").aliases("cleanssfinish", "controlfinish")
						.build(), new FinishCommand(this));

		getInstance().getServer().getCommandManager().register
				(server.getCommandManager().metaBuilder("ssreload").aliases("cleanssreload", "controlreload")
						.build(), new ReloadCommand(this));

		server.getEventManager().register(this, new CommandListener());

		if (VelocityMessages.CONTROL_CHAT.get(Boolean.class)) {
			server.getEventManager().register(this, new ChatListener());
		}

		server.getEventManager().register(this, new KickListener(this));

		if (VelocityConfig.STATS.get(Boolean.class)) {

			metricsFactory.make(this, 16951);

			logger.info("§7Metrics loaded §dsuccessfully§7!");

		}

		if (VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
			new UpdateCheck(this).getVersion(version -> {
				if (container.getDescription().getVersion().isPresent()) {
					if (!container.getDescription().getVersion().get().equals(version)) {
						logger.warn("There is a new update available, download it on SpigotMC!");
					}
				}
			});
		}

		logger.info("§7Plugin §dsuccessfully §7loaded!");
	}

	@Subscribe
	public void onProxyShutdown(ProxyShutdownEvent event) {

		logger.info("§7Clearing §dinstances§7...");
		instance = null;

		logger.info("§7Clearing §dlists§7...");
		PlayerCache.getSuspicious().clear();
		PlayerCache.getCouples().clear();

		logger.info("§7Plugin successfully §ddisabled§7!");
	}

}