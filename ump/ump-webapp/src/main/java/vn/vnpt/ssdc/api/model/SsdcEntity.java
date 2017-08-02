package vn.vnpt.ssdc.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by vietnq on 10/25/16.
 */
public class SsdcEntity<ID extends Serializable> implements Serializable {
    public ID id;
    public Long created;
    public Long updated;

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public List items;

    public int errorCode;
    public String errorMessage;
}
