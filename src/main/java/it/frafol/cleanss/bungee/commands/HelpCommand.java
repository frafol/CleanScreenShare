package it.frafol.cleanss.bungee.commands;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import it.frafol.cleanss.bungee.objects.Placeholder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class HelpCommand extends Command {

    public final CleanSS instance;

    public HelpCommand(CleanSS instance) {
        super(BungeeCommandsConfig.HELP.getStringList().get(0),"", BungeeCommandsConfig.HELP.getStringList().toArray(new String[0]));
        this.instance = instance;
    }

    @Override
    public void execute(CommandSender invocation, String[] args) {

        if (invocation.hasPermission(BungeeConfig.CONTROL_PERMISSION.get(String.class))) {
            invocation.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        BungeeMessages.USAGE.sendList(invocation, null,
                new Placeholder("%prefix%", BungeeMessages.PREFIX.color()));
    }
}
