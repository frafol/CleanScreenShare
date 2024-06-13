package it.frafol.cleanss.bukkit.commands;

import it.frafol.cleanss.bukkit.CleanSS;
import it.frafol.cleanss.bukkit.enums.SpigotConfig;
import it.frafol.cleanss.bukkit.objects.PlayerCache;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

public class AdminSpawnCommand implements CommandExecutor {

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        final Player player = (Player) sender;
        final YamlFile cache = CleanSS.getInstance().getCacheTextFile().getConfig();

        if (!player.hasPermission(SpigotConfig.ADMIN_PERMISSION.get(String.class))) {
            return false;
        }

        player.sendMessage(SpigotConfig.SPAWN_SET.color().replace("%type%", SpigotConfig.SPAWN_ADMIN.color()));
        cache.set("spawns.admin", PlayerCache.LocationToString(player.getLocation()));
        cache.save();
        return false;
    }
}
