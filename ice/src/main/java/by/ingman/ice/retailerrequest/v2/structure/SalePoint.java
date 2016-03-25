package by.ingman.ice.retailerrequest.v2.structure;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 05.05.13
 * Time: 19:28
 * To change this template use File | Settings | File Templates.
 */
//clients
public class SalePoint {
    // code_k
    private String contrAgentCode;
    // code_r
    private String code;
    // name_r
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

    @Override
    public String toString() {
        return String.format("%s %s", getCode(), getName());
    }
}
