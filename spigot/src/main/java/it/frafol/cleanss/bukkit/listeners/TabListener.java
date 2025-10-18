package it.frafol.cleanss.bukkit.listeners;

import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class TabListener implements TabCompleter {

    private final CleanSS instance = CleanSS.getInstance();

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (PlayerCache.getCouples().get(((Player) commandSender).getUniqueId()) == null) return null;
        if (instance.getServer().getPlayer(PlayerCache.getCouples().get(((Player) commandSender).getUniqueId())) == null) return null;
        if (strings.length != 1) return null;
        return Collections.singletonList(instance.getServer().getPlayer(PlayerCache.getCouples().get(((Player) commandSender).getUniqueId())).getName());
    }
}
