package it.frafol.cleanss.bungee.objects;

import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.CleanSS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLConnection {
    private Connection connection;
    private final ExecutorService executor;
    private final String host;
    private String database;
    private final String user;
    private final String password;

    private SQLConnection(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public SQLConnection(String host, String user, String password, String database) {
        this(host, user, password);
        this.database = database;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }

        return this.connection;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            executor.shutdownNow();
        }
    }

    private void connect() {
        try {
            CleanSS.getInstance().getLogger().info("§7Connecting to §dMySQL database§7...");
            connection = DriverManager.getConnection("JDBC:mysql://" + host + "/" + database + BungeeConfig.MYSQL_ARGUMENTS.get(String.class), user, password);
            CleanSS.getInstance().getLogger().info("§7Connected to §dMySQL database§7.");
        } catch (SQLException sqlException) {
            CleanSS.getInstance().getLogger().severe("§cUnable to connect to the database, cannot start the plugin. Is password correct?");
            sqlException.printStackTrace();
            CleanSS.getInstance().getProxy().stop();
        }
    }

    public void execute(String sql) {
        Runnable runnable = () -> {
            try {
                Statement statement = getConnection().createStatement();
                Throwable var3 = null;

                try {
                    statement.executeUpdate(sql);
                } catch (Throwable var13) {
                    var3 = var13;
                    throw var13;
                } finally {
                    if (statement != null) {
                        if (var3 != null) {
                            try {
                                statement.close();
                            } catch (Throwable var12) {
                                var3.addSuppressed(var12);
                            }
                        } else {
                            statement.close();
                        }
                    }

                }
            } catch (SQLException var15) {
                var15.printStackTrace();
            }

        };
        runnable.run();
    }
}