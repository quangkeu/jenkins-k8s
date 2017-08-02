package vn.vnpt.ssdc.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpt.ssdc.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vietnq on 11/12/16.
 */
public class UtilsTest {
    private static Logger logger = LoggerFactory.getLogger(UtilsTest.class);

    @Test
    public void testQuery() {
        Map<String,String> map = new HashMap<String,String>(){{
            put("a.b.c","test1");
            put("d.e.f","test2");
        }};
        logger.info("Query: {}", StringUtils.advancedQuery(map));
    }
}
