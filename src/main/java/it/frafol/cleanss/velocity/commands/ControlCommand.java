package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ControlCommand implements SimpleCommand {

	private final CleanSS instance;

	public ControlCommand(CleanSS instance) {
		this.instance = instance;
	}

	@Override
	public void execute(@NotNull Invocation invocation) {

		final CommandSource source = invocation.source();

		if (Utils.isConsole(source)) {
			source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ONLY_PLAYERS.color()
					.replace("%prefix%", VelocityMessages.PREFIX.color())));
			return;
		}

		if (!source.hasPermission(VelocityConfig.CONTROL_PERMISSION.get(String.class))) {
			source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
					.replace("%prefix%", VelocityMessages.PREFIX.color())));
			return;
		}

		if (invocation.arguments().length == 0) {

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
						source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_ALREADY.color()
								.replace("%prefix%", VelocityMessages.PREFIX.color())));
						return;
					}

					if (PlayerCache.getIn_control().get(player.get().getUniqueId()) != null && PlayerCache.getIn_control().get(player.get().getUniqueId()) == 1) {
						source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_ALREADY.color()
								.replace("%prefix%", VelocityMessages.PREFIX.color())));
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

						Utils.startControl(player.get(), sender, proxyServer.get());
						Utils.sendDiscordMessage(player.get(), sender, VelocityMessages.DISCORD_STARTED.get(String.class));


					});

				} else {

					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_EXIST.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())));

				}

			} else {

				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())
						.replace("%player%", invocation.arguments()[0])));

			}
		}
	}
}