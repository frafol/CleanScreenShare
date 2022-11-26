package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ChatListener {

    @Subscribe
    public void onChat(@NotNull PlayerChatEvent event) {

        final Player player = event.getPlayer();

        if (!player.getCurrentServer().isPresent()) {
            return;
        }

        if (player.getCurrentServer().get().getServerInfo().getName().equals(VelocityConfig.CONTROL.get(String.class))) {

            if (!(player.getProtocolVersion() == ProtocolVersion.MINECRAFT_1_19
                    || player.getProtocolVersion() == ProtocolVersion.MINECRAFT_1_19_1)) {

                event.setResult(PlayerChatEvent.ChatResult.denied());

            }

            for (Map.Entry<Player, Player> entry : PlayerCache.getCouples().entrySet()) {
                if (PlayerCache.getCouples().containsKey(player)) {

                    entry.getKey().sendMessage(Component.text(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("%player%", player.getUsername())
                            .replace("%message%", event.getMessage())
                            .replace("%state%", VelocityMessages.CONTROL_CHAT_STAFF.color())));

                    entry.getValue().sendMessage(Component.text(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("%player%", player.getUsername())
                            .replace("%message%", event.getMessage())
                            .replace("%state%", VelocityMessages.CONTROL_CHAT_STAFF.color())));

                    return;

                }

                if (PlayerCache.getCouples().containsValue(player)) {

                    entry.getKey().sendMessage(Component.text(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("%player%", player.getUsername())
                            .replace("%message%", event.getMessage())
                            .replace("%state%", VelocityMessages.CONTROL_CHAT_SUS.color())));

                    entry.getValue().sendMessage(Component.text(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("%player%", player.getUsername())
                            .replace("%message%", event.getMessage())
                            .replace("%state%", VelocityMessages.CONTROL_CHAT_SUS.color())));

                    return;

                }
            }
        }
    }
}
