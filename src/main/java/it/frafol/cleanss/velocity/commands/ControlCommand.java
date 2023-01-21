package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.Placeholder;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ControlCommand implements SimpleCommand {

	public final CleanSS instance;

	public ControlCommand(CleanSS instance) {
		this.instance = instance;
	}

	@Getter
	private ScheduledTask titleTask;

	@Override
	public void execute(@NotNull Invocation invocation) {

		final CommandSource source = invocation.source();

		if (!(source instanceof Player)) {
			source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ONLY_PLAYERS.color()
					.replace("%prefix%", VelocityMessages.PREFIX.color())));
			return;
		}

		if (invocation.arguments().length == 0) {

			if (!source.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class))) {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())));
				return;
			}

			source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.USAGE.color().replace("%prefix%", VelocityMessages.PREFIX.color())));
			return;

		}

		if (invocation.arguments().length == 1) {

			if (instance.getServer().getAllPlayers().toString().contains(invocation.arguments()[0])) {

				final Optional<Player> player = instance.getServer().getPlayer(invocation.arguments()[0]);
				final Player sender = (Player) source;

				if (!player.isPresent()) {
					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())
							.replace("%player%", invocation.arguments()[0])));
					return;
				}

				if (player.get().hasPermission(VelocityConfig.BYPASS_PERMISSION.get(String.class))) {
					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.PLAYER_BYPASS.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())));
					return;
				}

				if (instance.getServer().getAllServers().toString().contains(VelocityConfig.CONTROL.get(String.class))) {

					final Optional<RegisteredServer> proxyServer = instance.getServer().getServer(VelocityConfig.CONTROL.get(String.class));

					if (!proxyServer.isPresent()) {
						return;
					}

					if (sender.getUniqueId() == player.get().getUniqueId()) {
						source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.YOURSELF.color()
								.replace("%prefix%", VelocityMessages.PREFIX.color())));
						return;
					}

					if (PlayerCache.getSuspicious().contains(player.get().getUniqueId())) {
						instance.getLogger().error("Player already in control");
						return;
					}

					proxyServer.get().ping().whenComplete((result, throwable) -> {

						if (throwable != null) {
							source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_EXIST.color()
									.replace("%prefix%", VelocityMessages.PREFIX.color())));
							return;
						}

						if (result == null) {
							source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_EXIST.color()
									.replace("%prefix%", VelocityMessages.PREFIX.color())));
							return;
						}

						PlayerCache.getAdministrator().add(sender.getUniqueId());
						PlayerCache.getSuspicious().add(player.get().getUniqueId());
						PlayerCache.getCouples().put(sender, player.get());

						if (!sender.getCurrentServer().isPresent()) {
							return;
						}

						if (!player.get().getCurrentServer().isPresent()) {
							return;
						}

						if (sender.getCurrentServer().get().getServer() != proxyServer.get()) {
							sender.createConnectionRequest(proxyServer.get()).fireAndForget();
						}

						if (player.get().getCurrentServer().get().getServer() != proxyServer.get()) {
							player.get().createConnectionRequest(proxyServer.get()).fireAndForget();
						}

						if (VelocityMessages.CONTROL_USETITLE.get(Boolean.class)) {

							Title controlTitle = Title.title(

									LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_TITLE.color()),
									LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_SUBTITLE.color()),

									Title.Times.times(
											Duration.ofSeconds(VelocityMessages.CONTROL_FADEIN.get(Integer.class)),
											Duration.ofSeconds(VelocityMessages.CONTROL_STAY.get(Integer.class)),
											Duration.ofSeconds(VelocityMessages.CONTROL_FADEOUT.get(Integer.class))));

							titleTask = instance.getServer().getScheduler().buildTask(
											instance, () -> player.get().showTitle(controlTitle))
									.delay(VelocityMessages.CONTROL_DELAY.get(Integer.class), TimeUnit.SECONDS)
									.schedule();

						}

						player.get().sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.MAINSUS.color()
								.replace("%prefix%", VelocityMessages.PREFIX.color())));

						VelocityMessages.CONTROL_FORMAT.sendList(sender, player.get(),
								new Placeholder("cleanname", VelocityMessages.CONTROL_CLEAN_NAME.color()),
								new Placeholder("hackername", VelocityMessages.CONTROL_CHEATER_NAME.color()),
								new Placeholder("admitname", VelocityMessages.CONTROL_ADMIT_NAME.color()));

					});

				} else {
					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_EXIST.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())));
				}

			} else {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.get(String.class)
						.replace("%prefix%", VelocityMessages.PREFIX.color())
						.replace("%player%", invocation.arguments()[0])));
			}
		}
	}
}