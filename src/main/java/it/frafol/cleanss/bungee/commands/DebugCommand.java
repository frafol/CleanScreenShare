package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

public class DebugCommand extends Command {

    public final CleanSS instance;

    public DebugCommand(CleanSS instance) {
        super("ssdebug","","screensharedebug","cleanssdebug","cleanscreensharedebug", "controldebug");
        this.instance = instance;
    }

    @Override
    public void execute(@NotNull CommandSender invocation, String[] args) {

        if (args.length != 0) {
            return;
        }

        invocation.sendMessage(TextComponent.fromLegacyText("§d| "));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| §7CleanScreenShare Informations"));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| "));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| §7Version: §d" + instance.getDescription().getVersion()));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| §7BungeeCord: §d" + instance.getProxy().getVersion()));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| §7MySQL: §d" + getMySQL()));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| "));

    }

    private String getMySQL() {
        if (instance.getData() == null) {
            return "Not connected";
        } else {
            return "Connected";
        }
    }
}
