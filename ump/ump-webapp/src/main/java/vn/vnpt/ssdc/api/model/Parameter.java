package vn.vnpt.ssdc.api.model;

import vn.vnpt.ssdc.utils.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vietnq on 11/2/16.
 */
public class Parameter {
    public String path;
    public String subPath;
    public String shortName;
    public String dataType;
    public String value;
    public String defaultValue;
    //range [1-4], list of possible values [1;2;3]
    public String rule;
    public String inputType;
    public Integer useSubscriberData; //0 or 1
    public String tr069Name;
    public String access; //ReadOnly or ReadWrite
    public String parentObject;
    public String subscriberData;
    public Boolean instance;
    public String tr069ParentObject;

    public String value() {
        return ObjectUtils.empty(value) ? (ObjectUtils.empty(defaultValue) ? "" : defaultValue) : value;
    }

    public String min() {
        String min = "";
        try {
            if(DataType.INT.name().equalsIgnoreCase(dataType)
                    || DataType.UNSIGNEDINT.name().equalsIgnoreCase(dataType)) {
                if(!ObjectUtils.empty(rule)) {
                    min = rule().split("-")[0];
                }
            }
        } catch(Exception e) {
            //nothing to do, return empty
        }
        return min;
    }

    public String max() {
        String max = "";
        try {
            if(DataType.INT.name().equalsIgnoreCase(dataType)
                    || DataType.UNSIGNEDINT.name().equalsIgnoreCase(dataType)) {
                if(!ObjectUtils.empty(rule)) {
                    max = rule().split("-")[1];
                }
            }
        } catch(Exception e) {
            //nothing to do, return empty
        }
        return max;
    }

    public List<String> possibleValues() {
        List<String> res = new ArrayList<String>();
        String values = rule();
        if(!ObjectUtils.empty(values)) {
            res = Arrays.asList(values.split(";"));
        }
        return res;
    }

    private String rule() {
        String res = "";
        if(!ObjectUtils.empty(rule))
        {
            res = rule.replaceAll("\\[","");
            res = res.replaceAll("\\]","");
        }
        return res;
    }
}
