package it.frafol.cleanss.bukkit.commands;

import it.frafol.cleanss.bukkit.enums.SpigotCache;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            player.teleport(PlayerCache.StringToLocation(SpigotCache.ADMIN_SPAWN.get(String.class)));
            return false;
        }

        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            player.teleport(PlayerCache.StringToLocation(SpigotCache.SUSPECT_SPAWN.get(String.class)));
            return false;
        }

        player.teleport(PlayerCache.StringToLocation(SpigotCache.OTHER_SPAWN.get(String.class)));
        return false;
    }
}
