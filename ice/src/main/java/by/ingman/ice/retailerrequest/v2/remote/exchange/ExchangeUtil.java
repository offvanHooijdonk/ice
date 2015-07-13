package by.ingman.ice.retailerrequest.v2.remote.exchange;

import android.content.Context;

import java.util.List;
import java.util.Map;

import by.ingman.ice.retailerrequest.v2.helpers.DBHelper;
import by.ingman.ice.retailerrequest.v2.helpers.NotificationsUtil;
import by.ingman.ice.retailerrequest.v2.remote.dao.RequestDao;
import by.ingman.ice.retailerrequest.v2.structure.Request;

/**
 * Created by Yahor_Fralou on 7/13/2015.
 */
public class ExchangeUtil {
    private Context ctx;
    private DBHelper dbHelper;
    private RequestDao requestDao;
    private NotificationsUtil notifUtil;

    public ExchangeUtil(Context context) {
        this.ctx = context;

        dbHelper = new DBHelper(ctx);
        requestDao = new RequestDao(ctx);
        notifUtil = new NotificationsUtil(ctx);
    }

    public void sendRequests() {
        Map<String, List<Request>> requests = dbHelper.readUnsentRequests();

        for (String reqId : requests.keySet()) {
            List<Request> list = requests.get(reqId);

            boolean success = requestDao.batchAddRequests(list);
            if (success) {
                dbHelper.markRequestSent(reqId);

                if (list.size() > 0) { // just make sure, though else case should not be possible
                    Request reqInfo = list.get(0);
                    notifUtil.showRequestSentNotification(reqInfo);
                }
            }
        }
    }
}
