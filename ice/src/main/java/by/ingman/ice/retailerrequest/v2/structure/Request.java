package by.ingman.ice.retailerrequest.v2.structure;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 26.05.13
 * Time: 16:09
 * To change this template use File | Settings | File Templates.
 */
public class Request {

    private static final String SEPARATOR = ";";
    private static final String NEWLINE_HTML = "<br>";
    private static final String NEWLINE = "\n";



    private String id;
    private String manager;
    private String date;
    private String isCommercial;
    private String contrAgentCode;
    private String contrAgentName;
    private String salePointCode;
    private String salePointName;
    private String storehouseCode;
    private String storehouseName;
    private String productCode;
    private String productName;
    private Double productPacksCount;
    private Integer productCount;
    private String comment;

    public static String generateNewId() {
        return UUID.randomUUID().toString();
    }

    public static DateFormat getDateFormat() {
        return SimpleDateFormat.getDateInstance(DateFormat.SHORT, new Locale("ru", "RU"));
    }

    public Request(String id, String manager, boolean isCommercial, ContrAgent contrAgent, SalePoint salePoint,
                   Storehouse storehouse, Product product, Date date, String comment)
    {
        this.id = id;
        this.manager = manager;
        this.date = getDateFormat().format(date);
        this.isCommercial = isCommercial ? "1" : "0";
        this.contrAgentCode = contrAgent.getCode();
        this.contrAgentName = contrAgent.getName();
        this.salePointCode = salePoint.getCode();
        this.salePointName = salePoint.getName();
        this.storehouseCode = storehouse.getCode();
        this.storehouseName = storehouse.getName();
        this.productCode = product.getCode();
        this.productName = product.getName();
        this.productPacksCount = product.getPacks();//.replaceAll(",", ".");
        this.productCount = product.getRest();
        if (comment.length() > 200) {
            comment = comment.substring(0, 200);
        }
        this.comment = comment;
    }

    public Request(String request) {
        String[] array = request.split(";");
        id = array[0];
        manager = array[1];
        date = array[2];
        isCommercial = array[3];
        contrAgentCode = array[4];
        contrAgentName = array[5];
        salePointCode = array[6];
        salePointName = array[7];
        storehouseCode = array[8];
        storehouseName = array[9];
        productCode = array[10];
        productName = array[11];
        productPacksCount = Double.valueOf(array[12]);
        productCount = Integer.valueOf(array[13]);
        comment = array[14];
        ArrayList<String> products = new ArrayList<String>();
        for (int i = 10; i < array.length; i += 14) {
            StringBuilder sb = new StringBuilder();
            sb.append("Товар: ")
                    .append(array[i])
                    .append(" ")
                    .append(array[i + 1])
                    .append("\n")
                    .append("Упаковок")
                    .append(array[i + 2])
                    .append(", штук ")
                    .append(array[i + 3]);

        }
    }

    public static String parseRequest(String request) {
        String[] array = request.split(";");
        StringBuilder sb = new StringBuilder();
        sb.append("<b>ЗАЯВКА</b><br>")
                //.append("<b>ID: </b>").append(array[0]).append(NEWLINE_HTML)
                //.append("<b>Менеджер: </b>").append(array[1]).append(NEWLINE_HTML)
                //.append("<b>Дата: </b>").append(array[2]).append(NEWLINE_HTML)
                //.append("<b>Реклама: </b>").append(array[3]).append(NEWLINE_HTML)
                .append("<b>Клиент: </b>").append(array[4]).append(" ").append(array[5]).append(NEWLINE_HTML)
                .append("<b>Разгрузка: </b>").append(array[6]).append(" ").append(array[7]).append(NEWLINE_HTML);
                //.append("<b>Склад: </b>").append(array[8]).append(" ").append(array[9]);
        /*for (int i = 10; i < array.length; i += 14) {
            StringBuilder sbProd = new StringBuilder();
            sbProd.append("<br><b>Товар</b>: ")
                    .append(array[i])
                    .append(" ")
                    .append(array[i + 1])
                    .append("<br><b>Упаковок</b> ")
                    .append(array[i + 2])
                    .append(", <b>штук</b> ")
                    .append(array[i + 3]);
            sb.append(sbProd.toString());*//*
        }*/
        return sb.toString();
    }

    public static String parseRequestFully(String request) {
        String[] array = request.split(";");
        StringBuilder sb = new StringBuilder();
        sb.append("ЗАЯВКА\n")
                .append("ID: ").append(array[0]).append(NEWLINE)
                .append("Менеджер: ").append(array[1]).append(NEWLINE)
                .append("Дата: ").append(array[2]).append(NEWLINE)
                .append("Реклама: ").append(array[3]).append(NEWLINE)
                .append("Клиент: ").append(array[4]).append(" ").append(array[5]).append(NEWLINE)
                .append("Разгрузка: ").append(array[6]).append(" ").append(array[7]).append(NEWLINE)
                .append("Склад: ").append(array[8]).append(" ").append(array[9]).append(NEWLINE);
        for (int i = 10; i < array.length; i += 15) {
            StringBuilder sbProd = new StringBuilder();
            sbProd.append("Товар: ")
                    .append(array[i])
                    .append(" ")
                    .append(array[i + 1]).append(NEWLINE)
                    .append("упаковок ")
                    .append(array[i + 2]).append(NEWLINE)
                    .append("штук ")
                    .append(array[i + 3]).append(NEWLINE);
            sb.append(sbProd.toString());
        }
        sb.append("Комментарий: ").append(array[array.length-1]);
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String toStringForSending() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(SEPARATOR)
                .append(manager).append(SEPARATOR)
                .append(date).append(SEPARATOR)
                .append(isCommercial).append(SEPARATOR)
                .append(contrAgentCode).append(SEPARATOR)
                .append(contrAgentName).append(SEPARATOR)
                .append(salePointCode).append(SEPARATOR)
                .append(salePointName).append(SEPARATOR)
                .append(storehouseCode).append(SEPARATOR)
                .append(storehouseName).append(SEPARATOR)
                .append(productCode).append(SEPARATOR)
                .append(productName).append(SEPARATOR)
                .append(productPacksCount).append(SEPARATOR)
                .append(productCount).append(SEPARATOR)
                .append(comment == null ||  comment.equals("") ? " " : comment);
        return sb.toString();
    }

}
