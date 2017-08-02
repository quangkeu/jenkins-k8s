package vn.vnpt.ssdc.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import vn.vnpt.ssdc.api.client.AcsApiClient;
import vn.vnpt.ssdc.api.client.DeviceTypeClient;
import vn.vnpt.ssdc.api.client.DeviceTypeVersionClient;
import vn.vnpt.ssdc.api.model.DeviceType;
import vn.vnpt.ssdc.api.model.DeviceTypeVersion;
import vn.vnpt.ssdc.utils.ObjectUtils;
import vn.vnpt.ssdc.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Lamborgini on 2/16/2017.
 */
public class DataModelPaginator extends Paginator<DataModel> {

    public static final Logger logger = LoggerFactory.getLogger(DataModelPaginator.class);

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String PAGE_SIZE = "limit";
    public static final String PAGE_QUERY = "query";
    public static final String PAGE_OFFSET = "offset";
    public static final String PAGE_PARAM = "parameters";
    public static final String PAGE_CURRENT = "indexPage";
    public static final String PAGE_SORT_FIELD = "sortField";
    public static final String PAGE_SORT_TYPE = "sortType";
    public static final String PAGE_MANUFACTURER = "manufacturer";
    public static final String PAGE_MODEL_NAME = "modelName";


    public int currentPage;
    public int lastPage;
    public int pageSize = DEFAULT_PAGE_SIZE;
    public Map<String, String> dataModelQuery = new HashMap<String, String>();
    public Map<String, String> indexParams = new HashMap<String, String>();

    public String sortField;
    public String sortType;
    public String manufacturer;
    public String modelName;
    public String searchManufacturerDataOld;
    public String searchmodelNameDataOld;

    public String description;
    public List<DataModel> dataModels;


    public DataModelPaginator() {
        this.currentPage = 0;
        this.searchManufacturerDataOld = "All";
        this.searchmodelNameDataOld = "All";
    }

    public List<DeviceTypeVersion> deviceTypeVersionList = new ArrayList<DeviceTypeVersion>();
    public List<DeviceType> deviceTypeList = new ArrayList<DeviceType>();

    public DeviceTypeClient deviceTypeClient;
    public DeviceTypeVersionClient deviceTypeVersionClient;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    public void loadResult(HashMap<String, String> requestParams) {
//        if (requestParams.size() == 0) {

        if (requestParams.size() == 0) {
            // load all
            DeviceTypeVersion[] deviceTypeVersions = deviceTypeVersionClient.findInfoDevices(dataModelQuery, "");
            for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions) {
                deviceTypeVersion.setCreateDate(sdf.format(new Date(deviceTypeVersion.created)));
                deviceTypeVersion.setExportUrl("/data-models/export/" + deviceTypeVersion.getId());
                deviceTypeVersionList.add(deviceTypeVersion);
                deviceTypeVersion.setExportJsonUrl("/data-models/exportJson/"+deviceTypeVersion.getId());

            }
            this.totalPages = deviceTypeVersionClient.findAll().length;


        } else {
            //search + sort
            DeviceTypeVersion[] deviceTypeVersions = deviceTypeVersionClient.getDeviceTypeIDForSortAndSearch(dataModelQuery);
            for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions) {
                deviceTypeVersion.setCreateDate(sdf.format(new Date(deviceTypeVersion.created)));
                deviceTypeVersion.setExportUrl("/data-models/export/" + deviceTypeVersion.getId());
                deviceTypeVersionList.add(deviceTypeVersion);
            }
            this.totalPages = deviceTypeVersionClient.countDeviceTypeIDForSortAndSearch(dataModelQuery);
            if (requestParams.get(PAGE_MANUFACTURER) != null) {
                this.searchManufacturerDataOld = requestParams.get(PAGE_MANUFACTURER);
            }
            if (requestParams.get(PAGE_MODEL_NAME) != null) {
                this.searchmodelNameDataOld = requestParams.get(PAGE_MODEL_NAME);
            }

        }

        if (this.totalPages == 0) {
            this.lastPage = 1;
        } else {
            int page = this.totalPages % (requestParams.size() == 0 ? DEFAULT_PAGE_SIZE
                    : Integer.valueOf(requestParams.get(PAGE_SIZE)));
            int pageTotal = this.totalPages / (requestParams.size() == 0 ? DEFAULT_PAGE_SIZE
                    : Integer.valueOf(requestParams.get(PAGE_SIZE)));
            this.lastPage = page == 0 ? pageTotal : pageTotal + 1;
        }

    }

    public void parseParam(HashMap<String, String> requestParams) {
        String queryInput = null;
        this.number++;
        if (requestParams != null && !requestParams.isEmpty()) {

            if (requestParams.containsKey(PAGE_CURRENT) && requestParams.get(PAGE_CURRENT) != null) {
                this.currentPage = Integer.parseInt(requestParams.get(PAGE_CURRENT));
                this.number = Integer.parseInt(requestParams.get(PAGE_CURRENT));
            }
            if (requestParams.containsKey(PAGE_SIZE) && requestParams.get(PAGE_SIZE) != null) {
                if (!requestParams.get(PAGE_SIZE).toString().equals("0"))
                    this.pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));
            }
            if (requestParams.containsKey(PAGE_QUERY) && requestParams.get(PAGE_QUERY) != null) {
                queryInput = requestParams.get(PAGE_QUERY).toString();

            }
            if (requestParams.containsKey(PAGE_SORT_FIELD) && requestParams.get(PAGE_SORT_FIELD) != null) {
                if (requestParams.containsKey(PAGE_SORT_TYPE) && requestParams.get(PAGE_SORT_TYPE) != null) {
                    String filedSort = requestParams.get(PAGE_SORT_FIELD).toString();
                    String typeSort = requestParams.get(PAGE_SORT_TYPE).toString();
                    if (!filedSort.equals("") && !typeSort.equals("")) {
                        String tmp = "";
                        if (typeSort.equalsIgnoreCase("sorting_desc")) {
                            tmp = "-1";
                        } else if (typeSort.equalsIgnoreCase("sorting_asc")) {
                            tmp = "1";
                        }
                        String querySort = filedSort + ":" + tmp;
                        this.dataModelQuery.put("sort", querySort);
                        this.sortField = filedSort;
                        this.sortType = typeSort;
                    } else {
                        this.sortField = "_registered";
                        this.sortType = "sorting_disable";
                        String tmp = "";
                        if (this.sortType.equalsIgnoreCase("sorting_desc")) {
                            tmp = "-1";
                        } else if (this.sortType.equalsIgnoreCase("sorting_asc")) {
                            tmp = "1";
                        } else {
                            tmp = "1";
                        }
                        String querySort = this.sortField + ":" + tmp;
                        this.dataModelQuery.put("sort", querySort);
                    }

                }
            } else {
                this.sortField = "created";
                this.sortType = "sorting_disable";
                String tmp = "";
                if (this.sortType.equalsIgnoreCase("sorting_desc")) {
                    tmp = "-1";
                } else if (this.sortType.equalsIgnoreCase("sorting_asc")) {
                    tmp = "1";
                } else {
                    tmp = "1";
                }
                String querySort = this.sortField + ":" + tmp;
                this.dataModelQuery.put("sort", querySort);
            }
        } else {
            this.sortField = "created";
            this.sortType = "sorting_disable";
            String tmp = "";
            if (this.sortType.equalsIgnoreCase("sorting_desc")) {
                tmp = "-1";
            } else if (this.sortType.equalsIgnoreCase("sorting_asc")) {
                tmp = "1";
            } else {
                tmp = "1";
            }
            String querySort = this.sortField + ":" + tmp;
            this.dataModelQuery.put("sort", querySort);
        }
        this.dataModelQuery.put(PAGE_PARAM, String.join(",", this.indexParams.keySet()));
        this.dataModelQuery.put(PAGE_SIZE, String.valueOf(this.pageSize));
        if (queryInput != null) {
            this.dataModelQuery.put(PAGE_QUERY, queryInput);
        }
        int offset = (this.currentPage - 1) * this.pageSize;
        this.dataModelQuery.put(PAGE_OFFSET, String.valueOf(offset));
        this.dataModelQuery.put(PAGE_CURRENT, currentPage != 0 ? String.valueOf(currentPage - 1) : "0");
        if (requestParams.containsKey("group")) {
            this.dataModelQuery.put("query", String.format("{\"_groups\":\"%s\"}", requestParams.get("group")));
        }

        this.modelName = requestParams.get(PAGE_MODEL_NAME);
        this.manufacturer = requestParams.get(PAGE_MANUFACTURER);
        this.dataModelQuery.put(PAGE_MANUFACTURER, this.manufacturer != null ? this.manufacturer : "All");
        this.dataModelQuery.put(PAGE_MODEL_NAME, this.modelName != null ? this.modelName : "All");
    }

    public Map<String, String> parseParamDelete(String params) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(PAGE_SIZE, String.valueOf(this.pageSize));
        queryParams.put(PAGE_OFFSET, "0");
        queryParams.put(PAGE_PARAM, String.join(",", "_deviceId._OUI", "_deviceId._ProductClass", "InternetGatewayDevice.SoftwareVersion"));
        queryParams.put(PAGE_QUERY, StringUtils.queryDeleteDM(params));
        return queryParams;

    }

    public void parseParamAfterDelete() {
        this.sortType = "sorting_disable";
    }
}
