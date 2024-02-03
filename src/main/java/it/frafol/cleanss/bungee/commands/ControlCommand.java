package it.frafol.cleanss.bungee.commands;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.Utils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ControlCommand extends Command implements TabExecutor {

	public final CleanSS instance;

	public ControlCommand(CleanSS instance) {
		super(BungeeCommandsConfig.SS_PLAYER.getStringList().get(0),"", BungeeCommandsConfig.SS_PLAYER.getStringList().toArray(new String[0]));
		this.instance = instance;
	}

	@Override
	public void execute(CommandSender invocation, String[] args) {

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
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.USAGE.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())));
			return;
		}

		if (args.length != 1) {
			return;
		}

		if (!instance.getProxy().getPlayers().toString().contains(args[0])) {
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_ONLINE.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())
					.replace("%player%", args[0])));
			return;
		}

		final Optional<ProxiedPlayer> player = Optional.ofNullable(instance.getProxy().getPlayer(args[0]));
		final ProxiedPlayer sender = (ProxiedPlayer) invocation;

		if (!player.isPresent()) {
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NOT_ONLINE.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())
					.replace("%player%", args[0])
					.replace("&", "§")));
			return;
		}

		if (sender.getUniqueId().equals(player.get().getUniqueId())) {
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.YOURSELF.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())));
			return;
		}

		if (player.get().hasPermission(BungeeConfig.BYPASS_PERMISSION.get(String.class))) {
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.PLAYER_BYPASS.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())));
			return;
		}

		if (instance.getPremiumVanish() && BungeeConfig.PREMIUMVANISH.get(Boolean.class) && BungeeVanishAPI.getInvisiblePlayers().contains(player.get().getUniqueId())) {
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.PLAYER_BYPASS.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())));
			return;
		}

		List<ServerInfo> servers = Utils.getServerList(BungeeConfig.CONTROL.getStringList());

		if (!BungeeConfig.DISABLE_PING.get(Boolean.class)) {
			servers = Utils.getOnlineServers(servers);
		}

		if (servers.isEmpty()) {
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_EXIST.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())));
			return;
		}

		ServerInfo proxyServer = Utils.getBestServer(servers);

		if (PlayerCache.getSuspicious().contains(player.get().getUniqueId())) {
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_ALREADY.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())));
			return;
		}

		if (PlayerCache.getAdministrator().contains(sender.getUniqueId())) {
			invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_ALREADY.color()
					.replace("%prefix%", BungeeMessages.PREFIX.color())));
			return;
		}

		if (getMySQL()) {
			if (PlayerCache.getIn_control().get(player.get().getUniqueId()) != null && PlayerCache.getIn_control().get(player.get().getUniqueId()) == 1) {
				invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.CONTROL_ALREADY.color()
						.replace("%prefix%", BungeeMessages.PREFIX.color())));
				return;
			}
		}

		Utils.startControl(player.get(), sender, proxyServer);

		String suspect_group = "";
		String admin_group = "";

		if (luckperms) {
			admin_group = getDisplayName(sender);
			suspect_group = getDisplayName(player.get());
		}

		if (admin_group == null) {
			admin_group = "";
		}

		if (suspect_group == null) {
			suspect_group = "";
		}

		Utils.sendDiscordMessage(player.get(), (ProxiedPlayer) invocation, BungeeMessages.DISCORD_STARTED.get(String.class).replace("%suspectgroup%", suspect_group).replace("%admingroup%", admin_group));
	}

	private String getDisplayName(ProxiedPlayer player) {
		final LuckPerms api = LuckPermsProvider.get();

		final User user = api.getUserManager().getUser(player.getUniqueId());

		if (user == null) {
			return null;
		}

		final Group usergroup = api.getGroupManager().getGroup(user.getPrimaryGroup());

		if (usergroup != null) {
			if (usergroup.getFriendlyName().equalsIgnoreCase("default")) {
				return BungeeMessages.DISCORD_LUCKPERMS_FIX.get(String.class);
			}
			return usergroup.getFriendlyName();
		} else {
			return "";
		}
	}

	private boolean getMySQL() {
        return instance.getData() != null;
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String [] args) {

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
}