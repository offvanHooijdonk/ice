package by.ingman.ice.retailerrequest.v2.remote.dao;

import android.content.Context;

import java.util.Date;
import java.util.List;

import by.ingman.ice.retailerrequest.v2.structure.Product;

/**
 * Created by Yahor_Fralou on 10/28/2015.
 */
public class ProductDao {

    private Context ctx;

    public ProductDao(Context context) {
        this.ctx = context;
    }

    public List<Product> getUpdates(Date date) {

        // FIXME
        return null;
    }
}
