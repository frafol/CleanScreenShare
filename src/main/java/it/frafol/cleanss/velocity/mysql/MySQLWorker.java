package it.frafol.cleanss.velocity.mysql;

import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanss.velocity.CleanSS;
import it.frafol.cleanss.velocity.enums.VelocityConfig;
import it.frafol.cleanss.velocity.objects.PlayerCache;
import it.frafol.cleanss.velocity.objects.SQLConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

public class MySQLWorker {
    private final SQLConnection connection = new SQLConnection(VelocityConfig.MYSQL_HOST.get(String.class), VelocityConfig.MYSQL_USER.get(String.class), VelocityConfig.MYSQL_PASSWORD.get(String.class), VelocityConfig.MYSQL_DATABASE.get(String.class));

    public MySQLWorker() {
        connect();
    }

    private void connect() {
        connection.execute("CREATE TABLE IF NOT EXISTS `DataPlayer` (`uuid` VARCHAR(36) PRIMARY KEY, `name` VARCHAR(16), `in_control` TINYINT(1), `controls` INT(16), `controls_suffered` INT(16))");
    }

    public void setupPlayer(UUID uuid) {

        if (!VelocityConfig.MYSQL.get(Boolean.class)) {
            return;
        }

        try {
            Statement statement = connection.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM `DataPlayer` WHERE `uuid`='" + uuid + "'");

            if (!rs.next()) {

                Optional<Player> player = CleanSS.getInstance().getServer().getPlayer(uuid);

                if (!player.isPresent()) {
                    return;
                }

                connection.execute("INSERT INTO `DataPlayer` (`uuid`, `name`, `in_control`, `controls`, `controls_suffered`) VALUES ('" + uuid + "', '" + player.get().getUsername() + "', " + 0 + "', " + 0 + "', " + 0 + ")");
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

        if (!VelocityConfig.MYSQL.get(Boolean.class)) {
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
                Optional<Player> player = CleanSS.getInstance().getServer().getPlayer(uuid);

                if (!player.isPresent()) {
                    return 0;
                }

                connection.execute("INSERT INTO `DataPlayer` (`uuid`, `name`, `in_control`, `controls`, `controls_suffered`) VALUES ('" + uuid + "', '" + player.get().getUsername() + "', '" + 0 + "', '" + 0 + "', '" + 0 +"')");
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

        if (!VelocityConfig.MYSQL.get(Boolean.class)) {
            return;
        }

        connection.execute("UPDATE `DataPlayer` SET `in_control`=" + status + " WHERE `uuid`='" + uuid + "';");
        PlayerCache.getIn_control().put(uuid, status);
    }

    public void setControls(UUID uuid, Integer controls) {

        if (!VelocityConfig.MYSQL.get(Boolean.class)) {
            return;
        }

        connection.execute("UPDATE `DataPlayer` SET `controls`=" + controls + " WHERE `uuid`='" + uuid + "';");
        PlayerCache.getControls().put(uuid, controls);
    }

    public void setControlsSuffered(UUID uuid, Integer controls) {

        if (!VelocityConfig.MYSQL.get(Boolean.class)) {
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