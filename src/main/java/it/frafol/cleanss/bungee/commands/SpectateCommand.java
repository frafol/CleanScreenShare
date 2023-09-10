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
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpectateCommand extends Command implements TabExecutor {

    private final CleanSS instance;

    public SpectateCommand(CleanSS instance) {
        super("ssspectate","","sspectate", "sspec", "ssspec", "cleanssspec", "controlspectate", "cleansspec", "cleanssspectate", "cleansspectate", "controlspec");
        this.instance = instance;
    }

    @Override
    public void execute(@NotNull CommandSender invocation, String[] args) {

        if (!invocation.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class))) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (!(invocation instanceof ProxiedPlayer)) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.ONLY_PLAYERS.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) invocation;
        boolean luckperms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;

        if (args.length != 1) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.USAGE.color().replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (instance.getProxy().getServerInfo(args[0]) == null) {
            player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.INVALID_SERVER.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                    .replace("%server%", args[0])));
            return;
        }

        final ServerInfo server = instance.getProxy().getServerInfo(args[0]);

        if (!Utils.isInControlServer(server)) {
            player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.INVALID_SERVER.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                    .replace("%server%", args[0])));
            return;
        }

        if (PlayerCache.getAdministrator().contains(player.getUniqueId()) || PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.IN_CONTROL_ERROR.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        player.sendMessage(TextComponent.fromLegacyText(BungeeMessages.SPECTATING.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())
                .replace("%server%", server.getName())));
        PlayerCache.getSpectators().add(player.getUniqueId());
        player.connect(server);

        Utils.sendDiscordSpectatorMessage(player, BungeeMessages.DISCORD_SPECTATOR.color()
                .replace("%server%", server.getName())
                .replace("%staffer%", player.getName()));

        String admin_prefix;
        String admin_suffix;

        if (luckperms) {

            final LuckPerms api = LuckPermsProvider.get();

            final User admin = api.getUserManager().getUser(player.getUniqueId());

            if (admin == null) {
                return;
            }

            final String prefix = admin.getCachedData().getMetaData().getPrefix();
            final String suffix = admin.getCachedData().getMetaData().getSuffix();

            admin_prefix = prefix == null ? "" : prefix;
            admin_suffix = suffix == null ? "" : suffix;

        } else {
            admin_prefix = "";
            admin_suffix = "";
        }

        if (BungeeConfig.SEND_ADMIN_MESSAGE.get(Boolean.class)) {
            instance.getProxy().getPlayers().stream()
                    .filter(players -> players.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class)))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.SPECT_ADMIN_NOTIFY.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("%admin%", player.getName())
                            .replace("%adminprefix%", Utils.color(admin_prefix))
                            .replace("%adminsuffix%", Utils.color(admin_suffix)))));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String @NotNull [] args) {

        if (args.length != 1) {
            return Collections.emptyList();
        }

        String partialName = args[0].toLowerCase();

        List<String> completions = new ArrayList<>();
        for (String servers : BungeeConfig.CONTROL.getStringList()) {
            if (servers.toLowerCase().startsWith(partialName)) {
                completions.add(servers);
            }
        }

        return completions;
    }
}
