package it.frafol.cleanss.velocity.objects;

import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLConnection {

    private final CleanSS instance = CleanSS.getInstance();

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
            instance.getLogger().warn("Connecting to MySQL database...");
            connection = DriverManager.getConnection("JDBC:mysql://" + host + "/" + database + VelocityConfig.MYSQL_ARGUMENTS.get(String.class), user, password);
            instance.getLogger().info("Connected to MySQL database.");
        } catch (SQLException sqlException) {
            instance.getLogger().error("Unable to connect to the database, cannot start the plugin.");
            sqlException.printStackTrace();
            instance.mysql_installation = true;
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