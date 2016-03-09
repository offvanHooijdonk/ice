package by.ingman.ice.retailerrequest.v2.structure;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 09.06.13
 * Time: 0:50
 * To change this template use File | Settings | File Templates.
 */
// results
public class Answer {
    public static final String TABLE = "results";
    public static final String ORDER_ID = "order_ids";
    public static final String DESCRIPTION = "description";
    public static final String UNLOAD_TIME = "datetime_unload";

    // order_id
    private String id;
    // !!
    private String resCode;
    // description
    private String desc;
    // datetime_unload
    private String unloadTime;

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

    public String getUnloadTime() {
        return unloadTime;
    }

    public void setUnloadTime(String unloadTime) {
        this.unloadTime = unloadTime;
    }
}
