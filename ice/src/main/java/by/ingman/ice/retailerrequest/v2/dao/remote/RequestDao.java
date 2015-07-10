package by.ingman.ice.retailerrequest.v2.dao.remote;

import android.content.Context;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.Request;

/**
 * Created by off on 07.07.2015.
 */
public class RequestDao {
    private final Logger log = Logger.getLogger(RequestDao.class);

    private Context ctx;

    public RequestDao(Context context) {
        this.ctx = context;
    }

    public boolean batchAddRequests(List<Request> requests) {
        boolean success = true;
        Connection conn = getConnection();

        if (conn != null) {
            try {
                PreparedStatement stat = conn.prepareStatement("INSERT INTO ice_requests(REQ_ID, AGENT_NAME, " +
                        "REQUEST_DATE, IS_COMMERCIAL, CONTRAGENT_CODE, SALE_POINT_CODE, STORE_HOUSE_CODE, PRODUCT_CODE, " +
                        "PRODUCT_PACKS_NUM, PRODUCT_NUM, COMMENT) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

                for (Request r : requests) {
                    addRequestToBatch(r, stat);
                }

                stat.executeBatch();
                success = true;
            } catch (Exception e) {
                log.error("Error batch inserting request to remote DB.", e);
                success = false;
                try {
                    if (conn.isClosed()) {
                        conn.rollback();
                        conn.close();
                    }
                } catch (SQLException e1) {
                    log.error("Error closing connection to remote DB after insert failure.", e1);
                }
            } finally {
                try {
                    if (!conn.isClosed()) {
                        if (success) {
                            conn.commit();
                        } else {
                            conn.rollback();
                        }
                        conn.close();
                    }
                } catch (SQLException e) {
                    log.error("Error closing connection to remote DB after successful insert.", e);
                }
            }
        } else {
            success = false;
            log.error("Connection to remote DB is null.");
        }

        return success;
    }

    private void addRequestToBatch(Request request, PreparedStatement stat) throws Exception {
        stat.setString(1, request.getId());
        stat.setString(2, request.getManager());
        stat.setDate(3, new Date(request.getDate().getTime()));
        stat.setBoolean(4, Boolean.valueOf(request.getIsCommercial()));
        stat.setString(5, request.getContrAgentCode());
        stat.setString(6, request.getSalePointCode());
        stat.setString(7, request.getStorehouseCode());
        stat.setString(8, request.getProductCode());
        stat.setDouble(9, request.getProductPacksCount());
        stat.setInt(10, request.getProductCount());
        stat.setString(11, request.getComment());

        stat.addBatch();

    }

    private Connection getConnection() {
        Connector c = new Connector(ctx);
        Connection conn = null;
        try {
            conn = c.getConnection();
        } catch (SQLException e) {
            log.error("Error getting connection to remote DB with url " + c.getConnectionURL(), e);
        }

        return conn;
    }
}
