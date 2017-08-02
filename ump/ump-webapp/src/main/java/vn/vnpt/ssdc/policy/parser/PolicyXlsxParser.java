package vn.vnpt.ssdc.policy.parser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.vnpt.ssdc.umpexception.PolicyFileFormatException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
public class PolicyXlsxParser implements PolicyParser {
    private static final Logger logger = LoggerFactory.getLogger(PolicyXlsxParser.class);
    private static final Character DELIMITER = ',';

    @Override
    public String fileExtension() {
        return ".xlsx";
    }

    @Override
    public List<String> parse(InputStream excelFile) {
        Set<String> set = new TreeSet<String>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator iterator = sheet.rowIterator();
            if (iterator.hasNext()) {
                iterator.next();
            }
            while (iterator.hasNext()) {
                Row row = (Row) iterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();

                List<String> cellStrings = new ArrayList<String>();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    cellStrings.add(cell.toString());
                }

                String line = String.join("-", cellStrings);
                if(!"".equals(line)) {
                    String[] subLine = line.split("-");
                    if (subLine.length == 3) {
                        set.add(line);
                    } else {
                        throw new PolicyFileFormatException("File format error");
                    }
                }
            }
            excelFile.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<String>(set);
    }

}
