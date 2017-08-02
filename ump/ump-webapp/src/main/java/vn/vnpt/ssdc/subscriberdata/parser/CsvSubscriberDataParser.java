package vn.vnpt.ssdc.subscriberdata.parser;

import au.com.bytecode.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.vnpt.ssdc.api.client.SubscriberTemplateClient;
import vn.vnpt.ssdc.api.model.Subscriber;
import vn.vnpt.ssdc.api.model.SubscriberTemplate;
import vn.vnpt.ssdc.datamodel.parser.CsvParser;
import vn.vnpt.ssdc.utils.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Huy Hieu on 11/24/2016.
 */
@Component
public class CsvSubscriberDataParser implements FileParser {
    private static final Logger logger = LoggerFactory.getLogger(CsvParser.class);
    private static final Character DELIMITER = ',';

    @Autowired
    private SubscriberTemplateClient subscriberTemplateClient;

    @Override
    public String fileExtension() {
        return ".csv";
    }

    @Override
    public List<Subscriber> parse(InputStream inputStream, Long subscriberDataTemplateId) throws IOException, FileFormatIncorectException {

        List<Subscriber> subscribers = new ArrayList<Subscriber>();
        int skipLine = 0;
        CSVReader reader = new CSVReader(
                new BufferedReader(new InputStreamReader(inputStream, "UTF-8")),
                DELIMITER, '"', skipLine);
        List<String[]> rows = reader.readAll();

        //Get header and push to array
        String[] rowHeader = rows.get(0);

        if (validate(subscriberDataTemplateId, rowHeader)) {
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                Subscriber subscriber = initSubscriber();
                subscriber.subscriberId = row[0];

                // Add to data columns to subscriberData
                for (int columns = 1; columns < rowHeader.length; columns++) {
                    subscriber.subscriberData.put(rowHeader[columns], row[columns]);
                }
                subscribers.add(subscriber);
            }
        } else {
            reader.close();
            throw new FileFormatIncorectException("File format is incorrect");
        }

        reader.close();
        return subscribers;
    }

    private Subscriber initSubscriber() {
        Subscriber subscriber = new Subscriber();
        subscriber.subscriberData = new HashMap<>();
        return subscriber;
    }

    private boolean validate(Long subscriberDataTemplateId, String[] rowHeader) {

        HashSet<String> templateKeysImport = new HashSet<String>();
        templateKeysImport.addAll(Arrays.asList(rowHeader).subList(1, rowHeader.length));

        SubscriberTemplate subscriberTemplate = subscriberTemplateClient.get(subscriberDataTemplateId);
        if(!ObjectUtils.empty(subscriberTemplate)) {
            if (templateKeysImport.containsAll(subscriberTemplate.templateKeys) && subscriberTemplate.templateKeys.containsAll(templateKeysImport)) {
                return true;
            }
        }
        return false;
    }
}
