package it.frafol.cleanss.velocity.objects.redisbungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import it.frafol.cleanss.velocity.objects.ChatUtil;
import it.frafol.cleanss.velocity.objects.Placeholder;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class MessageUtil {

    private final CleanSS instance = CleanSS.getInstance();

    public void sendDiscordMessage(UUID suspect, Player staffer, String message, String thumbnail, RedisBungeeAPI redisBungeeAPI) {

        if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class)) {

            if (instance.getJda() == null) {
                return;
            }

            if (instance.getJda().getJda() == null) {
                return;
            }

            final TextChannel channel = instance.getJda().getJda().getTextChannelById(VelocityConfig.DISCORD_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(VelocityConfig.DISCORD_EMBED_TITLE.get(String.class), null);

            embed.setDescription(message
                    .replace("%suspect%", redisBungeeAPI.getNameFromUuid(suspect))
                    .replace("%staffer%", staffer.getUsername()));

            embed.setColor(Color.RED);
            embed.setFooter(VelocityConfig.DISCORD_EMBED_FOOTER.get(String.class));

            if (!thumbnail.equals("none")) {
                embed.setThumbnail(thumbnail);
            }

            if (!VelocityConfig.DISCORD_EMBED_FOOTER_ICON.get(String.class).equals("none")) {
                embed.setFooter(VelocityConfig.DISCORD_EMBED_FOOTER.get(String.class), VelocityConfig.DISCORD_EMBED_FOOTER_ICON.get(String.class));
            }

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    public void sendDiscordMessage(UUID suspect, Player staffer, String message, String result, String thumbnail, RedisBungeeAPI redisBungeeAPI) {

        if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class)) {

            final TextChannel channel = instance.getJda().getJda().getTextChannelById(VelocityConfig.DISCORD_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(VelocityConfig.DISCORD_EMBED_TITLE.get(String.class), null);

            embed.setDescription(message
                    .replace("%suspect%", redisBungeeAPI.getNameFromUuid(suspect))
                    .replace("%staffer%", staffer.getUsername())
                    .replace("%result%", result));

            embed.setColor(Color.RED);
            embed.setFooter(VelocityConfig.DISCORD_EMBED_FOOTER.get(String.class));

            if (!thumbnail.equals("none")) {
                embed.setThumbnail(thumbnail);
            }

            if (!VelocityConfig.DISCORD_EMBED_FOOTER_ICON.get(String.class).equals("none")) {
                embed.setFooter(VelocityConfig.DISCORD_EMBED_FOOTER.get(String.class), VelocityConfig.DISCORD_EMBED_FOOTER_ICON.get(String.class));
            }

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendChannelMessage(Player player, String type) {
        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF(type);
        buf.writeUTF(player.getUsername());
        player.getCurrentServer().ifPresent(sv ->
                sv.sendPluginMessage(CleanSS.channel_join, buf.toByteArray()));
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendChannelMessage(UUID player, String type, RedisBungeeAPI redisBungeeAPI) {
        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF(type);
        buf.writeUTF(redisBungeeAPI.getNameFromUuid(player));
        Optional<RegisteredServer> server = instance.getServer().getServer(redisBungeeAPI.getServerNameFor(player));
        server.ifPresent(sv ->
                sv.sendPluginMessage(CleanSS.channel_join, buf.toByteArray()));
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendChannelAdvancedMessage(Player administrator, UUID suspicious, String type, RedisBungeeAPI redisBungeeAPI) {
        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF(type);
        buf.writeUTF(administrator.getUsername());
        buf.writeUTF(redisBungeeAPI.getNameFromUuid(suspicious));
        administrator.getCurrentServer().ifPresent(sv ->
                sv.sendPluginMessage(CleanSS.channel_join, buf.toByteArray()));
    }

    public void sendButtons(Player administrator, UUID suspicious, String admin_prefix, String admin_suffix, String sus_prefix, String sus_suffix, RedisBungeeAPI redisBungeeAPI) {
        instance.getServer().getScheduler().buildTask(instance, () -> {
            if (VelocityMessages.CONTROL_USEVERTICALFORMAT.get(Boolean.class)) {
                VelocityMessages.CONTROL_VERTICALFORMAT.sendButtons(administrator, redisBungeeAPI.getNameFromUuid(suspicious),
                        new Placeholder("cleanname", VelocityMessages.CONTROL_CLEAN_NAME.color()),
                        new Placeholder("hackername", VelocityMessages.CONTROL_CHEATER_NAME.color()),
                        new Placeholder("admitname", VelocityMessages.CONTROL_ADMIT_NAME.color()),
                        new Placeholder("refusename", VelocityMessages.CONTROL_REFUSE_NAME.color()),
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                        new Placeholder("suspect", redisBungeeAPI.getNameFromUuid(suspicious)),
                        new Placeholder("administrator", administrator.getUsername()),
                        new Placeholder("adminprefix", ChatUtil.color(admin_prefix)),
                        new Placeholder("adminsuffix", ChatUtil.color(admin_suffix)),
                        new Placeholder("suspectprefix", ChatUtil.color(sus_prefix)),
                        new Placeholder("suspectsuffix", ChatUtil.color(sus_suffix)));
            } else {
                VelocityMessages.CONTROL_HORIZONTALFORMAT.sendButtons(administrator, redisBungeeAPI.getNameFromUuid(suspicious),
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                        new Placeholder("suspect", redisBungeeAPI.getNameFromUuid(suspicious)),
                        new Placeholder("administrator", administrator.getUsername()),
                        new Placeholder("adminprefix", ChatUtil.color(admin_prefix)),
                        new Placeholder("adminsuffix", ChatUtil.color(admin_suffix)),
                        new Placeholder("suspectprefix", ChatUtil.color(sus_prefix)),
                        new Placeholder("suspectsuffix", ChatUtil.color(sus_suffix)));
            }
        }).delay(VelocityMessages.CONTROL_DELAYMESSAGE.get(Integer.class), TimeUnit.SECONDS).schedule();
    }
}
