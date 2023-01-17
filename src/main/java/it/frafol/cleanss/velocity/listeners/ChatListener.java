package it.frafol.cleanss.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ChatListener {

    public final CleanSS instance;

    public ChatListener(CleanSS instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onChat(@NotNull PlayerChatEvent event) {

        final Player player = event.getPlayer();

        if (!player.getCurrentServer().isPresent()) {
            return;
        }

        if (player.getCurrentServer().get().getServerInfo().getName().equals(VelocityConfig.CONTROL.get(String.class))) {

            if (!(player.getProtocolVersion() == ProtocolVersion.MINECRAFT_1_19
                    || player.getProtocolVersion() == ProtocolVersion.MINECRAFT_1_19_1
                    || player.getProtocolVersion() == ProtocolVersion.MINECRAFT_1_19_3)) {

                event.setResult(PlayerChatEvent.ChatResult.denied());

            } else {

                instance.getLogger().warn("Unable to delete message for " + player.getUsername() + ". " +
                        "This is a Velocity issue affecting Minecraft 1.19+ clients. " +
                        "To fix this, make sure you have also installed the plugin on your Spigot server and enabled chat prevention.");

            }

            if (PlayerCache.getCouples().containsKey(player)) {

                instance.getValue(PlayerCache.getCouples(), player).sendMessage(Component.text(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", player.getUsername())
                        .replace("%message%", event.getMessage())
                        .replace("%state%", VelocityMessages.CONTROL_CHAT_STAFF.color())));

                player.sendMessage(Component.text(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", player.getUsername())
                        .replace("%message%", event.getMessage())
                        .replace("%state%", VelocityMessages.CONTROL_CHAT_STAFF.color())));

                return;

            }

            if (PlayerCache.getCouples().containsValue(player)) {

                instance.getKey(PlayerCache.getCouples(), player).sendMessage(Component.text(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", player.getUsername())
                        .replace("%message%", event.getMessage())
                        .replace("%state%", VelocityMessages.CONTROL_CHAT_SUS.color())));

                player.sendMessage(Component.text(VelocityMessages.CONTROL_CHAT_FORMAT.color()
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("%player%", player.getUsername())
                        .replace("%message%", event.getMessage())
                        .replace("%state%", VelocityMessages.CONTROL_CHAT_SUS.color())));


            }
        }
    }
}
