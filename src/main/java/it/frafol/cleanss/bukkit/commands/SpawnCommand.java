package it.frafol.cleanss.bukkit.commands;

import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotCache;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    private final CleanSS instance = CleanSS.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        if (strings.length != 1) {
            teleport(player);
            return false;
        }

        if (!PlayerCache.getAdministrator().contains(player.getUniqueId())) return false;
        String playerName = strings[0];
        Player selectedPlayer = instance.getServer().getPlayer(playerName);
        if (selectedPlayer == null) return false;
        if (PlayerCache.getCouples().get(player.getUniqueId()).equals(selectedPlayer.getUniqueId())) {
            teleport(selectedPlayer);
            player.sendMessage(SpigotConfig.SPAWN_SEND.color().replace("%suspect%", selectedPlayer.getName()));
        }
        return false;
    }

    private void teleport(Player player) {
        if (PlayerCache.getAdministrator().contains(player.getUniqueId())) {
            player.teleport(PlayerCache.StringToLocation(SpigotCache.ADMIN_SPAWN.get(String.class)));
            return;
        }
        if (PlayerCache.getSuspicious().contains(player.getUniqueId())) {
            player.teleport(PlayerCache.StringToLocation(SpigotCache.SUSPECT_SPAWN.get(String.class)));
            return;
        }
        player.teleport(PlayerCache.StringToLocation(SpigotCache.OTHER_SPAWN.get(String.class)));
    }
}
