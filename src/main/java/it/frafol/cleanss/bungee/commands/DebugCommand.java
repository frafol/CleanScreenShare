package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.objects.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class DebugCommand extends Command {

    public final CleanSS instance;

    public DebugCommand(CleanSS instance) {
        super("ssdebug","","screensharedebug","cleanssdebug","cleanscreensharedebug", "controldebug");
        this.instance = instance;
    }

    @Override
    public void execute(CommandSender invocation, String[] args) {

        if (args.length != 0) {
            return;
        }

        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7CleanScreenShare Informations"));
        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7Version: §d" + instance.getDescription().getVersion()));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7BungeeCord: §d" + instance.getProxy().getVersion()));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7MySQL: " + getMySQL()));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7Discord: " + getDiscord()));
        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7Control servers: "));

        Utils.getServerList(BungeeConfig.CONTROL.getStringList()).forEach(server -> {

            if (Utils.getOnlineServers(Utils.getServerList(BungeeConfig.CONTROL.getStringList())).contains(server)) {
                invocation.sendMessage(TextComponent.fromLegacy("§d| §7- §a" + server.getName()));
                return;
            }

            invocation.sendMessage(TextComponent.fromLegacy("§d| §7- §c" + server.getName()));
        });

        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7Fallback servers: "));

        Utils.getServerList(BungeeConfig.CONTROL_FALLBACK.getStringList()).forEach(server -> {

            if (Utils.getOnlineServers(Utils.getServerList(BungeeConfig.CONTROL_FALLBACK.getStringList())).contains(server)) {
                invocation.sendMessage(TextComponent.fromLegacy("§d| §7- §a" + server.getName()));
                return;
            }

            invocation.sendMessage(TextComponent.fromLegacy("§d| §7- §c" + server.getName()));
        });

        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
    }

    private String getMySQL() {
        if (instance.getData() == null) {
            return "§cNot connected";
        }
        return "§aConnected";
    }

    private String getDiscord() {
        if (instance.getJda() == null) {
            return "§cNot connected";
        }
        return "§aConnected";
    }
}
