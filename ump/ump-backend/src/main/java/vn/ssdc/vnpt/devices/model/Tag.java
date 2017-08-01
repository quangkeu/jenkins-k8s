package vn.ssdc.vnpt.devices.model;

import vn.vnpt.ssdc.jdbc.SsdcEntity;
import vn.vnpt.ssdc.jdbc.annotations.Serialized;

import java.util.Map;

/**
 * A tag is a collection of parameters <br/>
 * It is assigned to device type for dynamic configuration </br>
 * For example: VoIP tag, WAN tag, Device summary tag, ....
 *
 * Created by vietnq on 11/1/16.
 */
public class Tag extends SsdcEntity<Long>{
    public static final String OBJECT = "OBJECT";
    public static final String STATUS = "STATUS";
    public static final String PARAMS = "PARAMS";

    public String name;
    @Serialized
    public Map<String,Parameter> parameters;
    public String type;
    public Long deviceTypeId;

    //0 : unassigned, 1 : assigned, use integer for cross-platform db
    public Integer assigned;

}
