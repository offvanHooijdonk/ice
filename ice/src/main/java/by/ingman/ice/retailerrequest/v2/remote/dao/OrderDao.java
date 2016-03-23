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
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_RESULTS = "results";
    private static final String ORDER_ID = "order_id";
    private static final String DESCRIPTION = "description";
    private static final String UNLOAD_TIME = "datetime_unload";

    private final Logger log = Logger.getLogger(OrderDao.class);

    private Context ctx;

    public OrderDao(Context context) {
        this.ctx = context;
    }

    public boolean batchInsertOrders(List<Order> orders) throws Exception {
        boolean success = false;
        Connection conn = new ConnectionFactory(ctx).getConnection();

        if (conn != null) {
            try {
                PreparedStatement stat = conn.prepareStatement("INSERT INTO " + TABLE_ORDERS + "(ORDER_ID, name_m, " +
                        "order_date, is_advertising, code_k, name_k, code_r, name_r, code_s, name_s, code_p, name_p, amt_packs, amount, comments, in_datetime) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

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
                throw e;
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
        int i = 1;
        stat.setString(i++, order.getOrderId());
        stat.setString(i++, order.getManager());
        stat.setDate(i++, new Date(order.getOrderDate().getTime()));
        stat.setBoolean(i++, Boolean.valueOf(order.getIsCommercial()));
        stat.setString(i++, order.getContrAgentCode());
        stat.setString(i++, order.getContrAgentName());
        stat.setString(i++, order.getSalePointCode());
        stat.setString(i++, order.getSalePointName());
        stat.setString(i++, order.getStorehouseCode());
        stat.setString(i++, order.getStorehouseName());
        stat.setString(i++, order.getProductCode());
        stat.setString(i++, order.getProductName());
        stat.setDouble(i++, order.getProductPacksCount());
        stat.setInt(i++, order.getProductCount());
        stat.setString(i++, order.getComment());
        stat.setDate(i, new Date(new java.util.Date().getTime()));

        stat.addBatch();
    }

    public Answer findAnswer(String orderId) throws Exception {
        Connection conn = new ConnectionFactory(ctx).getConnection();
        Answer answer = null;

        try {
            PreparedStatement stat = conn.prepareStatement("SELECT * FROM " + TABLE_RESULTS + " WHERE " + ORDER_ID + " = ? ");
            stat.setString(1, orderId);
            ResultSet rs = stat.executeQuery();

            if (rs.next()) {
                answer = new Answer();
                answer.setOrderId(rs.getString(ORDER_ID));
                answer.setDescription(rs.getString(DESCRIPTION));
                answer.setUnloadTime(rs.getDate(UNLOAD_TIME));
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }

        return answer;
    }

}
