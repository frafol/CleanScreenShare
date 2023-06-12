package it.frafol.cleanss.bungee.objects;

import com.google.common.collect.Lists;
import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import lombok.SneakyThrows;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class TextFile {

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

        boolean first = BungeeConfig.MYSQL.get(Boolean.class);

        yamlFile.load();

        if (BungeeConfig.MYSQL.get(Boolean.class)) {
            if (!first) {
                CleanSS.getInstance().setData();
                CleanSS.getInstance().ControlTask();
            }
        }

    }

    public static void reloadAll() {
        list.forEach(TextFile::reload);
    }
}
