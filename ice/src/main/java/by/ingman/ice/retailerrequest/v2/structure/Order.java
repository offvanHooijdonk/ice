package by.ingman.ice.retailerrequest.v2.structure;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 26.05.13
 * Time: 16:09
 * To change this template use File | Settings | File Templates.
 */
public class Order {

    private static final String NEWLINE_HTML = "<br>";
    private static final String NEWLINE = "\n";

    private static DecimalFormat decimalFormat = new DecimalFormat("#.###");

    private String id;
    private String manager;
    private Date date;
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

    public Order(String id, String manager, boolean isCommercial, ContrAgent contrAgent, SalePoint salePoint,
                 Storehouse storehouse, Product product, Date date, String comment)
    {
        this.id = id;
        this.manager = manager;
        this.date = date;
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

    public Order(String request) throws ParseException {
        String[] array = request.split(";");
        id = array[0];
        manager = array[1];
        date = getDateFormat().parse(array[2]);
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

    public static String toReportString(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>ЗАЯВКА</b><br>")
                .append("<b>Клиент: </b>").append(order.getContrAgentCode()).append(" ").append(order.getContrAgentName()).append(NEWLINE_HTML)
                .append("<b>Разгрузка: </b>").append(order.getSalePointCode()).append(" ").append(order.getSalePointName()).append(NEWLINE_HTML);
        return sb.toString();
    }

    public static String toReportVerboseString(List<Order> orders) {
        Order order = orders.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("ЗАЯВКА\n")
                .append("ID: ").append(order.getId()).append(NEWLINE)
                .append("Менеджер: ").append(order.getManager()).append(NEWLINE)
                .append("Дата: ").append(Order.getDateFormat().format(order.getDate())).append(NEWLINE)
                .append("Реклама: ").append(order.getIsCommercial()).append(NEWLINE)
                .append("Клиент: ").append(order.getContrAgentCode()).append(" ").append(order.getContrAgentName()).append(NEWLINE)
                .append("Разгрузка: ").append(order.getSalePointCode()).append(" ").append(order.getSalePointName()).append(NEWLINE)
                .append("Склад: ").append(order.getStorehouseCode()).append(" ").append(order.getStorehouseName()).append(NEWLINE);
        for (Order r : orders) {
            StringBuilder sbProd = new StringBuilder();
            sbProd.append("Товар: ")
                    .append(r.getProductCode())
                    .append(" ")
                    .append(r.getProductName()).append(NEWLINE)
                    .append("упаковок ")
                    .append(decimalFormat.format(r.getProductPacksCount())).append(NEWLINE)
                    .append("штук ")
                    .append(r.getProductCount()).append(NEWLINE);
            sb.append(sbProd.toString());
        }
        sb.append("Комментарий: ").append(order.getComment());
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getManager() {
        return manager;
    }

    public String getIsCommercial() {
        return isCommercial;
    }

    public String getContrAgentCode() {
        return contrAgentCode;
    }

    public String getContrAgentName() {
        return contrAgentName;
    }

    public String getSalePointCode() {
        return salePointCode;
    }

    public String getSalePointName() {
        return salePointName;
    }

    public String getStorehouseCode() {
        return storehouseCode;
    }

    public String getStorehouseName() {
        return storehouseName;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public Double getProductPacksCount() {
        return productPacksCount;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public String getComment() {
        return comment;
    }

}
