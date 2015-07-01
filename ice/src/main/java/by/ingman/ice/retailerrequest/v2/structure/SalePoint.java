package by.ingman.ice.retailerrequest.v2.structure;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 05.05.13
 * Time: 19:28
 * To change this template use File | Settings | File Templates.
 */
public class SalePoint {
    private String contrAgentCode;
    private String code;
    private String name;

    public SalePoint(String contrAgentCode, String code, String name) {
        this.contrAgentCode = contrAgentCode;
        this.code = code;
        this.name = name;
    }

    public String getContrAgentCode() {
        return contrAgentCode;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
