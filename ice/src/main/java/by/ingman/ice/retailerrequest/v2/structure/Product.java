package by.ingman.ice.retailerrequest.v2.structure;

public class Product {
    private String storehouseCode;
    private String code;
    private String name;
    private String packs;
    private String rest;
    private String price;
    private int countInPack;
    private String storehousePacks;
    private String storehouseRest;
    private double weight;



    public Product(String storehouseCode, String code, String name, String storehousePacks, String storehouseRest, String price, int countInPack, double weight) {
        this.storehouseCode = storehouseCode;
        this.code = code;
        this.name = name;
        this.packs = "0";
        this.rest = "0";
        this.price = price;
        this.countInPack = countInPack;
        this.storehousePacks = storehousePacks;
        this.storehouseRest = storehouseRest;
        this.weight = weight;
    }

    public Product(Product p) {
        this.storehouseCode = p.getStorehouseCode();
        this.code = p.getCode();
        this.name = p.getName();
        this.packs = "0";
        this.rest = "0";
        this.price = p.getPrice();
        this.countInPack = p.getCountInPack();
        this.storehousePacks = p.storehousePacks;
        this.storehouseRest = p.storehouseRest;
        this.weight = p.getWeight();
    }

    public double getWeight() {
        return weight;
    }

    public int getCountInPack() {
        return countInPack;
    }

    public String getStorehouseCode() {
        return storehouseCode;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPacks() {
        return packs;
    }

    public String getRest() {
        return rest;
    }

    public String getPrice() {
        return price;
    }

    public void setPacks(String packs) {
        this.packs = packs;
    }

    public void setRest(String rest) {
        this.rest = rest;
    }

    public String getStorehousePacks() {
        return storehousePacks;
    }

    public String getStorehouseRest() {
        return storehouseRest;
    }

    public String getTextViewText() {
        StringBuilder sb = new StringBuilder();
        sb.append(code).append(" ").append(name)
                .append("\n\rВыбрано упаковок: ").append(packs)
                .append("\n\rВыбрано штук: ").append(rest)
                .append("\n\rЦена: ").append(price)
                .append("\n\rНа складе: (").append(storehousePacks).append(" уп., ").append(storehouseRest).append(" шт)");
        return sb.toString();
    }
}
