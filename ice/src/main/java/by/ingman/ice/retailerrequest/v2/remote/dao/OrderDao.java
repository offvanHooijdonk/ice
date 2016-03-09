package by.ingman.ice.retailerrequest.v2.remote.dao;

import android.content.Context;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.Order;
import by.ingman.ice.retailerrequest.v2.structure.Answer;

/**
 * Created by off on 07.07.2015.
 */
public class OrderDao {
    private final Logger log = Logger.getLogger(OrderDao.class);

    private Context ctx;

    public OrderDao(Context context) {
        this.ctx = context;
    }

    public boolean batchInsertRequests(List<Order> orders) {
        boolean success = false;
        Connection conn = getConnection();

        if (conn != null) {
            try {
                PreparedStatement stat = conn.prepareStatement("INSERT INTO ice_requests(REQ_ID, AGENT_NAME, " +
                        "REQUEST_DATE, IS_COMMERCIAL, CONTRAGENT_CODE, SALE_POINT_CODE, STORE_HOUSE_CODE, PRODUCT_CODE, " +
                        "PRODUCT_PACKS_NUM, PRODUCT_NUM, COMMENT) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

                for (Order r : orders) {
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

    private void addRequestToBatch(Order order, PreparedStatement stat) throws Exception {
        stat.setString(1, order.getId());
        stat.setString(2, order.getManager());
        stat.setDate(3, new Date(order.getDate().getTime()));
        stat.setBoolean(4, Boolean.valueOf(order.getIsCommercial()));
        stat.setString(5, order.getContrAgentCode());
        stat.setString(6, order.getSalePointCode());
        stat.setString(7, order.getStorehouseCode());
        stat.setString(8, order.getProductCode());
        stat.setDouble(9, order.getProductPacksCount());
        stat.setInt(10, order.getProductCount());
        stat.setString(11, order.getComment());

        stat.addBatch();

    }

    public Answer findAnswer(String orderId) {
        Connection conn = getConnection();
        Answer answer = null;

        try {
            PreparedStatement stat = conn.prepareStatement("SELECT * FROM " + Answer.TABLE + " WHERE " + Answer.ORDER_ID + " = ? ");
            stat.setString(0, orderId);
            ResultSet rs = stat.executeQuery();

            if (rs.next()) {
                answer = new Answer();
                answer.setId(rs.getString(Answer.ORDER_ID));
                answer.setDesc(rs.getString(Answer.DESCRIPTION));
                answer.setUnloadTime(rs.getString(Answer.UNLOAD_TIME));
            }
        } catch (SQLException e) {
            log.error(e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }

        return answer;
    }

    private Connection getConnection() {
        Connector c = new Connector(ctx);
        Connection conn = null;
        try {
            conn = c.getConnection();
        } catch (Exception e) {
            log.error("Error getting connection to remote DB with url " + c.getConnectionURL(), e);
        }

        return conn;
    }

}
