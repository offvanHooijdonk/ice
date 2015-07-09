package by.ingman.ice.retailerrequest.v2.dao.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;

/**
 * Created by off on 07.07.2015.
 */
public class Connector {
    // TODO move all values to preferences
    private static final String URL_FORMAT = "jdbc:jtds:sqlserver://{0}:{1}/{2}";
    private final SharedPreferences sharedPreferences;
    private Context ctx;

    public Connector(Context context) {
        this.ctx = context;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        try {
            //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // TODO log
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        String host = sharedPreferences.getString("host", "");
        String port = sharedPreferences.getString("port", "");
        String baseName = sharedPreferences.getString("baseName", "");
        String user = sharedPreferences.getString("usernameDB", "");
        String password = sharedPreferences.getString("password", "");
        String url = MessageFormat.format(URL_FORMAT, host, port, baseName);
        Connection connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);
        return connection;
    }
}
