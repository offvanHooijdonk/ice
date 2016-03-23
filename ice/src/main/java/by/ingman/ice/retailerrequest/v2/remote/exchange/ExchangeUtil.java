package by.ingman.ice.retailerrequest.v2.remote.exchange;

import android.content.Context;

import java.util.List;
import java.util.Map;

import by.ingman.ice.retailerrequest.v2.helpers.NotificationsUtil;
import by.ingman.ice.retailerrequest.v2.local.dao.OrderLocalDao;
import by.ingman.ice.retailerrequest.v2.remote.dao.OrderDao;
import by.ingman.ice.retailerrequest.v2.structure.Order;

/**
 * Created by Yahor_Fralou on 7/13/2015.
 */
public class ExchangeUtil {
    private Context ctx;
    private OrderLocalDao orderLocalDao;
    private OrderDao orderDao;
    private NotificationsUtil notifUtil;

    public ExchangeUtil(Context context) {
        this.ctx = context;

        orderLocalDao = new OrderLocalDao(ctx);
        orderDao = new OrderDao(ctx);
        notifUtil = new NotificationsUtil(ctx);
    }

    public void sendRequests() throws Exception {
        Map<String, List<Order>> requests = orderLocalDao.getUnsentOrders();

        for (String reqId : requests.keySet()) {
            List<Order> list = requests.get(reqId);

            boolean success = orderDao.batchInsertOrders(list);
            if (success) {
                orderLocalDao.markOrderSent(reqId);

                if (list.size() > 0) { // just make sure, though 'else' case must not be possible
                    Order order = list.get(0);
                    notifUtil.showOrderSentNotification(order);
                }
            }
        }
    }
}
