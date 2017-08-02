package vn.vnpt.ssdc.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.*;
import vn.vnpt.ssdc.api.model.BlacklistDevice;
import vn.vnpt.ssdc.models.DataModelPaginator;
import vn.vnpt.ssdc.models.DevicePaginator;
import vn.vnpt.ssdc.utils.StringUtils;

import java.util.*;

/**
 * Created by vietnq on 10/20/16.
 */
@Controller
public class HomeController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(HomeController.class);


    @Autowired
    private AcsApiClient acsApiClient;

    @Autowired
    private BlackListDeviceClient blackListDeviceClient;

    @Autowired
    private DeviceGroupClient deviceGroupClient;

    @GetMapping("/")
    @PreAuthorize("hasAuthority('API:ACS:READ-LIST-DEVICES')")
    public String index(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to list devices page","","");
        session.removeAttribute("listComboSearch");
        // get inform from input
        Map<String, String> deviceIndexParams = deviceIndexParams();
        DevicePaginator devicePaginator = new DevicePaginator();
        //set fields for device paginator //TODO constructor
        devicePaginator.acsApiClient = acsApiClient;
        devicePaginator.indexParams = deviceIndexParams;
        devicePaginator.parseParam((HashMap<String, String>) requestParams);
        devicePaginator.loadResult(requestParams);
        //add render args
        model.addAttribute("paginator", devicePaginator);

        loadDataSearch(model, devicePaginator, requestParams);

        return "index";
    }

    private void loadDataSearch(Model model, DevicePaginator devicePaginator, Map<String, String> requestParams) {

        List<String> manufacturerlist = new ArrayList<String>();
        List<String> modelNamelist = new ArrayList<String>();
        List<String> firmwareVersionlist = new ArrayList<String>();
        Object listComboSearch = session.getAttribute("listComboSearch");
        JsonObject response = new JsonObject();
        response = checkListComboSearchDevice(listComboSearch);

        if (requestParams.size() > 0) {
            if (requestParams.get(DevicePaginator.PAGE_MANUFACTURER) != null
                    && !requestParams.get(DevicePaginator.PAGE_MANUFACTURER).equals("")) {
                devicePaginator.searchManufacturerDataOld = requestParams.get(DevicePaginator.PAGE_MANUFACTURER);
            }
            if (requestParams.get(DevicePaginator.PAGE_MODEL_NAME) != null
                    && !requestParams.get(DevicePaginator.PAGE_MODEL_NAME).equals("")) {
                devicePaginator.searchModelNameDataOld = requestParams.get(DevicePaginator.PAGE_MODEL_NAME);
            }
            if (requestParams.get(DevicePaginator.PAGE_FIRMWARE_VERSION) != null
                    && !requestParams.get(DevicePaginator.PAGE_FIRMWARE_VERSION).equals("")) {
                devicePaginator.searchFirmwareVersionDataOld = requestParams.get(DevicePaginator.PAGE_FIRMWARE_VERSION);
            }
            if (requestParams.get(DevicePaginator.PAGE_SEARCH_LABEL) != null
                    && !requestParams.get(DevicePaginator.PAGE_SEARCH_LABEL).equals("")) {
                devicePaginator.searchLabelDataOld = requestParams.get(DevicePaginator.PAGE_SEARCH_LABEL);
            }
            devicePaginator.searchAllDataOld = "";
            if (requestParams.get(DevicePaginator.PAGE_SEARCH_ALL) != null ) {
                devicePaginator.searchAllDataOld = requestParams.get(DevicePaginator.PAGE_SEARCH_ALL);
                devicePaginator.searchManufacturerDataOld = "";
                devicePaginator.searchFirmwareVersionDataOld = "";
                devicePaginator.searchModelNameDataOld = "";
                devicePaginator.searchLabelDataOld = "";
            }


        }
        if (response.size() > 0) {
            for (Map.Entry<String, JsonElement> entry : response.entrySet()) {
                manufacturerlist.add(entry.getKey());
                JsonArray asJsonArray = response.get(entry.getKey()).getAsJsonArray();
                for (int i = 0; i < asJsonArray.size(); i++) {
                    JsonElement jsonElement = asJsonArray.get(i);
                    JsonObject asJsonObjectModelName = jsonElement.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry1 : asJsonObjectModelName.entrySet()) {
                        if (devicePaginator.searchManufacturerDataOld.equals(entry.getKey())) {
                            modelNamelist.add(entry1.getKey());
                        }
                    }
                    JsonArray asJsonArray1 = asJsonObjectModelName.getAsJsonArray(devicePaginator.searchModelNameDataOld);
                    if (asJsonArray1 != null && asJsonArray1.size() > 0) {
                        for (int j = 0; j < asJsonArray1.size(); j++) {
                            firmwareVersionlist.add(asJsonArray1.get(j).getAsString());
                        }
                    }
                }
            }
        }

        model.addAttribute("manufacturerlist", manufacturerlist);
        model.addAttribute("modelNamelist", modelNamelist);
        model.addAttribute("firmwareVersionList", firmwareVersionlist);
        model.addAttribute("searchAllDataOld", devicePaginator.searchAllDataOld);
        model.addAttribute("searchLabelDataOld", devicePaginator.searchLabelDataOld);
        model.addAttribute("manufacturerDataOld", devicePaginator.searchManufacturerDataOld);
        model.addAttribute("modelNameDataOld", devicePaginator.searchModelNameDataOld);
        model.addAttribute("firmwareVersionDataOld", devicePaginator.searchFirmwareVersionDataOld);
        model.addAttribute("itemPerPage", requestParams.get("limit"));

    }

    private Map<String, String> deviceComboDataIndexParams() {
        return new LinkedHashMap<String, String>() {{
            // Infor. map voi key trong file message
            put(MANUFACTURER, "Infor.Manufacturer");
            put(MODEL_NAME, "Infor.ModelName");
            put(FIRMWARE_VERSION, "Infor.FirmwareVersion");
        }};
    }

    @GetMapping("/device-type/get-list")
    @ResponseBody
    public HashMap<String, Object> getListDeviceType() {
        HashMap<String, Object> response = new HashMap<>();
        response.put("deviceType", deviceTypeClient.findAll());
        response.put("deviceTypeVersion", deviceTypeVersionClient.findAll());
        return response;
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('API:ACS:READ-LIST-DEVICES')")
    public String search(Model model, @RequestParam Map<String, String> searchParams) {
        Map<String, String> deviceIndexParams = deviceIndexParams();
        Map<String, String> acsQuery = new HashMap<>();

        DevicePaginator devicePaginator = new DevicePaginator();
        //set fields for device paginator //TODO constructor
        devicePaginator.acsApiClient = acsApiClient;
        devicePaginator.indexParams = deviceIndexParams;

        if (StringUtils.checkParameterSearch(searchParams)) {
            if (searchParams.get(DevicePaginator.PAGE_SEARCH_ALL) != null) {
                acsQuery.put("parameters", String.join(",", deviceIndexParams.keySet()));
                acsQuery.put("query", StringUtils.queryDeviceSearchFull(searchParams, deviceIndexParamsSearch()));
            } else {
                acsQuery.put("parameters", String.join(",", deviceIndexParams.keySet()));
                acsQuery.put("query", buildMongoQuery(searchParams.get(DevicePaginator.PAGE_MANUFACTURER),searchParams.get(DevicePaginator.PAGE_MODEL_NAME),
                        searchParams.get(DevicePaginator.PAGE_FIRMWARE_VERSION),searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL)));
            }

            if(searchParams.get(DevicePaginator.PAGE_SORT_FIELD) != null
                    && !searchParams.get(DevicePaginator.PAGE_SORT_FIELD).isEmpty()){
                String sortTypeParam = devicePaginator.sortTypeParam(searchParams.get(DevicePaginator.PAGE_SORT_TYPE),
                        searchParams.get(DevicePaginator.PAGE_SORT_FIELD));
                acsQuery.put("sort", sortTypeParam);
            } else {
                devicePaginator.sortField = "_registered";
                devicePaginator.sortType = "sorting sorting_disabled";
            }

            acsQuery.put("limit", searchParams.get(DevicePaginator.PAGE_SIZE));

            int offset = (Integer.valueOf(searchParams.get(DevicePaginator.PAGE_CURRENT)) - 1)
                    * Integer.valueOf(searchParams.get(DevicePaginator.PAGE_SIZE));

            acsQuery.put("offset", String.valueOf(offset));
            devicePaginator.acsQuery = acsQuery;
            devicePaginator.number = Integer.parseInt(searchParams.get(DataModelPaginator.PAGE_CURRENT));
        } else {
            devicePaginator.parseParam((HashMap<String, String>) searchParams);
        }
        devicePaginator.loadResult(searchParams);
        model.addAttribute("paginator", devicePaginator);

        loadDataSearch(model, devicePaginator, searchParams);
        return "index";
    }

    @PostMapping("/deleteDevices")
    @PreAuthorize("hasAuthority('API:ACS:DELETE-DEVICE')")
    @ResponseBody
    public String deleteDevices(@RequestParam Map<String, String> params) {
        String result="";
        if (params.keySet().contains("deleteIds")) {
            String[] paramDevice = params.get("deleteIds").substring(0, params.get("deleteIds").length() - 1).split(",");
            if (paramDevice.length > 0) {
                for (int i = 0; i < paramDevice.length; i++) {
                    acsApiClient.deleteDevice(paramDevice[i]);
                    if (params.keySet().contains("mode") && !params.get("mode").equals("temporarily")) {
                        BlacklistDevice blacklistDevice = new BlacklistDevice();
                        blacklistDevice.setDeviceID(paramDevice[i]);
                        blackListDeviceClient.create(blacklistDevice);
                    }
                }
                session.removeAttribute("manufacturerlist");
                session.removeAttribute("modelNamelist");
                session.removeAttribute("firmwareVersion");

            }
        }
        return result;
    }

    public String buildMongoQuery(String manufacturer, String modelName, String firmwareVersion, String labelQuery){
        String result = deviceGroupClient.buildMongoQuery(manufacturer,modelName,firmwareVersion,labelQuery);
        return result;
    }
}
