package by.ingman.ice.retailerrequest.v2.remote.dao;

import android.content.Context;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.MessageFormat;

import by.ingman.ice.retailerrequest.v2.helpers.PreferenceHelper;

/**
 * Created by off on 07.07.2015.
 */
public class ConnectionFactory {
    private final Logger log = Logger.getLogger(ConnectionFactory.class);
    private static final String URL_FORMAT = "jdbc:jtds:sqlserver://{0}:{1}/{2}";

    private Context ctx;

    public ConnectionFactory(Context context) {
        this.ctx = context;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("Failed to load remote DB driver", e);
        }
    }

    public Connection getConnection() {
        String user = PreferenceHelper.RemoteDB.getUserName(ctx);
        String password = PreferenceHelper.RemoteDB.getPassword(ctx);

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
        String host = PreferenceHelper.RemoteDB.getHost(ctx);
        String port = PreferenceHelper.RemoteDB.getPort(ctx);
        String baseName = PreferenceHelper.RemoteDB.getDBName(ctx);
        return MessageFormat.format(URL_FORMAT, host, port, baseName);
    }
}
