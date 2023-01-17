package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.TextFile;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends Command {

    public ReloadCommand() {
        super("ssreload","","screensharereload","cleanssreload","cleanscreensharereload");
    }

    @Override
    public void execute(@NotNull CommandSender sender, String[] args) {
        if (sender.hasPermission(BungeeConfig.RELOAD_PERMISSION.get(String.class))) {
            TextFile.reloadAll();
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.RELOADED.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
        }
    }
}
