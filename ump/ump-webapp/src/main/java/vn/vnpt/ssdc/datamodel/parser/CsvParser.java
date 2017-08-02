package vn.vnpt.ssdc.datamodel.parser;

import au.com.bytecode.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.vnpt.ssdc.api.model.DataType;
import vn.vnpt.ssdc.api.model.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vietnq on 11/6/16.
 */
@Component
public class CsvParser implements DataModelParser {
    private static final Logger logger = LoggerFactory.getLogger(CsvParser.class);
    private static final Character DELIMITER = ',';

    @Override
    public String fileExtension() {
        return ".csv";
    }

    @Override
    public Map<String, Parameter> parse(InputStream inputStream) {
        Map<String,Parameter> map = new HashMap<String,Parameter>();
        try {
            CSVReader reader = new CSVReader(
                    new BufferedReader(new InputStreamReader(inputStream,"UTF-8")),
                    DELIMITER,'"',0);
            List<String[]> rows = reader.readAll();
            for(String[] row : rows) {
                Parameter parameter = fromRow(row);
                map.put(parameter.path, parameter);
            }
            reader.close();
        } catch (UnsupportedEncodingException e) {
            logger.error("exception when parsing datamodel",e);
        } catch (IOException e) {
            logger.error("exception when parsing datamodel",e);
        }
        return map;
    }
    private Parameter fromRow(String[] row) {
        Parameter parameter = new Parameter();
        for(int i = 0; i < row.length; i++) {
            String value = row[i];
            switch (i) {
                case 0:
                    parameter.path = value;
                    break;
                case 1:
                    parameter.dataType = value;
                    break;
                case 2:
                    parameter.defaultValue = value;
                    break;
                case 3:
                    parameter.rule = value;
                    break;
                default: break;
            }
        }
        if(parameter.path.endsWith(".")) {
            parameter.dataType = DataType.OBJECT.name().toLowerCase();
        }
        return parameter;
    }
}
