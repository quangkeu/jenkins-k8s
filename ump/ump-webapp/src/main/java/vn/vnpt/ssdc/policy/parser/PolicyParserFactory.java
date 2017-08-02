package vn.vnpt.ssdc.policy.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PolicyParserFactory {

    private List<PolicyParser> parsers = new ArrayList<PolicyParser>();

    @Autowired
    public PolicyParserFactory(PolicyTxtParser txtParser, PolicyXlsxParser xlsxParser) {
        this.parsers.add(txtParser);
        this.parsers.add(xlsxParser);
    }

    public PolicyParser getParser(String fileName) throws FileTypeNotSupportedException {
        PolicyParser parser = null;
        for(PolicyParser supportedParser : this.parsers) {
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
