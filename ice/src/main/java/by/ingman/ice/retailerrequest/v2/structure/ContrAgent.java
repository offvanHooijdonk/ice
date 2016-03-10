package by.ingman.ice.retailerrequest.v2.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 05.05.13
 * Time: 19:27
 * To change this template use File | Settings | File Templates.
 */
public class ContrAgent {
    // code_k
    private String code;
    // name_k
    private String name;
    // code_r
    private List<SalePoint> salePoints = new ArrayList<>();

    public ContrAgent() {
    }

    public ContrAgent(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SalePoint> getSalePoints() {
        return salePoints;
    }

    public void setSalePoints(List<SalePoint> salePoints) {
        this.salePoints = salePoints;
    }

    public void addSalePoint(SalePoint salePoint) {
        salePoints.add(salePoint);
    }
}
