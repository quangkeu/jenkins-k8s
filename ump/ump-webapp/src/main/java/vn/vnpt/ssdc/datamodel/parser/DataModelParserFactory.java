package vn.vnpt.ssdc.datamodel.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vietnq on 11/6/16.
 */
@Component
public class DataModelParserFactory {

    private List<DataModelParser> parsers = new ArrayList<DataModelParser>();

    @Autowired
    public DataModelParserFactory(CsvParser csvParser) {
        this.parsers.add(csvParser);
    }

    public DataModelParser getParser(String fileName) throws FileTypeNotSupportedException {
        DataModelParser parser = null;
        for(DataModelParser supportedParser : this.parsers) {
            if(fileName.endsWith(supportedParser.fileExtension())) {
                parser = supportedParser;
                break;
            }
        }
        if(parser == null) {
            throw new FileTypeNotSupportedException("File " + fileName + " is not supported");
        }
        return parser;
    }
}
