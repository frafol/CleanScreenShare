package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ControlCommand extends Command implements TabExecutor {

	public final CleanSS instance;

	public ControlCommand(CleanSS instance) {
		super("ss","","screenshare","cleanss","cleanscreenshare", "control");
		this.instance = instance;
	}

	@Override
	public void execute(@NotNull CommandSender invocation, String[] args) {

		boolean luckperms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;

		if (!(invocation instanceof ProxiedPlayer)) {
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.ONLY_PLAYERS.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())));
			return;
		}

		if (!invocation.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class))) {
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())));
			return;
		}

		if (args.length == 0) {

			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.USAGE.color().replace("%prefix%", BungeeMessages.PREFIX.color())));
			return;

		}

		if (args.length == 1) {

			if (ProxyServer.getInstance().getPlayers().toString().contains(args[0])) {

				final Optional<ProxiedPlayer> player = Optional.ofNullable(ProxyServer.getInstance().getPlayer(args[0]));
				final ProxiedPlayer sender = (ProxiedPlayer) invocation;

				if (!player.isPresent()) {
					invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_ONLINE.color()
							.replace("%prefix%", BungeeMessages.PREFIX.color())
							.replace("%player%", args[0])
							.replace("&", "§")));
					return;
				}

				if (player.get().hasPermission(BungeeConfig.BYPASS_PERMISSION.get(String.class))) {
					invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.PLAYER_BYPASS.color()
							.replace("%prefix%", BungeeMessages.PREFIX.color())));
					return;
				}

				if (instance.getProxy().getServers().containsKey(BungeeConfig.CONTROL.get(String.class))) {

					final ServerInfo proxyServer = instance.getProxy().getServers().get(BungeeConfig.CONTROL.get(String.class));

					if (proxyServer == null) {
						return;
					}

					if (sender.getUniqueId() == player.get().getUniqueId()) {
						invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.YOURSELF.color()
								.replace("%prefix%", BungeeMessages.PREFIX.color())));
						return;
					}

					if (PlayerCache.getSuspicious().contains(player.get().getUniqueId())) {
						invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_ALREADY.color()
								.replace("%prefix%", BungeeMessages.PREFIX.color())));
						return;
					}

					if (PlayerCache.getIn_control().get(player.get().getUniqueId()) != null && PlayerCache.getIn_control().get(player.get().getUniqueId()) == 1) {
						invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_ALREADY.color()
								.replace("%prefix%", BungeeMessages.PREFIX.color())));
						return;
					}

					if (!BungeeConfig.DISABLE_PING.get(Boolean.class)) {
						instance.getProxy().getServers().get(proxyServer.getName()).ping((result, throwable) -> {

							if (throwable != null) {
								invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_EXIST.color()
										.replace("%prefix%", BungeeMessages.PREFIX.color())));
								return;
							}

							if (result == null) {
								invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_EXIST.color()
										.replace("%prefix%", BungeeMessages.PREFIX.color())));
								return;
							}

							if (sender.getServer() == null) {
								return;
							}

							if (player.get().getServer() == null) {
								return;
							}

							Utils.startControl(player.get(), sender, proxyServer);
							Utils.sendDiscordMessage(player.get(), sender, BungeeMessages.DISCORD_STARTED.get(String.class));

						});
					} else {
						if (sender.getServer() == null) {
							return;
						}

						if (player.get().getServer() == null) {
							return;
						}

						Utils.startControl(player.get(), sender, proxyServer);

						String suspect_group = "";
						String admin_group = "";

						if (luckperms) {

							final LuckPerms api = LuckPermsProvider.get();

							final User admin = api.getUserManager().getUser(((ProxiedPlayer) invocation).getUniqueId());
							final User suspect = api.getUserManager().getUser(player.get().getUniqueId());

							if (admin == null || suspect == null) {
								return;
							}

							final String admingroup = admin.getCachedData().getMetaData().getPrimaryGroup();
							admin_group = admingroup == null ? "" : admingroup;

							final String suspectgroup = suspect.getCachedData().getMetaData().getPrimaryGroup();
							suspect_group = suspectgroup == null ? "" : suspectgroup;

						}

						Utils.sendDiscordMessage(player.get(), (ProxiedPlayer) invocation, BungeeMessages.DISCORD_STARTED.get(String.class).replace("%suspectgroup%", suspect_group).replace("%admingroup%", admin_group));
					}

				} else {
					invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_EXIST.color()
							.replace("%prefix%", BungeeMessages.PREFIX.color())));
				}

			} else {
				invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_ONLINE.color()
						.replace("%prefix%", BungeeMessages.PREFIX.color())
						.replace("%player%", args[0])));
			}
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String @NotNull [] args) {
		Set<String> list = new HashSet<>();

		if (args.length == 1) {
			for (ProxiedPlayer players : instance.getProxy().getPlayers()) {
				list.add(players.getName());
			}
		}
		return list;
	}
}