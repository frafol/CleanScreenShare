package it.frafol.cleanss.velocity.objects.adapter;

import it.frafol.cleanss.velocity.CleanSS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public class MySQLFix {

    private static final String MYSQL_VERSION = "8.0.30";
    private static final String MYSQL_SHA256 = "b5bf2f0987197c30adf74a9e419b89cda4c257da2d1142871f508416d5f2227a";

    public final CleanSS instance;

    public MySQLFix(CleanSS instance) throws Exception {
        this.instance = instance;

        if (ReflectUtil.getClass("com.mysql.cj.jdbc.Driver") != null) {
            return;
        }

        if (!Files.exists(instance.getPath())) {
            Files.createDirectories(instance.getPath());
        }

        if (loadCache()) {
            return;
        }

        downloadAndLoad();
    }

    private String getMySQLLibraryName() {
        return "mysql-connector-java-" + MYSQL_VERSION + ".jar";
    }

    private File getMySQLLiraryFile() {
        return new File(instance.getPath().toFile(), getMySQLLibraryName());
    }

    private String getMySQLDownloadUrl() {
        return "https://maven.aliyun.com/repository/public/mysql/mysql-connector-java/"
                .concat(MYSQL_VERSION)
                .concat("/")
                .concat(getMySQLLibraryName());
    }

    public boolean loadCache() throws Exception {
        File libraryFile = getMySQLLiraryFile();
        if (libraryFile.exists()) {
            if (getSha256(libraryFile).equals(MYSQL_SHA256)) {
                instance.getLogger().info("[Installation] The cache file was verified successfully and is being implanted...");
                try {
                    ReflectUtil.addFileLibrary(libraryFile);
                } catch (Throwable ignored) {
                    instance.getLogger().error("[Installation failed] Can't download MySQL drivers, the server will stop.");
                    instance.getServer().shutdown();
                }
                instance.getLogger().info("[Installation] MySQL installation finished!");
                return true;
            }
            instance.getLogger().error("[Installation failed] Can't download MySQL drivers, a new retry will be done.");
        }
        return false;
    }

    public void downloadAndLoad() throws Exception {

        instance.getLogger().info("[Download] MySQL support for Velocity is being downloaded: " + getMySQLLibraryName());
        File libraryFile = getMySQLLiraryFile();
        Path path = new MySQLDownloadTask(getMySQLDownloadUrl(), libraryFile.toPath()).call();

        if (!getSha256(path.toFile()).equals(MYSQL_SHA256)) {
            throw new RuntimeException("Error failed, the 'SHA256' is not correct.");
        }

        instance.getLogger().info("[Download] The MySQL driver was downloaded successfully and is being started...");

        try {
            ReflectUtil.addFileLibrary(libraryFile);
        } catch (Throwable e) {
            instance.getLogger().error("Exception when trying to start MySQL", e);
        }

        instance.getLogger().info("[Download] MySQL has been downloaded.");
    }

    private String getSha256(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            byte[] buff = new byte[1024];
            int n;
            while ((n = fis.read(buff)) > 0) {
                baos.write(buff, 0, n);
            }
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(baos.toByteArray());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : digest) {
                String temp = Integer.toHexString((aByte & 0xFF));
                if (temp.length() == 1) {
                    sb.append("0");
                }
                sb.append(temp);
            }
            return sb.toString();
        }
    }

}
