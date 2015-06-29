package com.example.ice.structure;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 05.05.13
 * Time: 19:27
 * To change this template use File | Settings | File Templates.
 */
public class ContrAgent {
    private String code;
    private String name;
    private String salePoints;

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

    /*public LinkedList<SalePoint> getSalePoints() {
        String[] array = salePoints.split(";");

    }*/

    public String[] getSalePoints() {
        String[] array = salePoints.split(";");
        String[] result = new String[array.length / 2];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i * 2] + array[i * 2 + 1];
        }
        return result;
    }

    public void addSalePoint(String code, String name) {
        salePoints += code + ";" + "" + name;
    }
}
