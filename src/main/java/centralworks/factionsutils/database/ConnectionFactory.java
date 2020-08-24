package centralworks.factionsutils.database;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.lib.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory {

    public static Connection make() {
        try {
            final Configuration configuration = Main.getConfiguration();
            String password = configuration.get("MySQL.Password", false);
            String user = configuration.get("MySQL.User", false);
            String host = configuration.get("MySQL.Host", false);
            String port = configuration.get("MySQL.Port", false);
            String database = configuration.get("MySQL.Database", false);
            String type = "jdbc:mysql://";
            String url = type + host + ":" + port + "/" + database;
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
