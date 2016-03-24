package by.ingman.ice.retailerrequest.v2.structure;

import android.content.Context;

import java.util.List;

import by.ingman.ice.retailerrequest.v2.local.dao.ContrAgentLocalDao;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 05.05.13
 * Time: 19:35
 * To change this template use File | Settings | File Templates.
 */
public class ContrAgentHelper {
    private List<ContrAgent> filteredList;
    private ContrAgentLocalDao contrAgentLocalDao;

    public ContrAgentHelper(Context ctx) {
        contrAgentLocalDao = new ContrAgentLocalDao(ctx);
    }

    public String[] getContrAgentsNamesArray(String filter) {
        filteredList = contrAgentLocalDao.getContrAgents(filter);
        String[] names = new String[filteredList.size()];
        int i = 0;
        for (ContrAgent c : filteredList) {
            names[i++] = String.format("%s %s", c.getCode(), c.getName());
        }

        return names;
    }

    public ContrAgent getSelectedContrAgent(int checkedItemPosition) {
        return filteredList.get(checkedItemPosition);
    }


    public String[] getSalePointsNamesArray(ContrAgent ca, String filter) {
        List<SalePoint> salePoints = contrAgentLocalDao.getSalePointsByContrAgent(ca, filter);
        String[] names = new String[salePoints.size()];
        int i = 0;
        for (SalePoint sp : salePoints) {
            names[i++] = String.format("%s %s", sp.getCode(), sp.getName());
        }
        return names;
    }

}
