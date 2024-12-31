package it.frafol.cleanss.bungee.objects;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeMessages;
import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class TitleUtil {

    private final CleanSS instance = CleanSS.getInstance();
    private final boolean isLuckPerms = instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null;

    void sendStartTitle(ProxiedPlayer suspicious) {

        if (!BungeeMessages.CONTROL_USETITLE.get(Boolean.class)) return;
        final Title title = instance.getProxy().createTitle();
        title.fadeIn(BungeeMessages.CONTROL_FADEIN.get(Integer.class) * 20);
        title.stay(BungeeMessages.CONTROL_STAY.get(Integer.class) * 20);
        title.fadeOut(BungeeMessages.CONTROL_FADEOUT.get(Integer.class) * 20);
        title.title(new TextComponent(BungeeMessages.CONTROL_TITLE.color()));
        title.subTitle(new TextComponent(BungeeMessages.CONTROL_SUBTITLE.color()));

        instance.getProxy().getScheduler().schedule(instance, () ->
                title.send(suspicious), BungeeMessages.CONTROL_DELAY.get(Integer.class), TimeUnit.SECONDS);
    }

    void sendAdminStartTitle(ProxiedPlayer administrator, ProxiedPlayer suspicious) {

        if (!BungeeMessages.ADMINCONTROL_USETITLE.get(Boolean.class)) return;

        final Title title = instance.getProxy().createTitle();
        title.fadeIn(BungeeMessages.ADMINCONTROL_FADEIN.get(Integer.class) * 20);
        title.stay(BungeeMessages.ADMINCONTROL_STAY.get(Integer.class) * 20);
        title.fadeOut(BungeeMessages.ADMINCONTROL_FADEOUT.get(Integer.class) * 20);

        String user_prefix = "";
        String user_suffix = "";

        if (isLuckPerms) {
            final LuckPerms api = LuckPermsProvider.get();
            final User user = api.getUserManager().getUser(suspicious.getUniqueId());
            if (user == null) return;
            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            user_prefix = prefix == null ? "" : prefix;
            user_suffix = suffix == null ? "" : suffix;
        }

        title.title(new TextComponent(BungeeMessages.ADMINCONTROL_TITLE.color()
                .replace("%suspect%", suspicious.getName())
                .replace("%suspectprefix%", user_prefix)
                .replace("%suspectsuffix%", user_suffix)));

        title.subTitle(new TextComponent(BungeeMessages.ADMINCONTROL_SUBTITLE.color()
                .replace("%suspect%", suspicious.getName())
                .replace("%suspectprefix%", user_prefix)
                .replace("%suspectsuffix%", user_suffix)));

        instance.getProxy().getScheduler().schedule(instance, () ->
                title.send(administrator), BungeeMessages.ADMINCONTROL_DELAY.get(Integer.class), TimeUnit.SECONDS);
    }

    void sendEndTitle(ProxiedPlayer suspicious) {

        if (!BungeeMessages.CONTROLFINISH_USETITLE.get(Boolean.class)) return;

        final Title title = instance.getProxy().createTitle();
        title.fadeIn(BungeeMessages.CONTROLFINISH_FADEIN.get(Integer.class) * 20);
        title.stay(BungeeMessages.CONTROLFINISH_STAY.get(Integer.class) * 20);
        title.fadeOut(BungeeMessages.CONTROLFINISH_FADEOUT.get(Integer.class) * 20);
        title.title(new TextComponent(BungeeMessages.CONTROLFINISH_TITLE.color()));
        title.subTitle(new TextComponent(BungeeMessages.CONTROLFINISH_SUBTITLE.color()));

        instance.getProxy().getScheduler().schedule(instance, () ->
                title.send(suspicious), BungeeMessages.CONTROLFINISH_DELAY.get(Integer.class), TimeUnit.SECONDS);
    }

    void sendAdminEndTitle(ProxiedPlayer administrator, ProxiedPlayer suspicious) {

        if (!BungeeMessages.ADMINCONTROLFINISH_USETITLE.get(Boolean.class)) return;

        final Title title = instance.getProxy().createTitle();
        title.fadeIn(BungeeMessages.ADMINCONTROLFINISH_FADEIN.get(Integer.class) * 20);
        title.stay(BungeeMessages.ADMINCONTROLFINISH_STAY.get(Integer.class) * 20);
        title.fadeOut(BungeeMessages.ADMINCONTROLFINISH_FADEOUT.get(Integer.class) * 20);

        String user_prefix = "";
        String user_suffix = "";

        if (isLuckPerms) {
            final LuckPerms api = LuckPermsProvider.get();
            final User user = api.getUserManager().getUser(suspicious.getUniqueId());
            if (user == null) return;
            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            user_prefix = prefix == null ? "" : prefix;
            user_suffix = suffix == null ? "" : suffix;
        }

        title.title(new TextComponent(BungeeMessages.ADMINCONTROLFINISH_TITLE.color()
                .replace("%suspect%", suspicious.getName())
                .replace("%suspectprefix%", user_prefix)
                .replace("%suspectsuffix%", user_suffix)));

        title.subTitle(new TextComponent(BungeeMessages.ADMINCONTROLFINISH_SUBTITLE.color()
                .replace("%suspect%", suspicious.getName())
                .replace("%suspectprefix%", user_prefix)
                .replace("%suspectsuffix%", user_suffix)));

        instance.getProxy().getScheduler().schedule(instance, () ->
                title.send(administrator), BungeeMessages.ADMINCONTROLFINISH_DELAY.get(Integer.class), TimeUnit.SECONDS);
    }
}
