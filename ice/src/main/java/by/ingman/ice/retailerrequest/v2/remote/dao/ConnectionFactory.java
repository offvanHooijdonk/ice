package by.ingman.ice.retailerrequest.v2.remote.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.MessageFormat;

/**
 * Created by off on 07.07.2015.
 */
public class ConnectionFactory {
    private final Logger log = Logger.getLogger(ConnectionFactory.class);
    private static final String URL_FORMAT = "jdbc:jtds:sqlserver://{0}:{1}/{2}";
    private final SharedPreferences sharedPreferences;
    private Context ctx;

    public ConnectionFactory(Context context) {
        this.ctx = context;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("Failed to load remote DB driver", e);
        }
    }

    public Connection getConnection() {
        String user = sharedPreferences.getString("usernameDB", "");
        String password = sharedPreferences.getString("password", "");

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(getConnectionURL(), user, password);
            connection.setAutoCommit(false);
        } catch (Exception e) {
            log.error("Error getting connection to remote DB with url " + getConnectionURL(), e);
        }
        return connection;
    }

    public String getConnectionURL() {
        String host = sharedPreferences.getString("host", "");
        String port = sharedPreferences.getString("port", "");
        String baseName = sharedPreferences.getString("baseName", "");
        return MessageFormat.format(URL_FORMAT, host, port, baseName);
    }
}
