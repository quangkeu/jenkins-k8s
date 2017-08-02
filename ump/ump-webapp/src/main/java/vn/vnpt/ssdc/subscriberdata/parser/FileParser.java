package vn.vnpt.ssdc.subscriberdata.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by Huy Hieu on 11/24/2016.
 */
public interface FileParser {
    public String fileExtension();

    public Object parse(InputStream inputStream, Long subscriberDataTemplateId) throws IOException, FileFormatIncorectException;
}
