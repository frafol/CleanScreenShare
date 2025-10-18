package it.frafol.cleanss.velocity.objects;

import com.google.common.collect.Lists;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import lombok.SneakyThrows;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class TextFile {

    private final CleanSS instance = CleanSS.getInstance();
    private final YamlFile yamlFile;

    private static final List<TextFile> list = Lists.newArrayList();

    @SneakyThrows
    public TextFile(Path path, String fileName) {
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        Path configPath = path.resolve(fileName);

        if (!Files.exists(configPath)) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
                Files.copy(Objects.requireNonNull(in), configPath);
            }
        }

        yamlFile = new YamlFile(configPath.toFile());
        yamlFile.load();

        list.add(this);

    }

    public YamlFile getConfig() {return yamlFile;}

    @SneakyThrows
    public void reload() {

        boolean first = VelocityConfig.MYSQL.get(Boolean.class);

        yamlFile.load();

        if (VelocityConfig.MYSQL.get(Boolean.class)) {
            if (!first) {
                instance.setData();
                instance.ControlTask();
            }
        }

    }

    public static void reloadAll() {
        list.forEach(TextFile::reload);
    }
}
