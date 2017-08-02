package vn.vnpt.ssdc.models;

import vn.vnpt.ssdc.api.model.SsdcEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lamborgini on 3/10/2017.
 */
public class DataModel extends SsdcEntity<Long> {

    public Map<String, String> parameters = new HashMap<>();
}
