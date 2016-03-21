package by.ingman.ice.retailerrequest.v2.remote.dao;

import android.content.Context;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.ContrAgent;
import by.ingman.ice.retailerrequest.v2.structure.SalePoint;

/**
 * Created by Yahor_Fralou on 3/10/2016.
 */
public class ContrAgentDao {
    private final Logger log = Logger.getLogger(ContrAgentDao.class);
    private Context ctx;

    public ContrAgentDao(Context context) {
        this.ctx = context;
    }

    public List<ContrAgent> getContrAgentList(Date date) {
        List<ContrAgent> contrAgents = new ArrayList<>();
        Connection conn = new ConnectionFactory(ctx).getConnection();

        if (conn != null) {
            try {
                PreparedStatement stat = conn.prepareStatement("SELECT * FROM clients WHERE datetime_unload >= ? ORDER BY name_k");
                stat.setDate(1, new java.sql.Date(date.getTime()));
                ResultSet rs = stat.executeQuery();
                ContrAgent ca = null;
                while (rs != null && rs.next()) {
                    String caCode = rs.getString("code_k");
                    if (ca == null || !ca.getCode().equals(caCode)) { // if new code not equal to previous - add ContrAgent and create a new one, else - just add new Sale Store
                        if (ca != null) {
                            contrAgents.add(ca);
                        }
                        ca = new ContrAgent();
                        ca.setCode(caCode);
                        ca.setName(rs.getString("name_k"));
                    }
                    ca.addSalePoint(new SalePoint(caCode, rs.getString("code_r"), rs.getString("name_r")));
                }
                if (ca != null) { // add the last ContrAgent
                    contrAgents.add(ca);
                }

            } catch (Exception e) {
                log.error("Error getting contrAgents from remote DB.", e);
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

        return contrAgents;
    }
}
