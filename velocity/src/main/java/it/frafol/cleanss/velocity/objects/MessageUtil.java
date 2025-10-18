package it.frafol.cleanss.velocity.objects;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class MessageUtil {

    private final CleanSS instance = CleanSS.getInstance();

    public void sendDiscordSpectatorMessage(Player player, String message, String thumbnail) {

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
                    .replace("%staffer%", player.getUsername()));

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

    public void sendDiscordMessage(Player suspect, Player staffer, String message, String thumbnail) {

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
                    .replace("%suspect%", suspect.getUsername())
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

    public void sendDiscordMessage(Player suspect, Player staffer, String message, String result, String thumbnail) {

        if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class)) {

            final TextChannel channel = instance.getJda().getJda().getTextChannelById(VelocityConfig.DISCORD_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(VelocityConfig.DISCORD_EMBED_TITLE.get(String.class), null);

            embed.setDescription(message
                    .replace("%suspect%", suspect.getUsername())
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
        try {
            final ByteArrayDataOutput buf = ByteStreams.newDataOutput();
            buf.writeUTF(type);
            buf.writeUTF(player.getUsername());
            player.getCurrentServer().ifPresent(sv ->
                    sv.sendPluginMessage(CleanSS.channel_join, buf.toByteArray()));
        } catch (IllegalStateException exception) {
            instance.getLogger().error("Failed to send message for player " + player.getUsername() + ". Connection failed?");
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendChannelAdvancedMessage(Player administrator, Player suspicious, String type) {
        try {
            final ByteArrayDataOutput buf = ByteStreams.newDataOutput();
            buf.writeUTF(type);
            buf.writeUTF(administrator.getUsername());
            buf.writeUTF(suspicious.getUsername());
            administrator.getCurrentServer().ifPresent(sv ->
                    sv.sendPluginMessage(CleanSS.channel_join, buf.toByteArray()));
        } catch (IllegalStateException exception) {
            instance.getLogger().error("Failed to send message for players " + administrator.getUsername() + " and " + suspicious.getUsername() + ". Connection failed?");
        }
    }

    public void sendButtons(Player administrator, Player suspicious, String admin_prefix, String admin_suffix, String sus_prefix, String sus_suffix) {
        instance.getServer().getScheduler().buildTask(instance, () -> {
            if (VelocityMessages.CONTROL_USEVERTICALFORMAT.get(Boolean.class)) {
                VelocityMessages.CONTROL_VERTICALFORMAT.sendButtons(administrator, suspicious,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                        new Placeholder("suspect", suspicious.getUsername()),
                        new Placeholder("administrator", administrator.getUsername()),
                        new Placeholder("adminprefix", ChatUtil.color(admin_prefix)),
                        new Placeholder("adminsuffix", ChatUtil.color(admin_suffix)),
                        new Placeholder("suspectprefix", ChatUtil.color(sus_prefix)),
                        new Placeholder("suspectsuffix", ChatUtil.color(sus_suffix)));
            } else {
                VelocityMessages.CONTROL_HORIZONTALFORMAT.sendButtons(administrator, suspicious,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                        new Placeholder("suspect", suspicious.getUsername()),
                        new Placeholder("administrator", administrator.getUsername()),
                        new Placeholder("adminprefix", ChatUtil.color(admin_prefix)),
                        new Placeholder("adminsuffix", ChatUtil.color(admin_suffix)),
                        new Placeholder("suspectprefix", ChatUtil.color(sus_prefix)),
                        new Placeholder("suspectsuffix", ChatUtil.color(sus_suffix)));
            }
        }).delay(VelocityMessages.CONTROL_DELAYMESSAGE.get(Integer.class), TimeUnit.SECONDS).schedule();
    }
}
