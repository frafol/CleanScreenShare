package it.frafol.cleanss.bungee.commands;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import lombok.SneakyThrows;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.netty.PipelineUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

				if (instance.getProxy().getServersCopy().containsKey(BungeeConfig.CONTROL.get(String.class))) {

					final ServerInfo proxyServer = instance.getProxy().getServersCopy().get(BungeeConfig.CONTROL.get(String.class));

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
						ping((BungeeServerInfo) proxyServer, (result, throwable) -> {

							if (throwable != null || result == null) {
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

							String suspect_group = "";
							String admin_group = "";

							if (luckperms) {

								final LuckPerms api = LuckPermsProvider.get();

								final User admin = api.getUserManager().getUser(((ProxiedPlayer) invocation).getUniqueId());
								final User suspect = api.getUserManager().getUser(player.get().getUniqueId());

								if (admin == null || suspect == null) {
									return;
								}

								final Group admingroup = api.getGroupManager().getGroup(admin.getPrimaryGroup());

								String admingroup_displayname;
								if (admingroup != null) {
									admingroup_displayname = admingroup.getFriendlyName();

									if (admingroup_displayname.equalsIgnoreCase("default")) {
										admingroup_displayname = BungeeMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
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
										suspectroup_displayname = BungeeMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
									}

								} else {
									suspectroup_displayname = "";
								}

								suspect_group = suspectgroup == null ? "" : suspectroup_displayname;

							}

							Utils.sendDiscordMessage(player.get(), (ProxiedPlayer) invocation, BungeeMessages.DISCORD_STARTED.get(String.class).replace("%suspectgroup%", suspect_group).replace("%admingroup%", admin_group));

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

							final Group admingroup = api.getGroupManager().getGroup(admin.getPrimaryGroup());

							String admingroup_displayname;
							if (admingroup != null) {
								admingroup_displayname = admingroup.getFriendlyName();

								if (admingroup_displayname.equalsIgnoreCase("default")) {
									admingroup_displayname = BungeeMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
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
									suspectroup_displayname = BungeeMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
								}

							} else {
								suspectroup_displayname = "";
							}

							suspect_group = suspectgroup == null ? "" : suspectroup_displayname;

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

		if (args.length != 1) {
			return Collections.emptyList();
		}

		String partialName = args[0].toLowerCase();

		List<String> completions = new ArrayList<>();
		for (ProxiedPlayer player : instance.getProxy().getPlayers()) {
			if (player.getName().toLowerCase().startsWith(partialName)) {
				completions.add(player.getName());
			}
		}

		return completions;
	}

	private void ping(BungeeServerInfo target, Callback<Boolean> callback) {
		Bootstrap b = new Bootstrap().channel(getChannel(target.getAddress())).group(getEventLoopGroup()).handler(PipelineUtils.BASE).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000).remoteAddress(target.getAddress());
		b.connect().addListener(future -> callback.done(future.isSuccess(), future.cause()));
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Channel> getChannel(SocketAddress addr) {

		if (!oldPipelineUtils()) {
			return PipelineUtils.getChannel(addr);
		}

		try {
			getPipelineUtils().setAccessible(true);
			return (Class<? extends Channel>) getPipelineUtils().invoke(null);
		} catch (ClassCastException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	private EventLoopGroup getEventLoopGroup() {
		boolean useEpoll = Epoll.isAvailable();
		return useEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
	}

	@SuppressWarnings("ALL")
	@SneakyThrows
	private Method getPipelineUtils() {
		return PipelineUtils.class.getMethod("getChannel");
	}

	private boolean oldPipelineUtils() {
		try {
			PipelineUtils.class.getMethod("getChannel", SocketAddress.class);
		} catch (NoSuchMethodException | SecurityException e) {
			return true;
		}
		return false;
	}
}