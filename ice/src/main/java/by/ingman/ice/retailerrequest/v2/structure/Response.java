package by.ingman.ice.retailerrequest.v2.structure;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 09.06.13
 * Time: 0:50
 * To change this template use File | Settings | File Templates.
 */
public class Response {
    private String id;
    private String resCode;
    private String desc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResCode() {
        return resCode;
    }

    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String toStringForViewing() {
        return resCode + " " + desc;
    }
}
