package by.ingman.ice.retailerrequest.v2.remote.dao;

import android.content.Context;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.Product;
import by.ingman.ice.retailerrequest.v2.structure.Storehouse;

/**
 * Created by Yahor_Fralou on 10/28/2015.
 */
public class ProductDao {
    private final Logger log = Logger.getLogger(ProductDao.class);

    private Context ctx;

    public ProductDao(Context context) {
        this.ctx = context;
    }

    public Long getUnloadDate() throws Exception {
        Long date = null;
        Connection conn = new ConnectionFactory(ctx).getConnection();

        if (conn != null) {
            try {
                PreparedStatement stat = conn.prepareStatement("SELECT TOP(1) datetime_unload FROM rests WHERE datetime_unload IS NOT NULL order by datetime_unload desc");
                ResultSet rs = stat.executeQuery();

                if (rs.next()) {
                    date = rs.getTimestamp("datetime_unload").getTime();
                }
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

    public List<Product> getAll()throws Exception {
        List<Product> products = new ArrayList<>();
        Connection conn = new ConnectionFactory(ctx).getConnection();

        if (conn != null) {
            try {
                PreparedStatement stat = conn.prepareStatement("SELECT * FROM rests");
                ResultSet rs = stat.executeQuery();

                while (rs.next()) {
                    Product pr = new Product(
                            rs.getString("code_p"),
                            rs.getString("name_p"),
                            rs.getString("packs"),
                            rs.getString("amount"),
                            rs.getDouble("price"),
                            rs.getInt("amt_in_pack"),
                            rs.getDouble("gross_weight"),
                            new Storehouse(
                                    rs.getString("code_s"),
                                    rs.getString("name_s")
                            )
                    );

                    products.add(pr);
                }
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

        return products;
    }
}
