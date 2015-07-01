package by.ingman.ice.retailerrequest.v2.structure;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 09.05.13
 * Time: 23:21
 * To change this template use File | Settings | File Templates.
 */
public class Storehouse {
    private String code;
    private String name;

    public Storehouse(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
