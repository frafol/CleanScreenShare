package it.frafol.cleanss.velocity.objects;

import it.frafol.cleanss.velocity.enums.VelocityConfig;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class JdaBuilder {

    private JDA jda;

    public JDA JdaWorker() {
        return jda;
    }

    public JDA getJda() {
        return JdaWorker();
    }

    @SneakyThrows
    public void startJDA() {
        jda = JDABuilder.createDefault(VelocityConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
    }
}
