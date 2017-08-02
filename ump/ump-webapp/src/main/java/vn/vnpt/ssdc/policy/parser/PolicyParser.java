package vn.vnpt.ssdc.policy.parser;

import java.io.InputStream;
import java.util.List;

public interface PolicyParser {
    public String fileExtension();

    public List<String> parse(InputStream inputStream);
}
