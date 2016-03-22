package by.ingman.ice.retailerrequest.v2.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 05.05.13
 * Time: 19:35
 * To change this template use File | Settings | File Templates.
 */
public class ContrAgentList {

    HashMap<String, ContrAgent> contrAgentHashMap = new HashMap<String, ContrAgent>();

    public void addAllContrAgents(List<ContrAgent> caList) {
        for (ContrAgent ca : caList) {
            addContrAgent(ca);
        }
    }

    public void addContrAgent(ContrAgent contrAgent) {
        if (!contrAgentHashMap.containsKey(contrAgent.getCode())) {
            contrAgentHashMap.put(contrAgent.getCode(), contrAgent);
        }
    }


    public String[] getContrAgentsNamesArray() {
        LinkedList<String> list = new LinkedList<String>();
        for (ContrAgent ca : contrAgentHashMap.values()) {
            list.add(ca.getCode() + " " + ca.getName());
        }
        return list.toArray(new String[list.size()]);
    }


    public String[] getContrAgentsSortedNamesArray(String namePart) {
        if (null == namePart || namePart.equals("")) {
            return getContrAgentsNamesArray();
        }
        LinkedList<String> list = new LinkedList<String>();
        for (ContrAgent ca : contrAgentHashMap.values()) {
            String s = ca.getCode() + " " + ca.getName();
            if (s.toLowerCase().contains(namePart.toLowerCase())) {
                list.add(s);
            }
        }
        return list.toArray(new String[list.size()]);
    }


    public ContrAgent getCurrentContrAgent(String namePart, int checkedItemPosition) {
        if (null == namePart || namePart.equals("")) {
            return contrAgentHashMap.values().toArray(new ContrAgent[]{})[checkedItemPosition];
        }
        LinkedList<ContrAgent> temp = new LinkedList<ContrAgent>();
        for (ContrAgent ca : contrAgentHashMap.values()) {
            if (ca.getName().toLowerCase().contains(namePart.toLowerCase())) {
                temp.add(ca);
            }
        }
        return temp.get(checkedItemPosition);
    }


    public String[] getSalePointsNamesArray(List<SalePoint> salePoints, String filter) {
        List<String> result = new ArrayList<>();
        for (SalePoint sp : salePoints) {
            String val = sp.getCode() + " " + sp.getName();
            if (val.toLowerCase().contains(filter.toLowerCase())) {
                result.add(val);
            }
        }
        return result.toArray(new String[result.size()]);
    }


    public List<SalePoint> getSalePoints(List<SalePoint> salePoints, String filter) {
        List<SalePoint> result = new ArrayList<SalePoint>();
        for (SalePoint sp : salePoints) {
            if (sp.getName().toLowerCase().contains(filter.toLowerCase()) || sp.getCode().contains(filter.toLowerCase())) {
                result.add(sp);
            }
        }
        return result;
    }


    /*public Collection<ContrAgent> getContrAgentsCollection(String filter) {
        if (filter == null || filter.equals("")) {
            return contrAgentHashMap.values();
        } else {
            Collection<ContrAgent> result = new ArrayList<ContrAgent>();
            for (ContrAgent ca : contrAgentHashMap.values()) {
                if (ca.getName().toLowerCase().contains(filter.toLowerCase()) || ca.getCode().contains(filter.toLowerCase())) {
                    result.add(ca);
                }
            }
            return result;
        }
    }*/

    public void clear() {
        contrAgentHashMap = new HashMap<String, ContrAgent>();
    }

    public int getSize() {
        return contrAgentHashMap.size();
    }
}
