package by.ingman.ice.retailerrequest.v2.structure;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 09.06.13
 * Time: 0:50
 * To change this template use File | Settings | File Templates.
 */
// results
public class Answer {
    // order_id
    private String orderId;
    // description
    private String description;
    // datetime_unload
    private Date unloadTime;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toStringForViewing() {
        return description;
    }

    public Date getUnloadTime() {
        return unloadTime;
    }

    public void setUnloadTime(Date unloadTime) {
        this.unloadTime = unloadTime;
    }
}
