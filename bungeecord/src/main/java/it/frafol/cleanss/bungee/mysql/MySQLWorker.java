package it.frafol.cleanss.bungee.mysql;

import it.frafol.cleanss.bungee.CleanSS;
import it.frafol.cleanss.bungee.enums.BungeeConfig;
import it.frafol.cleanss.bungee.objects.PlayerCache;
import it.frafol.cleanss.bungee.objects.SQLConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class MySQLWorker {
    private final SQLConnection connection = new SQLConnection(BungeeConfig.MYSQL_HOST.get(String.class), BungeeConfig.MYSQL_USER.get(String.class), BungeeConfig.MYSQL_PASSWORD.get(String.class), BungeeConfig.MYSQL_DATABASE.get(String.class));

    public MySQLWorker() {
        connect();
    }

    private void connect() {
        connection.execute("CREATE TABLE IF NOT EXISTS `DataPlayer` (`uuid` VARCHAR(36) PRIMARY KEY, `name` VARCHAR(16), `in_control` TINYINT(1), `controls` INT(16), `controls_suffered` INT(16))");
    }

    public void setupPlayer(UUID uuid) {

        if (!BungeeConfig.MYSQL.get(Boolean.class)) {
            return;
        }

        try {
            Statement statement = connection.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM `DataPlayer` WHERE `uuid`='" + uuid + "'");

            if (!rs.next()) {

                ProxiedPlayer player = CleanSS.getInstance().getProxy().getPlayer(uuid);

                if (!player.isConnected()) {
                    return;
                }

                connection.execute("INSERT INTO `DataPlayer` (`uuid`, `name`, `in_control`, `controls`, `controls_suffered`) VALUES ('" + uuid + "', '" + player.getName() + "', " + 0 + "', " + 0 + "', " + 0 + ")");
                PlayerCache.getControls().put(uuid, 0);
                PlayerCache.getIn_control().put(uuid, 0);
                PlayerCache.getControls_suffered().put(uuid, 0);

            }

            rs.close();
            statement.close();
        } catch (SQLException var9) {
            var9.printStackTrace();
        }

    }

    public int getStats(UUID uuid, String types) {

        if (!BungeeConfig.MYSQL.get(Boolean.class)) {
            return 0;
        }

        int incontrol = 0;
        int controls = 0;
        int suffered = 0;

        try {
            Statement statement = connection.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM `DataPlayer` WHERE `uuid`='" + uuid + "'");

            if (rs.next()) {
                incontrol = rs.getInt("in_control");
                controls = rs.getInt("controls");
                suffered = rs.getInt("controls_suffered");

            } else {
                ProxiedPlayer player = CleanSS.getInstance().getProxy().getPlayer(uuid);

                if (!player.isConnected()) {
                    return 0;
                }

                connection.execute("INSERT INTO `DataPlayer` (`uuid`, `name`, `in_control`, `controls`, `controls_suffered`) VALUES ('" + uuid + "', '" + player.getName() + "', '" + 0 + "', '" + 0 + "', '" + 0 +"')");
                PlayerCache.getControls().put(uuid, 0);
                PlayerCache.getIn_control().put(uuid, 0);

            }

            int data;
            switch (types) {
                case "incontrol":
                    data = incontrol;
                    PlayerCache.getIn_control().put(uuid, data);
                    break;
                case "controls":
                    data = controls;
                    PlayerCache.getControls().put(uuid, data);
                    break;
                case "suffered":
                    data = suffered;
                    PlayerCache.getControls_suffered().put(uuid, data);
                    break;
                default:
                    data = -1;
            }

            rs.close();
            statement.close();

            return data;
        } catch (SQLException var9) {
            var9.printStackTrace();
            return -1;
        }
    }

    public void setInControl(UUID uuid, Integer status) {

        if (!BungeeConfig.MYSQL.get(Boolean.class)) {
            return;
        }

        connection.execute("UPDATE `DataPlayer` SET `in_control`=" + status + " WHERE `uuid`='" + uuid + "';");
        PlayerCache.getIn_control().put(uuid, status);
    }

    public void setControls(UUID uuid, Integer controls) {

        if (!BungeeConfig.MYSQL.get(Boolean.class)) {
            return;
        }

        connection.execute("UPDATE `DataPlayer` SET `controls`=" + controls + " WHERE `uuid`='" + uuid + "';");
        PlayerCache.getControls().put(uuid, controls);
    }

    public void setControlsSuffered(UUID uuid, Integer controls) {

        if (!BungeeConfig.MYSQL.get(Boolean.class)) {
            return;
        }

        connection.execute("UPDATE `DataPlayer` SET `controls_suffered`=" + controls + " WHERE `uuid`='" + uuid + "';");
        PlayerCache.getControls().put(uuid, controls);

    }

    public void close() {
        try {
            this.connection.close();
        } catch (SQLException var2) {
            var2.printStackTrace();
        }
    }
}