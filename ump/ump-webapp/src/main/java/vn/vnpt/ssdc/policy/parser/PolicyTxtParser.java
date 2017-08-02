package vn.vnpt.ssdc.policy.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.vnpt.ssdc.umpexception.PolicyFileFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
public class PolicyTxtParser implements PolicyParser {
    private static final Logger logger = LoggerFactory.getLogger(PolicyTxtParser.class);
    private static final Character DELIMITER = ',';

    @Override
    public String fileExtension() {
        return ".txt";
    }

    @Override
    public List<String> parse(InputStream inputStream) {
        Set<String> set = new TreeSet<String>();
        try {
            LineNumberReader br = new LineNumberReader(new InputStreamReader(inputStream));

            String line;
            while ((line = br.readLine()) != null) {
                if (br.getLineNumber() == 1) {
                    continue;
                }
                line = line.replaceAll("[\t]", "-");
                if (!"".equals(line)) {
                    String[] subLine = line.split("-");
                    if (subLine.length == 3) {
                        set.add(line);
                    } else {
                        throw new PolicyFileFormatException("File format error");
                    }
                }
            }
            br.close();

        } catch (IOException e) {
            logger.error("Exception when parsing Policy txt", e);
        }
        return new ArrayList<String>(set);
    }

}
