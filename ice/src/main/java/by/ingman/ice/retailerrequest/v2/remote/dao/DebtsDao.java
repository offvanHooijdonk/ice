package by.ingman.ice.retailerrequest.v2.remote.dao;

import android.content.Context;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.Debt;

/**
 * Created by Yahor_Fralou on 3/14/2016.
 */
public class DebtsDao {
    private final Logger log = Logger.getLogger(DebtsDao.class);

    Context ctx;

    public DebtsDao(Context context) {
        this.ctx = context;
    }

    public Long getUnloadDate() {
        Long date = null;
        Connection conn = new ConnectionFactory(ctx).getConnection();

        if (conn != null) {
            try {
                PreparedStatement stat = conn.prepareStatement("SELECT datetime_unload FROM debts TOP 1");
                ResultSet rs = stat.executeQuery();

                if (rs.next()) {
                    date = rs.getDate("datetime_unload").getTime();
                }
            } catch (Exception e) {
                log.error("Error getting Debts unload date from remote DB.", e);
            } finally {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    log.error("Error closing connection to remote DB.", e);
                }
            }
        } else {
            log.error("Connection to remote DB is null.");
        }

        return date;
    }

    public List<Debt> getDebts() {
        List<Debt> debts = new ArrayList<>();
        Connection conn = new ConnectionFactory(ctx).getConnection();

        if (conn != null) {
            try {
                PreparedStatement stat = conn.prepareStatement("SELECT * FROM debts");
                ResultSet rs = stat.executeQuery();

                while (rs.next()) {
                    Debt d = new Debt(
                            rs.getString("code_k"),
                            rs.getString("rating"),
                            rs.getString("debt"),
                            rs.getString("overdue")
                    );

                    debts.add(d);
                }
            } catch (Exception e) {
                log.error("Error getting Debts from remote DB.", e);
            } finally {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    log.error("Error closing connection to remote DB.", e);
                }
            }
        } else {
            log.error("Connection to remote DB is null.");
        }

        return debts;
    }
}
