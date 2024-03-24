package it.frafol.cleanss.bungee.objects;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.awt.*;

@UtilityClass
public class MessageUtil {

    private final CleanSS instance = CleanSS.getInstance();

    public void sendDiscordSpectatorMessage(ProxiedPlayer player, String message, String thumbnail) {

        if (BungeeConfig.DISCORD_ENABLED.get(Boolean.class)) {

            if (instance.getJda() == null) {
                return;
            }

            final TextChannel channel = instance.getJda().getTextChannelById(BungeeConfig.DISCORD_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(BungeeConfig.DISCORD_EMBED_TITLE.get(String.class), null);

            embed.setDescription(message
                    .replace("%staffer%", player.getName()));

            embed.setColor(Color.RED);
            embed.setFooter(BungeeConfig.DISCORD_EMBED_FOOTER.get(String.class));

            if (!thumbnail.equals("none")) {
                embed.setThumbnail(thumbnail);
            }

            if (!BungeeConfig.DISCORD_EMBED_FOOTER_ICON.get(String.class).equals("none")) {
                embed.setFooter(BungeeConfig.DISCORD_EMBED_FOOTER.get(String.class), BungeeConfig.DISCORD_EMBED_FOOTER_ICON.get(String.class));
            }

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    public void sendDiscordMessage(ProxiedPlayer suspect, ProxiedPlayer staffer, String message, String result, String thumbnail) {

        if (BungeeConfig.DISCORD_ENABLED.get(Boolean.class)) {

            final TextChannel channel = instance.getJda().getTextChannelById(BungeeConfig.DISCORD_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(BungeeConfig.DISCORD_EMBED_TITLE.get(String.class), null);

            embed.setDescription(message
                    .replace("%suspect%", suspect.getName())
                    .replace("%staffer%", staffer.getName())
                    .replace("%result%", result));

            embed.setColor(Color.RED);
            embed.setFooter(BungeeConfig.DISCORD_EMBED_FOOTER.get(String.class));

            if (!thumbnail.equals("none")) {
                embed.setThumbnail(thumbnail);
            }

            if (!BungeeConfig.DISCORD_EMBED_FOOTER_ICON.get(String.class).equals("none")) {
                embed.setFooter(BungeeConfig.DISCORD_EMBED_FOOTER.get(String.class), BungeeConfig.DISCORD_EMBED_FOOTER_ICON.get(String.class));
            }

            channel.sendMessageEmbeds(embed.build()).queue();

        }
    }

    public void sendDiscordMessage(ProxiedPlayer suspect, ProxiedPlayer staffer, String message, String thumbnail) {

        if (BungeeConfig.DISCORD_ENABLED.get(Boolean.class)) {

            if (instance.getJda() == null) {
                return;
            }

            final TextChannel channel = instance.getJda().getTextChannelById(BungeeConfig.DISCORD_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(BungeeConfig.DISCORD_EMBED_TITLE.get(String.class), null);

            embed.setDescription(message
                    .replace("%suspect%", suspect.getName())
                    .replace("%staffer%", staffer.getName()));

            embed.setColor(Color.RED);
            embed.setFooter(BungeeConfig.DISCORD_EMBED_FOOTER.get(String.class));

            if (!thumbnail.equals("none")) {
                embed.setThumbnail(thumbnail);
            }

            if (!BungeeConfig.DISCORD_EMBED_FOOTER_ICON.get(String.class).equals("none")) {
                embed.setFooter(BungeeConfig.DISCORD_EMBED_FOOTER.get(String.class), BungeeConfig.DISCORD_EMBED_FOOTER_ICON.get(String.class));
            }

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendChannelMessage(ProxiedPlayer player, String type) {

        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();

        buf.writeUTF(type);
        buf.writeUTF(player.getName());

        if (player.getServer() == null) {
            instance.getLogger().severe("The player " + player.getName() + " has no server, please check your control server if it's working correctly!");
            return;
        }

        player.getServer().sendData("cleanss:join", buf.toByteArray());

    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendChannelAdvancedMessage(ProxiedPlayer administrator, ProxiedPlayer suspicious, String type) {

        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();

        buf.writeUTF(type);
        buf.writeUTF(administrator.getName());
        buf.writeUTF(suspicious.getName());

        if (administrator.getServer() == null) {
            instance.getLogger().severe("The player " + administrator.getName() + " has no server, please check your control server if it's working correctly!");
            return;
        }

        administrator.getServer().sendData("cleanss:join", buf.toByteArray());
    }
}
