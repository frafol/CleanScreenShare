package it.frafol.cleanss.velocity.objects;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityMessages;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class TitleUtil {

    private final CleanSS instance = CleanSS.getInstance();

    @Getter
    private ScheduledTask titleTask;

    @Getter
    private ScheduledTask titleTaskAdmin;

    void sendStartTitle(Player suspicious) {

        if (!VelocityMessages.CONTROL_USETITLE.get(Boolean.class)) {
            return;
        }

        Title controlTitle = Title.title(

                LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_TITLE.color()),
                LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROL_SUBTITLE.color()),

                Title.Times.times(
                        Duration.ofSeconds(VelocityMessages.CONTROL_FADEIN.get(Integer.class)),
                        Duration.ofSeconds(VelocityMessages.CONTROL_STAY.get(Integer.class)),
                        Duration.ofSeconds(VelocityMessages.CONTROL_FADEOUT.get(Integer.class))));

        titleTask = instance.getServer().getScheduler().buildTask(
                        instance, () -> suspicious.showTitle(controlTitle))
                .delay(VelocityMessages.CONTROL_DELAY.get(Integer.class), TimeUnit.SECONDS)
                .schedule();
    }

    void sendAdminStartTitle(Player administrator, Player suspicious) {

        if (!VelocityMessages.ADMINCONTROL_USETITLE.get(Boolean.class)) {
            return;
        }

        boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();
        String user_prefix = "";
        String user_suffix = "";

        if (luckperms) {
            user_suffix = Utils.getSuffix(suspicious);
            user_prefix = Utils.getPrefix(suspicious);
        }

        if (user_prefix == null) {
            user_prefix = "";
        }

        if (user_suffix == null) {
            user_suffix = "";
        }

        Title controlTitle = Title.title(

                LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ADMINCONTROL_TITLE.color()
                        .replace("%suspect%", suspicious.getUsername())
                        .replace("%suspectprefix%", user_prefix)
                        .replace("%suspectsuffix%", user_suffix)),

                LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ADMINCONTROL_SUBTITLE.color()
                        .replace("%suspect%", suspicious.getUsername())
                        .replace("%suspectprefix%", user_prefix)
                        .replace("%suspectsuffix%", user_suffix)),

                Title.Times.times(
                        Duration.ofSeconds(VelocityMessages.ADMINCONTROL_FADEIN.get(Integer.class)),
                        Duration.ofSeconds(VelocityMessages.ADMINCONTROL_STAY.get(Integer.class)),
                        Duration.ofSeconds(VelocityMessages.ADMINCONTROL_FADEOUT.get(Integer.class))));

        titleTaskAdmin = instance.getServer().getScheduler().buildTask(
                        instance, () -> administrator.showTitle(controlTitle))
                .delay(VelocityMessages.ADMINCONTROL_DELAY.get(Integer.class), TimeUnit.SECONDS)
                .schedule();
    }

    void sendEndTitle(Player suspicious) {

        if (!VelocityMessages.CONTROLFINISH_USETITLE.get(Boolean.class)) {
            return;
        }

        Title controlTitle = Title.title(

                LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROLFINISH_TITLE.color()),
                LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.CONTROLFINISH_SUBTITLE.color()),

                Title.Times.times(
                        Duration.ofSeconds(VelocityMessages.CONTROLFINISH_FADEIN.get(Integer.class)),
                        Duration.ofSeconds(VelocityMessages.CONTROLFINISH_STAY.get(Integer.class)),
                        Duration.ofSeconds(VelocityMessages.CONTROLFINISH_FADEOUT.get(Integer.class))));

        titleTask = instance.getServer().getScheduler().buildTask(
                        instance, () -> suspicious.showTitle(controlTitle))
                .delay(VelocityMessages.CONTROLFINISH_DELAY.get(Integer.class), TimeUnit.SECONDS)
                .schedule();
    }

    void sendAdminEndTitle(Player administrator, Player suspicious) {

        if (!VelocityMessages.ADMINCONTROLFINISH_USETITLE.get(Boolean.class)) {
            return;
        }

        boolean luckperms = instance.getServer().getPluginManager().getPlugin("luckperms").isPresent();
        String user_prefix = "";
        String user_suffix = "";

        if (luckperms) {
            user_suffix = Utils.getSuffix(suspicious);
            user_prefix = Utils.getPrefix(suspicious);
        }

        if (user_prefix == null) {
            user_prefix = "";
        }

        if (user_suffix == null) {
            user_suffix = "";
        }

        Title controlTitle = Title.title(

                LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ADMINCONTROLFINISH_TITLE.color()
                        .replace("%suspect%", suspicious.getUsername())
                        .replace("%suspectprefix%", user_prefix)
                        .replace("%suspectsuffix%", user_suffix)),

                LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ADMINCONTROLFINISH_SUBTITLE.color()
                        .replace("%suspect%", suspicious.getUsername())
                        .replace("%suspectprefix%", user_prefix)
                        .replace("%suspectsuffix%", user_suffix)),

                Title.Times.times(
                        Duration.ofSeconds(VelocityMessages.ADMINCONTROLFINISH_FADEIN.get(Integer.class)),
                        Duration.ofSeconds(VelocityMessages.ADMINCONTROLFINISH_STAY.get(Integer.class)),
                        Duration.ofSeconds(VelocityMessages.ADMINCONTROLFINISH_FADEOUT.get(Integer.class))));

        titleTaskAdmin = instance.getServer().getScheduler().buildTask(
                        instance, () -> administrator.showTitle(controlTitle))
                .delay(VelocityMessages.ADMINCONTROLFINISH_DELAY.get(Integer.class), TimeUnit.SECONDS)
                .schedule();
    }
}
