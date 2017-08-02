package vn.vnpt.ssdc.subscriberdata.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.vnpt.ssdc.datamodel.parser.FileTypeNotSupportedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huy Hieu on 11/24/2016.
 */
@Component
public class SubscriberDataParserFactory {

    private List<FileParser> parsers = new ArrayList<FileParser>();

    @Autowired
    public SubscriberDataParserFactory(CsvSubscriberDataParser csvSubscriberDataParser) {
        parsers.add(csvSubscriberDataParser);
    }

    public FileParser getParser(String fileName) throws FileTypeNotSupportedException {
        FileParser parser = null;
        for (FileParser supportedParser : parsers) {
            if (fileName.endsWith(supportedParser.fileExtension())) {
                parser = supportedParser;
                break;
            }
        }
        if (parser == null) {
            throw new FileTypeNotSupportedException("File " + fileName + " is not supported");
        }
        return parser;
    }

}
