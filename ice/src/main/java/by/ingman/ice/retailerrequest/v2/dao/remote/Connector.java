package by.ingman.ice.retailerrequest.v2.dao.remote;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by off on 07.07.2015.
 */
public class Connector {
    // TODO move all values to preferences
    /*private SQLServerDataSource ds;*/

    public Connector() {
        /*ds = new SQLServerDataSource();
        ds.setUser("sa");
        ds.setPassword("0");
        ds.setServerName("192.168.1.3");
        ds.setPortNumber(1433);
        ds.setDatabaseName("ice");
        ds.setSendTimeAsDatetime(true);
        ds.setSendStringParametersAsUnicode(true);*/

        try {
            //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // TODO log
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        /*Connection connection = ds.getConnection();
        connection.setAutoCommit(false);*/

        //Connection connection = DriverManager.getConnection("jdbc:sqlserver://192.168.1.3:1433;databaseName=ice", "sa", "0");
        Connection connection = DriverManager.getConnection("jdbc:jtds:sqlserver://192.168.1.3:1433:/ice", "sa", "0");

        return connection;
    }
}
