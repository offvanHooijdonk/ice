package by.ingman.ice.retailerrequest.v2.structure;

// rests
public class Product {
    // code_s
    private String storehouseCode;
    // code_p
    private String code;
    // name_p
    private String name;
    private Double packs;
    private Integer rest;
    // price
    private Integer price;
    // amt_in_pack
    private int countInPack;
    // packs
    private String storehousePacks;
    // amount
    private String storehouseRest;
    // gross_weight
    private double weight;



    public Product(String storehouseCode, String code, String name, String storehousePacks, String storehouseRest,
                   Integer price, int countInPack, double weight) {
        this.storehouseCode = storehouseCode;
        this.code = code;
        this.name = name;
        this.packs = 0.0;
        this.rest = 0;
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
        this.packs = 0.0;
        this.rest = 0;
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

    public Double getPacks() {
        return packs;
    }

    public Integer getRest() {
        return rest;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPacks(Double packs) {
        this.packs = packs;
    }

    public void setRest(Integer rest) {
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
