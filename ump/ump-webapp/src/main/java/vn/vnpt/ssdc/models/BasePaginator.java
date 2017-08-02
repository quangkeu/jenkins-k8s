package vn.vnpt.ssdc.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpt.ssdc.api.client.AcsApiClient;
import vn.vnpt.ssdc.api.client.DiagnosticApiClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kiendt on 2/17/2017.
 */
public class BasePaginator {
    public static final Logger logger = LoggerFactory.getLogger(DevicePaginator.class);

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String PAGE_SIZE = "limit";
    public static final String PAGE_QUERY = "query";
    public static final String PAGE_OFFSET = "offset";
    public static final String PAGE_PARAM = "parameters";
    public static final String PAGE_CURRENT = "indexPage";
    public static final String PAGE_SORT_FIELD = "sortField";
    public static final String PAGE_SORT_TYPE = "sortType";
    public static final String PAGE_MANUFACTURER = "manufacturer";
//    public static final String PAGE_MODEL_NAME = "modelName";
    public static final String PAGE_MODEL_NAME = "modelName";
    public static final String PAGE_SEARCH_ALL = "searchAll";
    public static final String PAGE_SEARCH_LABEL = "searchLabel";


    public AcsApiClient acsApiClient;
    public DiagnosticApiClient diagnosticApiClient;
    public int currentPage;
    public int total;
    public int lastPage;
    public int pageSize = DEFAULT_PAGE_SIZE;
    public Map<String, String> acsQuery = new HashMap<>();
    public Map<String, String> indexParams = new HashMap<>();

    public String sortField;
    public String sortType;

    public String description;
    public int totalSearch;

    public BasePaginator() {
        this.currentPage = 1;

    }


    public static boolean isExits(HashMap<String, String> requestParams, String key) {
        if (requestParams.containsKey(key) && requestParams.get(key) != null) {
            return true;
        }
        return false;
    }

    public void parseParam(HashMap<String, String> requestParams) {
        String queryInput = null;
        // offset value
        if (requestParams != null && !requestParams.isEmpty()) {

            if (isExits(requestParams, PAGE_CURRENT)) {
                this.currentPage = Integer.parseInt(requestParams.get(PAGE_CURRENT));
            }
            if (isExits(requestParams, PAGE_SIZE) && !("0").equals(requestParams.get(PAGE_SIZE))) {
                this.pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));
            }
            if (isExits(requestParams, PAGE_QUERY)) {
                queryInput = requestParams.get(PAGE_QUERY);

            }
            if (isExits(requestParams, PAGE_SORT_FIELD) && isExits(requestParams, PAGE_SORT_TYPE)) {
                String filedSort = requestParams.get(PAGE_SORT_FIELD);
                String typeSort = requestParams.get(PAGE_SORT_TYPE);
                String tmp = "";
                if ("sorting_desc".equalsIgnoreCase(typeSort)) {
                    tmp = "-1";
                } else if ("sorting_asc".equalsIgnoreCase(typeSort)) {
                    tmp = "1";
                }
                String querySort = "{\"" + filedSort + "\":" + tmp + "}";
                this.acsQuery.put("sort", querySort);
                this.sortField = filedSort;
                this.sortType = typeSort;
            }
        } else {
            this.sortField = "_registered";
            this.sortType = "sorting_desc";
            String tmp = "";
            if ("sorting_desc".equalsIgnoreCase(this.sortType)) {
                tmp = "-1";
            } else if ("sorting_asc".equalsIgnoreCase(this.sortType)) {
                tmp = "1";
            }
            String querySort = "{\"" + this.sortField + "\":" + tmp + "}";
            this.acsQuery.put("sort", querySort);
        }
        this.acsQuery.put(PAGE_PARAM, String.join(",", this.indexParams.keySet()));
        this.acsQuery.put(PAGE_SIZE, String.valueOf(this.pageSize));

        if (queryInput != null) {
            this.acsQuery.put(PAGE_QUERY, queryInput);
        }
        int offset = (this.currentPage - 1) * this.pageSize;
        this.acsQuery.put(PAGE_OFFSET, String.valueOf(offset));
        this.acsQuery.put(PAGE_CURRENT, String.valueOf(currentPage -1));
        if (requestParams.containsKey("group")) {
            this.acsQuery.put("query", String.format("{\"_groups\":\"%s\"}", requestParams.get("group")));
        }


    }

    public String sortTypeParam(String sortType, String sortField) {
        String tmp = "";
        if ("sorting_desc".equalsIgnoreCase(sortType)) {
            tmp = "1";
        } else if ("sorting_asc".equalsIgnoreCase(sortType)) {
            tmp = "-1";
        } else {
            tmp = "1";
        }
        this.sortType = sortType;
        String querySort = "{\"" + sortField + "\":" + tmp + "}";
        this.sortField = sortField;
        return querySort;

    }

}
