package vn.vnpt.ssdc.datamodel.parser;

import vn.vnpt.ssdc.api.model.Parameter;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by vietnq on 11/6/16.
 */
public interface DataModelParser {
    public String fileExtension();
    public Map<String,Parameter> parse(InputStream inputStream);
}
