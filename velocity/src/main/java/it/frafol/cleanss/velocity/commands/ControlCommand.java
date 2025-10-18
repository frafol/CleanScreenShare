package it.frafol.cleanss.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.*;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ControlCommand implements SimpleCommand {

	private final CleanSS instance;

	public ControlCommand(CleanSS instance) {
		this.instance = instance;
	}

	@Override
	public void execute(Invocation invocation) {

		final CommandSource source = invocation.source();
		boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();

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
			source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.PLAYER_MISSING.color()
					.replace("%prefix%", VelocityMessages.PREFIX.color())));
			return;
		}

		if (invocation.arguments().length > 1) {
			VelocityMessages.USAGE.sendList(source, null,
					new Placeholder("%prefix%", VelocityMessages.PREFIX.color()));
			return;
		}

		if (instance.getServer().getAllPlayers().toString().contains(invocation.arguments()[0])) {

			final Optional<Player> player = instance.getServer().getPlayer(invocation.arguments()[0]);
			final Player sender = (Player) source;

			if (!player.isPresent()) {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())
						.replace("%player%", invocation.arguments()[0])));
				return;
			}

			if (sender.getUniqueId().equals(player.get().getUniqueId())) {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.YOURSELF.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())));
				return;
			}

			for (String blocked_servers : VelocityConfig.CONTROL_BYPASS.getStringList()) {
				if (instance.getServer().getServer(blocked_servers).isPresent() && player.get().getCurrentServer().isPresent()) {
					ServerInfo blockedServer = instance.getServer().getServer(blocked_servers).get().getServerInfo();
					if (player.get().getCurrentServer().get().getServerInfo().equals(blockedServer)) {
						source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.PLAYER_BYPASS_SERVER.color()
								.replace("%prefix%", VelocityMessages.PREFIX.color())
								.replace("%server%", blockedServer.getName())
								.replace("%player%", player.get().getUsername())));
						return;
					}
				}
			}

			if (player.get().hasPermission(VelocityConfig.BYPASS_PERMISSION.get(String.class))) {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.PLAYER_BYPASS.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())));
				return;
			}

			if (instance.getVelocityVanish() && VelocityConfig.VELOCITYVANISH.get(Boolean.class) && VelocityVanishUtils.isVanished(player.get())) {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.PLAYER_BYPASS.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())));
				return;
			}

			if (instance.getPremiumVanish() && VelocityConfig.PREMIUMVANISH.get(Boolean.class) && PremiumVanishUtils.isVanished(player.get())) {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.PLAYER_BYPASS.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())));
				return;
			}

			if (instance.useLimbo) {

				if (PlayerCache.getSuspicious().contains(player.get().getUniqueId())) {
					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_ALREADY.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())));
					return;
				}

				if (PlayerCache.getAdministrator().contains(sender.getUniqueId())) {
					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_ALREADY.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())));
					return;
				}

				if (PlayerCache.getIn_control().get(player.get().getUniqueId()) != null && PlayerCache.getIn_control().get(player.get().getUniqueId()) == 1) {
					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_ALREADY.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())));
					return;
				}

				String admin_group;
				String suspect_group;

				if (luckperms) {

					final LuckPerms api = LuckPermsProvider.get();

					final User admin = api.getUserManager().getUser(sender.getUniqueId());
					final User suspect = api.getUserManager().getUser(player.get().getUniqueId());

					if (admin == null || suspect == null) {
						return;
					}

					final Group admingroup = api.getGroupManager().getGroup(admin.getPrimaryGroup());

					String admingroup_displayname;
					if (admingroup != null) {
						admingroup_displayname = admingroup.getFriendlyName();

						if (admingroup_displayname.equalsIgnoreCase("default")) {
							admingroup_displayname = VelocityMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
						}

					} else {
						admingroup_displayname = "";
					}

					admin_group = admingroup == null ? "" : admingroup_displayname;

					final Group suspectgroup = api.getGroupManager().getGroup(suspect.getPrimaryGroup());

					String suspectroup_displayname;
					if (suspectgroup != null) {
						suspectroup_displayname = suspectgroup.getFriendlyName();

						if (suspectroup_displayname.equalsIgnoreCase("default")) {
							suspectroup_displayname = VelocityMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
						}

					} else {
						suspectroup_displayname = "";
					}

					suspect_group = suspectgroup == null ? "" : suspectroup_displayname;

				} else {
					admin_group = "";
					suspect_group = "";
				}


				LimboUtils.spawnPlayerLimbo(sender);
				LimboUtils.spawnPlayerLimbo(player.get());

				instance.getServer().getScheduler().buildTask(instance, () -> {
					Utils.startControl(player.get(), sender, null);
					MessageUtil.sendDiscordMessage(
							player.get(),
							sender,
							VelocityMessages.DISCORD_STARTED.get(String.class)
									.replace("%suspectgroup%", suspect_group)
									.replace("%admingroup%", admin_group),
							VelocityMessages.DISCORD_STARTED_THUMBNAIL.get(String.class));
				}).delay(Duration.ofSeconds(1)).schedule();
				return;
			}

			List<Optional<RegisteredServer>> servers = Utils.getServerList(VelocityConfig.CONTROL.getStringList());

			if (!VelocityConfig.DISABLE_PING.get(Boolean.class)) {
				servers = Utils.getOnlineServers(servers);
			}

			Optional<RegisteredServer> proxyServer = Utils.getBestServer(servers);

			if (!proxyServer.isPresent()) {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_EXIST.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())));
				return;
			}

			if (PlayerCache.getSuspicious().contains(player.get().getUniqueId())) {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_ALREADY.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())));
				return;
			}

			if (PlayerCache.getAdministrator().contains(sender.getUniqueId())) {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_ALREADY.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())));
				return;
			}

			if (PlayerCache.getIn_control().get(player.get().getUniqueId()) != null && PlayerCache.getIn_control().get(player.get().getUniqueId()) == 1) {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_ALREADY.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())));
				return;
			}

			String admin_group;
			String suspect_group;

			if (luckperms) {

				final LuckPerms api = LuckPermsProvider.get();

				final User admin = api.getUserManager().getUser(sender.getUniqueId());
				final User suspect = api.getUserManager().getUser(player.get().getUniqueId());

				if (admin == null || suspect == null) {
					return;
				}

				final Group admingroup = api.getGroupManager().getGroup(admin.getPrimaryGroup());

				String admingroup_displayname;
				if (admingroup != null) {
					admingroup_displayname = admingroup.getFriendlyName();

					if (admingroup_displayname.equalsIgnoreCase("default")) {
						admingroup_displayname = VelocityMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
					}

				} else {
					admingroup_displayname = "";
				}

				admin_group = admingroup == null ? "" : admingroup_displayname;

				final Group suspectgroup = api.getGroupManager().getGroup(suspect.getPrimaryGroup());

				String suspectroup_displayname;
				if (suspectgroup != null) {
					suspectroup_displayname = suspectgroup.getFriendlyName();

					if (suspectroup_displayname.equalsIgnoreCase("default")) {
						suspectroup_displayname = VelocityMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
					}

				} else {
					suspectroup_displayname = "";
				}

				suspect_group = suspectgroup == null ? "" : suspectroup_displayname;

			} else {
				admin_group = "";
				suspect_group = "";
			}

			Utils.startControl(player.get(), sender, proxyServer.get());
			MessageUtil.sendDiscordMessage(
					player.get(),
					sender,
					VelocityMessages.DISCORD_STARTED.get(String.class)
							.replace("%suspectgroup%", suspect_group)
							.replace("%admingroup%", admin_group),
					VelocityMessages.DISCORD_STARTED_THUMBNAIL.get(String.class));

		} else {
			source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
					.replace("%prefix%", VelocityMessages.PREFIX.color())
					.replace("%player%", invocation.arguments()[0])));
		}
	}

	@Override
	public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
		final String[] strings = invocation.arguments();

		if (Utils.isConsole(invocation.source())) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		final List<String> players = instance.getServer().getAllPlayers().stream()
				.map(Player::getUsername)
				.filter(player -> strings.length != 1 || strings[0].isEmpty()
						|| player.toLowerCase().startsWith(strings[0].toLowerCase()))
				.collect(Collectors.toList());

		return CompletableFuture.completedFuture(players);
	}
}