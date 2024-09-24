package it.frafol.cleanss.velocity.objects;

import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class JdaBuilder {

    private JDA jda;
    private final CleanSS instance = CleanSS.getInstance();

    public JDA JdaWorker() {
        return jda;
    }

    public JDA getJda() {
        return JdaWorker();
    }

    @SneakyThrows
    public void startJDA() {
        try {
            jda = JDABuilder.createDefault(VelocityConfig.DISCORD_TOKEN.get(String.class))
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .setStatus(selectStatus())
                    .build();
        } catch (ExceptionInInitializerError e) {
            System.out.println("[CleanScreenShare] Invalid Discord configuration, please check your config.yml file.");
        }
    }

    private OnlineStatus selectStatus() {
        String status = VelocityConfig.DISCORD_STATUS.get(String.class);
        if (status.equalsIgnoreCase("ONLINE")) {
            return OnlineStatus.ONLINE;
        } else if (status.equalsIgnoreCase("IDLE")) {
            return OnlineStatus.IDLE;
        } else if (status.equalsIgnoreCase("DND")) {
            return OnlineStatus.DO_NOT_DISTURB;
        } else if (status.equalsIgnoreCase("INVISIBLE")) {
            return OnlineStatus.INVISIBLE;
        } else {
            return OnlineStatus.ONLINE;
        }
    }
}
