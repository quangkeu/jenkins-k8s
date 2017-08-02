package vn.vnpt.ssdc.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vnpt.ssdc.api.client.DeviceGroupClient;
import vn.vnpt.ssdc.api.client.DeviceTypeClient;
import vn.vnpt.ssdc.api.client.PerformanceSettingApiClient;
import vn.vnpt.ssdc.api.model.*;
import vn.vnpt.ssdc.models.Device;
import vn.vnpt.ssdc.models.PerformanceSettingPaginator;
import vn.vnpt.ssdc.policy.parser.FileTypeNotSupportedException;
import vn.vnpt.ssdc.policy.parser.PolicyParser;
import vn.vnpt.ssdc.policy.parser.PolicyParserFactory;
import vn.vnpt.ssdc.utils.StringUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by thangnc on 20-Jun-17.
 */
@Controller
public class PerformanceController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(PerformanceController.class);
    private static final String PARAM_CPU = "InternetGatewayDevice.CpuRam.CpuUsed";
    private static final String PARAM_RAM = "InternetGatewayDevice.CpuRam.RamUsedPer";
    private static final String PARAM_LAN = "LANDevice";
    private static final String PARAM_WAN = "WANDevice";
    private static final String PARAM_WLAN = "WlanAdapter";
    private static final String PARAM_BYTES_RECEIVED = "BytesReceived";
    private static final String PARAM_PACKETS_RECEIVED = "PacketsReceived";
    private static final String PARAM_X_BROADCOM_COM_RXERRORS = "X_BROADCOM_COM_RxErrors";
    private static final String PARAM_X_BROADCOM_COM_RXDROPS = "X_BROADCOM_COM_RxDrops";
    private static final String PARAM_BYTES_SENT = "BytesSent";
    private static final String PARAM_PACKETSSENT = "PacketsSent";
    private static final String PARAM_X_BROADCOM_COM_TXRERRORS = "X_BROADCOM_COM_TxErrors";
    private static final String PARAM_X_BROADCOM_COM_TXDROPS = "X_BROADCOM_COM_TxDrops";
    private static final String PARAM_X_BROADCOM_COM_IFNAME = "X_BROADCOM_COM_IfName";
    private static final String PARAM_X_BROADCOM_COM_WIFINAME = "WlIfcname";

    @Autowired
    DeviceGroupClient deviceGroupClient;

    @Autowired
    PerformanceSettingApiClient performanceSettingApiClient;

    @Autowired
    DeviceTypeClient deviceTypeClient;

    @Autowired
    PolicyParserFactory policyParserFactory;

    @Autowired
    private ServletContext context;

    @GetMapping("/performance-setting")
    public String performance(Model model, @RequestParam Map<String, String> requestParams) throws UnsupportedEncodingException {
        PerformanceSettingPaginator performaceSettingPaginator = new PerformanceSettingPaginator();
        performaceSettingPaginator.performanceSettingApiClient = performanceSettingApiClient;
        performaceSettingPaginator.indexParams = performanceSettingIndexParams();
        performaceSettingPaginator.parseParam((HashMap<String, String>) requestParams);
        performaceSettingPaginator.loadResult(requestParams);

        DeviceGroup[] deviceGroups = deviceGroupClient.findAll();
        loadDataSearch(model);
        model.addAttribute("search", false);
        model.addAttribute("deviceGroups", deviceGroups);
        model.addAttribute("paginator", performaceSettingPaginator);
        return "performance/performance";
    }

    @GetMapping("/performance-setting/search")
    public String search(Model model, @RequestParam Map<String, String> requestParams) throws UnsupportedEncodingException {
        PerformanceSettingPaginator performanceSettingPaginator = new PerformanceSettingPaginator();
        performanceSettingPaginator.performanceSettingApiClient = performanceSettingApiClient;
        performanceSettingPaginator.indexParams = performanceSettingIndexParams();
        performanceSettingPaginator.loadResultSearch(requestParams);

        DeviceGroup[] deviceGroups = deviceGroupClient.findAll();
        model.addAttribute("search", true);
        model.addAttribute("deviceGroups", deviceGroups);
        model.addAttribute("paginator", performanceSettingPaginator);
        return "performance/performance";
    }

    private void loadDataSearch(Model model) {
        List<String> manufacturerlist = new ArrayList<String>();
        JsonObject response = checkListComboSearchDevice(null);
        if (response.size() > 0) {
            for (Map.Entry<String, JsonElement> entry : response.entrySet()) {
                manufacturerlist.add(entry.getKey());
            }
        }
        model.addAttribute("manufacturerlist", manufacturerlist);
        model.addAttribute("addNewFileParam", response.toString());
    }

    @PostMapping("/performance-setting/getListSerialNumber")
    public
    @ResponseBody
    Map<String, String> getListSerialNumber(@RequestParam Map<String, String> searchParams) throws UnsupportedEncodingException {
        Map<String, String> acsQuery = new HashMap<>();
        Map<String, String> result = new LinkedHashMap<>();
        acsQuery.put("parameters", String.join(",", deviceIndexParams().keySet()));
        acsQuery.put("query", StringUtils.queryDeviceSearchObject(searchParams, deviceIndexParamsReverse()));
        AcsResponse response = acsApiClient.findDevicesOld(acsQuery);
        List<Device> devices = Device.fromJsonString(response.body, deviceIndexParams().keySet());
        for (int i = 0; i < devices.size(); i++) {
            result.put(devices.get(i).serialNumber(), String.valueOf(200));
        }
        return result;
    }

    @PostMapping("/performance-setting/save")
    public String save(@RequestParam Map<String, String> params, @RequestParam() MultipartFile externalFile) {
        savePerformanceSetting(params, externalFile, true);
        return "redirect:/performance-setting";
    }

    @PostMapping("/performance-setting/edit")
    public String edit(@RequestParam Map<String, String> params, @RequestParam() MultipartFile externalFile) {
        savePerformanceSetting(params, externalFile, false);
        return "redirect:/performance-setting";
    }

    public void savePerformanceSetting(Map<String, String> params, MultipartFile externalFile, boolean type) {
        PerformanceSetting performanceSetting = new PerformanceSetting();
        String traffic = params.get("traffic");
        if (("LAN").equals(traffic) || ("WAN").equals(traffic) || ("WLAN").equals(traffic)) {
            performanceSetting.type = params.get("type");
        }

        performanceSetting.stasticsInterval = Integer.parseInt(params.get("interval"));
        performanceSetting.start = convertDatetoLong(params.get("fromDate"));
        performanceSetting.end = convertDatetoLong(params.get("toDate"));
        performanceSetting.stasticsType = traffic;
        if (params.get("group") != null && !("").equals(params.get("group"))) {
            performanceSetting.deviceGroupId = Long.parseLong(params.get("filter"));
            performanceSetting.monitoring = 2;
        }

        if (params.get("single") != null && !("").equals(params.get("single"))) {
            Map<String, String> acsQuery = new HashMap<>();
            acsQuery.put("parameters", String.join(",", deviceIndexParams().keySet()));
            acsQuery.put("query", StringUtils.queryDeviceSearchObject(params, deviceIndexParamsReverse()));
            AcsResponse response = acsApiClient.findDevices(acsQuery);
            List<Device> devices = Device.fromJsonString(response.body, deviceIndexParams().keySet());
            if (devices.size() > 0) {
                performanceSetting.deviceId = devices.get(0).id;
                performanceSetting.manufacturer = devices.get(0).parameters.get(MANUFACTURER);
                performanceSetting.modelName = devices.get(0).parameters.get(MODEL_NAME);
                performanceSetting.serialNumber = devices.get(0).parameters.get(SERIAL_NUMBER);
            }
            performanceSetting.monitoring = 1;
        }

        if (params.get("file") != null && !("").equals(params.get("file"))) {
            performanceSetting.monitoring = 3;
            if (externalFile != null && !externalFile.getOriginalFilename().isEmpty()) {
                PolicyParser parser = null;

                try {
                    parser = (PolicyParser) policyParserFactory.getParser(externalFile.getOriginalFilename());
                    performanceSetting.externalDevices = parser.parse(externalFile.getInputStream());
                    performanceSetting.external_filename = externalFile.getOriginalFilename();
                } catch (FileTypeNotSupportedException e) {
                    logger.error("save ", e);
                } catch (IOException e) {
                    logger.error("save ", e);
                }
            }
        }

        List<String> parameterNames = new LinkedList<>();
        if (performanceSetting.stasticsType.equals("RAM")) {
            parameterNames.add(RAM_STATISTIC_LIST);
        }
        if (performanceSetting.stasticsType.equals("CPU")) {
            parameterNames.add(CPU_STATISTIC_LIST);
        }

        if (("LAN").equals(performanceSetting.stasticsType) && ("RECEIVED").equals(performanceSetting.type))
            parameterNames = createParameterNames(LAN_STATISTIC_RECEIVED_LIST.split(","));
        if (("LAN").equals(performanceSetting.stasticsType) && ("TRANSMITTED").equals(performanceSetting.type))
            parameterNames = createParameterNames(LAN_STATISTIC_TRANSMITTED_LIST.split(","));
        if (("WAN").equals(performanceSetting.stasticsType) && ("RECEIVED").equals(performanceSetting.type))
            parameterNames = createParameterNames(WAN_STATISTIC_RECEIVED_LIST.split(","));
        if (("WAN").equals(performanceSetting.stasticsType) && ("TRANSMITTED").equals(performanceSetting.type))
            parameterNames = createParameterNames(WAN_STATISTIC_TRANSMITTED_LIST.split(","));
        if (("WLAN").equals(performanceSetting.stasticsType) && ("RECEIVED").equals(performanceSetting.type))
            parameterNames = createParameterNames(WLAN_STATISTIC_RECEIVED_LIST.split(","));
        if (("WLAN").equals(performanceSetting.stasticsType) && ("TRANSMITTED").equals(performanceSetting.type))
            parameterNames = createParameterNames(WLAN_STATISTIC_TRANSMITTED_LIST.split(","));

        performanceSetting.parameterNames = parameterNames;

        if (!type) {
            performanceSetting.id = Long.parseLong(params.get("performanceId"));
            performanceSettingApiClient.update(performanceSetting.id, performanceSetting);
        } else performanceSettingApiClient.create(performanceSetting);
    }

    public List<String> createParameterNames(String[] listParameters) {
        List<String> parameterNames = new LinkedList<>();
        String parameters = "";
        int listSize = listParameters.length;
        if (listSize == 10) listSize = 5;
        for (int i = 1; i < listSize; i++) {

            for (int j = 0; j < listParameters.length; j++) {
                String parameter = listParameters[j];
                parameter = parameter.replaceFirst("\\{i\\}", "1");
                parameter = parameter.replaceFirst("\\{j\\}", String.valueOf(i));
                parameters = parameters + parameter + ",";
            }

            if (!("").equals(parameters)) {
                parameters = parameters.substring(0, parameters.length() - 1);
                parameterNames.add(parameters);
                parameters = "";
            }

        }
        return parameterNames;
    }

    @PostMapping("/performance-setting/remove")
    public
    @ResponseBody
    Map<String, String> removeperformance(Model model, @RequestBody String listId) throws UnsupportedEncodingException {
        String lst = URLDecoder.decode(listId, "UTF-8");
        Map<String, String> result = new LinkedHashMap<>();
        String regex = "\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(lst);
        while (matcher.find()) {
            String performanceID = matcher.group();
            performanceID = performanceID.replaceAll("\"", "");
            try {
                performanceSettingApiClient.delete(Long.parseLong(performanceID));
                result.put(performanceID, String.valueOf(200));
            } catch (Exception ex) {
                result.put(performanceID, String.valueOf(404));
            }
        }
        return result;
    }


    public long convertDatetoLong(String day) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long milliseconds = 0;
        try {
            Date d = f.parse(day);
            milliseconds = d.getTime();
        } catch (ParseException e) {
            logger.error("convertDatetoLong ", e);
        }
        return milliseconds;
    }


    @GetMapping("/performance-setting/stb-view-detail")
    @ResponseBody
    public String stbViewDetail(@RequestParam Map<String, String> params) {

        return "[]";
    }

    @GetMapping("/performance-setting/voip-view-detail")
    @ResponseBody
    public String voipViewDetail(@RequestParam Map<String, String> params) {

        return "[]";
    }

    @PostMapping("/performance-setting/stb-service-chart")
    @ResponseBody
    public String stbServiceChart(@RequestParam Map<String, String> params) {
        String result = "";
        result = "[]|[]" + "|" + params.get("serialNumber");
        return result;
    }

    @PostMapping("/performance-setting/voip-service-chart")
    @ResponseBody
    public String voipServiceChart(@RequestParam Map<String, String> params) {
        String result = "";
        result = "[]|[]" + "|" + params.get("serialNumber");
        return result;
    }

    @GetMapping("/performance-setting/ram-cpu-view-detail")
    @ResponseBody
    public String ramCpuViewDetail(Model model, @RequestParam Map<String, String> params) {
        JsonArray list = new JsonArray();
        Device[] deviceList = null;
        if (params.get("monitoring").equals("1")) {
            // single
            deviceList = new Device[1];
            deviceList[0] = new Device();
            Map<String, String> parameters = new HashMap<>();
            parameters.put("_deviceId._Manufacturer", params.get("manufacturer"));
            parameters.put("summary.modelName", params.get("modelName"));
            parameters.put("_deviceId._SerialNumber", params.get("serialNumber"));
            AcsResponse response = acsApiClient.getDevices(params.get("serialNumber"));
            JsonArray array = new Gson().fromJson(response.body, JsonArray.class);
            JsonObject deviceObject = array.get(0).getAsJsonObject();
            deviceList[0].parameters = parameters;
            deviceList[0].id = deviceObject.get("_id").getAsString();
        } else if (params.get("monitoring").equals("2")) {
            // group
            deviceList = deviceGroupClient.getListDeviceByGroup(params.get("deviceGroupId"));
        } else {
            // file
            PerformanceSetting performanceSetting = performanceSettingApiClient.get(Long.valueOf(params.get("performanceSettingId")));
            List<String> listDeviceIds = new ArrayList<String>();
            for (String deviceId : performanceSetting.externalDevices) {
                listDeviceIds.add(String.format("{\"%s\":\"%s\"}", "_id", deviceId));
            }
            String query = String.format("{\"$or\":[%s]}", org.apache.commons.lang3.StringUtils.join(listDeviceIds, ","));
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("query", query);
            queryParams.put("projection", "_deviceId._SerialNumber");
            AcsResponse response = acsApiClient.findDevices(queryParams);
            List<Device> deviceList1 = Device.fromJsonString(response.body, deviceIndexParams().keySet());
            deviceList = deviceList1.stream().toArray(Device[]::new);
            System.out.println("lol");
        }
        list = loadDataRamCpu(params.get("performanceSettingId"), deviceList, params.get("statisticType"),
                params.get("textFromDate"), params.get("textToDate"));
        return list.toString();
    }

    @PostMapping("/performance-setting/ram-cpu-service-chart")
    @ResponseBody
    public String ramCpuServiceChart(@RequestParam Map<String, String> params) {

        String result = "";
        result = session.getAttribute("ramCpu").toString();
        return result;
    }

    @PostMapping("/performance-setting/delete-performance")
    @ResponseBody
    public boolean deleteInterfaceRamCpu(@RequestParam Map<String, String> params) {
        boolean result = false;
        if (params.size() > 0) {
            String[] paramAlarm = params.get("deleteIds")
                    .substring(0, params.get("deleteIds").length() - 1).split(",");
            String[] deleteInterface = params.get("deleteInterface")
                    .substring(0, params.get("deleteInterface").length() - 1).split(",");
            String performanceSettingId = params.get("performanceSettingId");

            for (int i = 0; i < paramAlarm.length; i++) {
                result = performanceSettingApiClient.deleteStatiticsInterface(paramAlarm[i].substring(1),
                        performanceSettingId, deleteInterface[i]);
            }
        }

        return result;
    }

    @GetMapping("/performance-setting/exportExcel")
    public void exportExcel(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        performanceSettingApiClient.exportExcel(params, response);

    }

    @GetMapping("/performance-setting/lan-wan-wLan-view-detail")
    @ResponseBody
    public String lanWanWLanViewDetail(@RequestParam Map<String, String> params) {
        System.out.println("start");
        JsonArray list = new JsonArray();
        Device[] deviceList = null;
        if (params.get("monitoring").equals("1")) {
            // single
            deviceList = new Device[1];
            deviceList[0] = new Device();
            Map<String, String> parameters = new HashMap<>();
            parameters.put("_deviceId._Manufacturer", params.get("manufacturer"));
            parameters.put("summary.modelName", params.get("modelName"));
            parameters.put("_deviceId._SerialNumber", params.get("serialNumber"));
            AcsResponse response = acsApiClient.getDevices(params.get("serialNumber"));
            JsonArray array = new Gson().fromJson(response.body, JsonArray.class);
            JsonObject deviceObject = array.get(0).getAsJsonObject();
            deviceList[0].parameters = parameters;
            deviceList[0].id = deviceObject.get("_id").getAsString();
        } else if (params.get("monitoring").equals("2")) {
            // group
            deviceList = deviceGroupClient.getListDeviceByGroup(params.get("deviceGroupId"));
        } else {
            // file
            PerformanceSetting performanceSetting = performanceSettingApiClient.get(Long.valueOf(params.get("performanceSettingId")));
            List<String> listDeviceIds = new ArrayList<String>();
            for (String deviceId : performanceSetting.externalDevices) {
                listDeviceIds.add(String.format("{\"%s\":\"%s\"}", "_id", deviceId));
            }
            String query = String.format("{\"$or\":[%s]}", org.apache.commons.lang3.StringUtils.join(listDeviceIds, ","));
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("query", query);
            queryParams.put("projection", "_deviceId._SerialNumber");
            AcsResponse response = acsApiClient.findDevices(queryParams);
            List<Device> deviceList1 = Device.fromJsonString(response.body, deviceIndexParams().keySet());
            deviceList = deviceList1.stream().toArray(Device[]::new);
            System.out.println("lol");
        }
        list = loadDataLanWanWLan(params.get("performanceSettingId"), deviceList, params.get("statisticType"),
                params.get("textFromDate"), params.get("textToDate"), params.get("type"),
                params.get("indexPage"), params.get("limitPage"));
        return list.toString();
    }

    @GetMapping("/performance-setting/data-confirm-chart")
    @ResponseBody
    public String dataConfirmChart(@RequestParam Map<String, String> params) {
        String result = "";
        StringBuilder sbManufacturer = new StringBuilder("[");
        StringBuilder sbModelName = new StringBuilder("[");
        StringBuilder sbSerialNumber = new StringBuilder("[");
        if (params.get("deviceGroupId") != null) {
            Map<String, String> queryParams = new HashMap<String, String>();
            String query = "";
            if (params.get("monitoring").equals("3")) {
                // file
                PerformanceSetting performanceSetting = performanceSettingApiClient.get(Long.valueOf(params.get("performanceSettingId")));
                List<String> listDeviceIds = new ArrayList<String>();
                for (String deviceId : performanceSetting.externalDevices) {
                    listDeviceIds.add(String.format("{\"%s\":\"%s\"}", "_id", deviceId));
                }
                query = String.format("{\"$or\":[%s]}", org.apache.commons.lang3.StringUtils.join(listDeviceIds, ","));
            } else {
                // group
                DeviceGroup deviceGroup = deviceGroupClient.get(Long.valueOf(params.get("deviceGroupId")));
                query = deviceGroup.query;
            }
            queryParams.put("query", query);
            queryParams.put("projection", "_deviceId._SerialNumber");
            AcsResponse response = acsApiClient.findDevices(queryParams);
            JsonArray array = new Gson().fromJson(response.body, JsonArray.class);

            List<String> listManufacturer = new ArrayList<String>();
            List<String> listModelName = new ArrayList<String>();

            for (int i = 0; i < array.size(); i++) {
                JsonObject deviceObject = array.get(i).getAsJsonObject();
                String modelName = deviceObject.get("summary.modelName").getAsJsonObject().get("_value").getAsString().replaceAll("\"", "");
                String manufacturer = deviceObject.get("summary.manufacturer").getAsString().replaceAll("\"", "");
                if (!listManufacturer.contains(manufacturer)) {
                    sbManufacturer.append(String.format(",{\"id\": \"%s\", \"text\": \"%s\"}", manufacturer, manufacturer));
                    listManufacturer.add(manufacturer);
                }
                if (!listModelName.contains(modelName)) {
                    sbModelName.append(String.format(",{\"id\": \"%s\", \"text\": \"%s\"}", modelName, modelName));
                    listModelName.add(modelName);
                }
            }

            if (sbManufacturer.length() > 2) {
                sbManufacturer.deleteCharAt(1);
            }
            sbManufacturer.append("]");

            if (sbModelName.length() > 2) {
                sbModelName.deleteCharAt(1);
            }
            sbModelName.append("]");

            result += sbManufacturer.toString() + "|" + sbModelName.toString();
        } else {
            String modelName = params.get("modelName");
            String manufacturer = params.get("manufacturer");
            sbManufacturer.append(String.format(",{\"id\": \"%s\", \"text\": \"%s\"}", manufacturer, manufacturer));
            sbModelName.append(String.format(",{\"id\": \"%s\", \"text\": \"%s\"}", modelName, modelName));

            List<String> listSerialNumber = new ArrayList<String>();
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("query", "{\"_deviceId._Manufacturer\":\"/" + manufacturer + "/\",\"summary.modelName\":\"/" + modelName + "/\"}");
            queryParams.put("projection", "_deviceId._SerialNumber");
            AcsResponse response = acsApiClient.findDevices(queryParams);
            JsonArray array = new Gson().fromJson(response.body, JsonArray.class);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < array.size(); i++) {
                JsonObject deviceObject = array.get(i).getAsJsonObject();
                String serialNumber = deviceObject.get("summary.serialNumber").getAsString();
                if (!listSerialNumber.contains(serialNumber)) {
                    sbSerialNumber.append(String.format(",{\"id\": \"%s\", \"text\": \"%s\"}", serialNumber, serialNumber));
                    listSerialNumber.add(serialNumber);
                }
            }

            if (sbSerialNumber.length() > 2) {
                sbSerialNumber.deleteCharAt(1);
            }
            sbSerialNumber.append("]");

            result = sbSerialNumber.toString();
        }


        return result;
    }

    @PostMapping("/performance-setting/lan-service-chart")
    @ResponseBody
    public String lanServiceChart(@RequestParam Map<String, String> params) {

        String result = "";
        AcsResponse acsResponse = acsApiClient.getDevices(params.get("serialNumber"));
        JsonArray array = new Gson().fromJson(acsResponse.body, JsonArray.class);
        String deviceId = "";
        for (int i = 0; i < array.size(); i++) {
            deviceId = array.get(i).getAsJsonObject().get("_id").getAsString();
        }
        result = loadDataLanWanWLanChart(params, params.get("performanceSettingId"), deviceId, params.get("statisticType"),
                params.get("startTime"), params.get("endTime"), params.get("type"));

        return result;
    }

    private String loadDataLanWanWLanChart(Map<String, String> params, String performanceSettingId, String deviceId,
                                           String statisticType, String startTime, String endTime, String wanMode) {
        String result = "";
        JsonArray arrayLanWanWLanStatistics = new JsonArray();

        List<String> headers = new ArrayList<String>();
        List<String> interfaceName = new ArrayList<String>();
        List<String> headersNameInterface = new ArrayList<String>();

        String ssid = "";
        String bytesRxTx = PARAM_BYTES_RECEIVED;
        String ptskRxTx = PARAM_PACKETS_RECEIVED;
        String errorsRxTx = PARAM_X_BROADCOM_COM_RXERRORS;
        String dropsRxTx = PARAM_X_BROADCOM_COM_RXDROPS;
        if (statisticType.equals("LAN")) {
            statisticType = PARAM_LAN;
            ssid = PARAM_X_BROADCOM_COM_IFNAME;
            if (!wanMode.isEmpty() && wanMode.equals("TRANSMITTED")) {
                bytesRxTx = PARAM_BYTES_SENT;
                ptskRxTx = PARAM_PACKETSSENT;
                errorsRxTx = PARAM_X_BROADCOM_COM_TXRERRORS;
                dropsRxTx = PARAM_X_BROADCOM_COM_TXDROPS;
            }
        } else if (statisticType.equals("WAN")) {
            statisticType = PARAM_WAN;
            ssid = PARAM_X_BROADCOM_COM_IFNAME;
            if (!wanMode.isEmpty() && wanMode.equals("TRANSMITTED")) {
                bytesRxTx = PARAM_BYTES_SENT;
                ptskRxTx = PARAM_PACKETSSENT;
                errorsRxTx = PARAM_X_BROADCOM_COM_TXRERRORS;
                dropsRxTx = PARAM_X_BROADCOM_COM_TXDROPS;
            }
        } else {
            statisticType = PARAM_WLAN;
            ssid = PARAM_X_BROADCOM_COM_WIFINAME;
            if (!wanMode.isEmpty() && wanMode.equals("TRANSMITTED")) {
                bytesRxTx = PARAM_BYTES_SENT;
                ptskRxTx = PARAM_PACKETSSENT;
                errorsRxTx = PARAM_X_BROADCOM_COM_TXRERRORS;
                dropsRxTx = PARAM_X_BROADCOM_COM_TXDROPS;
            }
        }

        JsonObject jsonObject = new JsonObject();
        PerformanceStatisticsELK[] performanceStatisticsELKS = performanceSettingApiClient.searchPerformanceStatistics(deviceId,
                performanceSettingId, startTime, endTime);

        for (int k = 0; k < performanceStatisticsELKS.length; k++) {
            String paramLanName = "";
            JsonArray jsonArrayData = new JsonArray();
            if (performanceStatisticsELKS[k].parameterNames.contains(statisticType)) {
                String time = "";
                JsonObject jsonObject4 = new JsonObject();
                JsonObject jsonObject1 = new Gson().fromJson(performanceStatisticsELKS[k].valueChanges, JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : jsonObject1.entrySet()) {
                    String value = entry.getValue().toString().replaceAll("\"", "");
                    if (entry.getKey().contains(bytesRxTx)) {
                        jsonObject4.addProperty("bytes", value);
                    } else if (entry.getKey().contains(ptskRxTx)) {
                        jsonObject4.addProperty("pkts", value);
                    } else if (entry.getKey().contains(errorsRxTx)) {
                        jsonObject4.addProperty("errors", value);
                    } else if (entry.getKey().contains(dropsRxTx)) {
                        jsonObject4.addProperty("drops", value);
                    }

                    if (entry.getKey().contains(ssid)) {
                        paramLanName = value;
                    }
                }

//                if (headers.size() < performanceStatisticsELKS.length) {
                    time = StringUtils.convertDateFromElk(performanceStatisticsELKS[k].timestamp,
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd HH:mm:ss");
                    headers.add(time);
                    interfaceName.add(paramLanName);
                    headersNameInterface.add(time + "|" + paramLanName);

//                }

                jsonObject4.addProperty("name", paramLanName);
                jsonObject4.addProperty("timestamp", time);
                if (jsonObject.get("data") != null) {
                    jsonArrayData = jsonObject.get("data").getAsJsonArray();
                }
                jsonArrayData.add(jsonObject4);
                jsonObject.add("data", jsonArrayData);
            }

        }

        arrayLanWanWLanStatistics.add(jsonObject);
        addHeaderInterfaceName(headersNameInterface, arrayLanWanWLanStatistics);

        StringBuilder sb = new StringBuilder("[");
        java.util.Collections.sort(headers);
        Set<String> depdupeCustomers = new LinkedHashSet<>(headers);
        headers.clear();
        headers.addAll(depdupeCustomers);

        Set<String> depdupeCustomers1 = new LinkedHashSet<>(interfaceName);
        interfaceName.clear();
        interfaceName.addAll(depdupeCustomers1);


        for (String a : headers) {
            sb.append(String.format(",\"%s\"", a.replaceAll("T", " ").replaceAll("Z", "")));
        }
        if (headers.size() != 0) {
            sb.deleteCharAt(1);
        }
        sb.append("]");

        JsonObject jsonObject1 = arrayLanWanWLanStatistics.get(0).getAsJsonObject();
        if (jsonObject1.size() != 0) {
            JsonArray jsonArrayData = jsonObject1.get("data").getAsJsonArray();
            result = getDataChartLanWanWLan(interfaceName, headers, jsonArrayData);
            result = result.substring(1);
        } else {
            result = "{}|{}|{}|{}";
        }
        result += "|" + sb.toString();
        result += "|" + params.get("serialNumber");
        return result;
    }

    private String getDataChartLanWanWLan(List<String> interfaceName, List<String> headers, JsonArray jsonArrayData) {

        String result = "";
        for (int transmitted = 0; transmitted < 4; transmitted++) {
            JsonArray jsonArrayBytes = new JsonArray();
            for (int interfaceN = 0; interfaceN < interfaceName.size(); interfaceN++) {
                List<String> interfaceName1 = new ArrayList<String>();
                if (jsonArrayBytes.size() > 0) {
                    for (int check = 0; check < jsonArrayBytes.size(); check++) {
                        JsonObject jsonObject5 = jsonArrayBytes.get(check).getAsJsonObject();
                        String nameTemp = jsonObject5.get("name").toString().replaceAll("\"", "");
                        interfaceName1.add(nameTemp);
                    }
                    if (!interfaceName1.contains(interfaceName.get(interfaceN)) && !interfaceName.get(interfaceN).isEmpty()) {
                        JsonObject jsonObject3 = new JsonObject();
                        jsonObject3.addProperty("name", interfaceName.get(interfaceN));
                        jsonObject3.add("data", createBytesArray(headers, jsonArrayData, interfaceName, interfaceN, transmitted));
                        jsonArrayBytes.add(jsonObject3);
                    }
                } else {
                    if (!interfaceName.get(interfaceN).isEmpty()) {
                        JsonObject jsonObject3 = new JsonObject();
                        jsonObject3.addProperty("name", interfaceName.get(interfaceN));
                        jsonObject3.add("data", createBytesArray(headers, jsonArrayData, interfaceName, interfaceN, transmitted));
                        jsonArrayBytes.add(jsonObject3);
                    }
                }
            }

            result += "|" + jsonArrayBytes.toString();
        }

        return result;
    }

    private JsonArray createBytesArray(List<String> headers, JsonArray jsonArrayData,
                                       List<String> interfaceName, int interfaceN, int transmitted) {
        JsonArray jsonArray = new JsonArray();
        for (int header = 0; header < headers.size(); header++) {
            boolean check = false;
            for (int array = 0; array < jsonArrayData.size(); array++) {
                JsonObject jsonObject2 = jsonArrayData.get(array).getAsJsonObject();
                String timestamp = jsonObject2.get("timestamp").toString().replaceAll("\"", "");
                String name = jsonObject2.get("name").toString().replaceAll("\"", "");
                String mode = "";
                if (transmitted == 0) {
                    mode = "bytes";
                } else if (transmitted == 1) {
                    mode = "pkts";
                } else if (transmitted == 2) {
                    mode = "errors";
                } else if (transmitted == 3) {
                    mode = "drops";
                }
                if (headers.get(header).equals(timestamp) && interfaceName.get(interfaceN).equals(name)) {
                    try {
                        jsonArray.add(Double.valueOf(jsonObject2.get(mode).toString().replaceAll("\"", "")));
                    } catch (Exception e) {
                        jsonArray.add(0);
                    }
                    check = true;
                }
            }
            if (!check) {
                jsonArray.add(0);
            }
        }
        return jsonArray;
    }

    private JsonArray loadDataLanWanWLan(String performanceSettingId, Device[] deviceList, String type,
                                         String startTime, String endTime, String wanMode,
                                         String indexPage, String limitPage) {
        JsonArray arrayLanWanWLanStatistics = new JsonArray();
        if (deviceList.length > 0) {
            List<String> headers = new ArrayList<String>();
            List<String> headersNameInterface = new ArrayList<String>();
            String ssid = "";
            String bytesRxTx = PARAM_BYTES_RECEIVED;
            String ptskRxTx = PARAM_PACKETS_RECEIVED;
            String errorsRxTx = PARAM_X_BROADCOM_COM_RXERRORS;
            String dropsRxTx = PARAM_X_BROADCOM_COM_RXDROPS;
            String typeDelete = type;
            if (type.equals("LAN")) {
                type = PARAM_LAN;
                ssid = PARAM_X_BROADCOM_COM_IFNAME;
                if (!wanMode.isEmpty() && wanMode.equals("TRANSMITTED")) {
                    bytesRxTx = PARAM_BYTES_SENT;
                    ptskRxTx = PARAM_PACKETSSENT;
                    errorsRxTx = PARAM_X_BROADCOM_COM_TXRERRORS;
                    dropsRxTx = PARAM_X_BROADCOM_COM_TXDROPS;
                }
            } else if (type.equals("WAN")) {
                type = PARAM_WAN;
                ssid = PARAM_X_BROADCOM_COM_IFNAME;
                if (!wanMode.isEmpty() && wanMode.equals("TRANSMITTED")) {
                    bytesRxTx = PARAM_BYTES_SENT;
                    ptskRxTx = PARAM_PACKETSSENT;
                    errorsRxTx = PARAM_X_BROADCOM_COM_TXRERRORS;
                    dropsRxTx = PARAM_X_BROADCOM_COM_TXDROPS;
                }
            } else {
                type = PARAM_WLAN;
                ssid = PARAM_X_BROADCOM_COM_WIFINAME;
                if (!wanMode.isEmpty() && wanMode.equals("TRANSMITTED")) {
                    bytesRxTx = PARAM_BYTES_SENT;
                    ptskRxTx = PARAM_PACKETSSENT;
                    errorsRxTx = PARAM_X_BROADCOM_COM_TXRERRORS;
                    dropsRxTx = PARAM_X_BROADCOM_COM_TXDROPS;
                }
            }


            for (int i = 0; i < deviceList.length; i++) {
                JsonObject jsonObject = new JsonObject();
                PerformanceStatisticsELK[] performanceStatisticsELKS = performanceSettingApiClient.searchPerformanceStatistics(deviceList[i].id,
                        performanceSettingId, startTime, endTime);

                for (int k = 0; k < performanceStatisticsELKS.length; k++) {
                    String paramLanName = "";
                    double bytes = 0;
                    double ptsk = 0;
                    double errors = 0;
                    double drops = 0;
                    JsonArray jsonArrayParamLan = new JsonArray();
                    JsonArray jsonArrayData = new JsonArray();
                    System.out.println("valueChanges : "+performanceStatisticsELKS[k].valueChanges);
                    if (performanceStatisticsELKS[k].valueChanges.contains(type)) {
                        String time = "";
                        JsonObject jsonObject4 = new JsonObject();
                        JsonObject jsonObject1 = new Gson().fromJson(performanceStatisticsELKS[k].valueChanges, JsonObject.class);

                        for (Map.Entry<String, JsonElement> entry : jsonObject1.entrySet()) {
                            String value = entry.getValue().toString().replaceAll("\"", "");
                            if (entry.getKey().contains(bytesRxTx)) {
                                bytes = Double.valueOf(value);
                                jsonObject4.addProperty("bytes", Double.valueOf(value));
                            } else if (entry.getKey().contains(ptskRxTx)) {
                                ptsk = Double.valueOf(value);
                                jsonObject4.addProperty("pkts", Double.valueOf(value));
                            } else if (entry.getKey().contains(errorsRxTx)) {
                                errors = Double.valueOf(value);
                                jsonObject4.addProperty("errors", Double.valueOf(value));
                            } else if (entry.getKey().contains(dropsRxTx)) {
                                drops = Double.valueOf(value);
                                jsonObject4.addProperty("drops", Double.valueOf(value));
                            }

                            if (!entry.getKey().isEmpty() && entry.getKey().contains(ssid)) {
                                if (jsonObject.get(value) != null) {
                                    jsonArrayParamLan = jsonObject.get(value).getAsJsonArray();
                                    for (int a = 0; a < jsonArrayParamLan.size(); a++) {
                                        JsonObject jsonObject5 = jsonArrayParamLan.get(a).getAsJsonObject();
                                        for (Map.Entry<String, JsonElement> entry1 : jsonObject5.entrySet()) {
                                            String value1 = entry1.getValue().toString().replaceAll("\"", "");
                                            if (entry1.getKey().contains(bytesRxTx)) {
                                                bytes = Double.valueOf(value1);
                                            } else if (entry1.getKey().contains(ptskRxTx)) {
                                                ptsk = Double.valueOf(value1);
                                            } else if (entry1.getKey().contains(errorsRxTx)) {
                                                errors = Double.valueOf(value1);
                                            } else if (entry1.getKey().contains(dropsRxTx)) {
                                                drops = Double.valueOf(value1);
                                            }
                                        }
                                    }
                                }
                                paramLanName = value;
                            }
                        }

//                        if (headers.size() < performanceStatisticsELKS.length) {
                        time = StringUtils.convertDateFromElk(performanceStatisticsELKS[k].timestamp,
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd HH:mm:ss");
                        headers.add(time);
                        if (!paramLanName.isEmpty()) {
                            headersNameInterface.add(time + "|" + paramLanName);
                        }

//                        }
                        if (!paramLanName.isEmpty()) {
                            JsonObject jsonObject3 = new JsonObject();
                            jsonObject3.addProperty("bytes", bytes);
                            jsonObject3.addProperty("errors", errors);
                            jsonObject3.addProperty("pkts", ptsk);
                            jsonObject3.addProperty("drops", drops);
                            jsonArrayParamLan.add(jsonObject3);
                            jsonObject.add(paramLanName, jsonArrayParamLan);
                            System.out.println("======================================");
                            System.out.println("timestamp : " + time);
                            jsonObject4.addProperty("name", paramLanName);
                            jsonObject4.addProperty("timestamp", time);
                            if (jsonObject.get("data") != null) {
                                jsonArrayData = jsonObject.get("data").getAsJsonArray();
                            }
                            jsonArrayData.add(jsonObject4);
                            jsonObject.add("data", jsonArrayData);
                        }

                    }

                }

                jsonObject.addProperty("id", deviceList[i].id);
                jsonObject.addProperty("manufacture", deviceList[i].parameters.get("_deviceId._Manufacturer"));
                jsonObject.addProperty("modelName", deviceList[i].parameters.get("summary.modelName"));
                jsonObject.addProperty("serialNumber", deviceList[i].parameters.get("_deviceId._SerialNumber"));
                arrayLanWanWLanStatistics.add(jsonObject);
            }

            // paging
            int lastPage = 0;
            if (deviceList.length == 0) {
                lastPage = 1;
            } else {
                int page = deviceList.length % (Integer.valueOf(limitPage));
                int pageTotal = deviceList.length / Integer.valueOf(limitPage);
                lastPage = page == 0 ? pageTotal : pageTotal + 1;
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("totalPage", deviceList.length);
            jsonObject.addProperty("lastPage", lastPage);
            jsonObject.addProperty("indexPage", indexPage);
            arrayLanWanWLanStatistics.add(jsonObject);

            System.out.println(arrayLanWanWLanStatistics.toString());
            deleteParm(arrayLanWanWLanStatistics, typeDelete);
            addHeaderInterfaceName(headersNameInterface, arrayLanWanWLanStatistics);

            System.out.println(arrayLanWanWLanStatistics);
        }
        return arrayLanWanWLanStatistics;
    }

    private JsonArray addHeaderInterfaceName(List<String> headersNameInterface, JsonArray arrayLanWanWLanStatistics) {
        JsonObject jsonObject3 = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        java.util.Collections.sort(headersNameInterface);
        List<String> headerList = new ArrayList<>();
        for (String header : headersNameInterface) {
            jsonArray.add(header);
            headerList.add(header);
        }
        jsonObject3.add("headersNameInterface", jsonArray);
        arrayLanWanWLanStatistics.add(jsonObject3);
        return arrayLanWanWLanStatistics;
    }

    private JsonArray deleteParm(JsonArray arrayLanWanWLanStatistics, String type) {
        if (type.equals("LAN")) {
            type = "eth";
        } else if (type.equals("WAN")) {
            type = "ve";
        } else {
            type = "wl";
        }
        for (int a = 0; a < arrayLanWanWLanStatistics.size(); a++) {
            JsonObject jsonObject5 = new JsonObject();
            jsonObject5 = arrayLanWanWLanStatistics.get(a).getAsJsonObject();
            if (jsonObject5.get("data") != null) {
                for (Map.Entry<String, JsonElement> entry1 : jsonObject5.entrySet()) {
                    String value = entry1.getKey().toString().replace("\"", "");
                    if (value.startsWith(type)) {
                        JsonArray jsonArray = jsonObject5.get(value).getAsJsonArray();
//                    if (jsonArray.size() > 1) {
                        double bytes = 0;
                        double pkts = 0;
                        double errors = 0;
                        double drops = 0;
                        for (int x = 0; x < jsonArray.size(); x++) {
                            JsonObject jsonObject = jsonArray.get(x).getAsJsonObject();
                            for (Map.Entry<String, JsonElement> entry2 : jsonObject.entrySet()) {
                                String value1 = entry2.getValue().toString().replaceAll("\"", "");
                                if (entry2.getKey().contains("bytes")) {
                                    bytes += Double.valueOf(value1);
                                } else if (entry2.getKey().contains("pkts")) {
                                    pkts += Double.valueOf(value1);
                                } else if (entry2.getKey().contains("errors")) {
                                    errors += Double.valueOf(value1);
                                } else if (entry2.getKey().contains("drops")) {
                                    drops += Double.valueOf(value1);
                                }
                            }
                        }

                        for (int k = 0; k < jsonArray.size(); ) {
                            System.out.println("------------------------");
                            jsonArray.remove(k);
                        }
                        JsonObject jsonObject3 = new JsonObject();
                        jsonObject3.addProperty("bytesFinal", bytes);
                        jsonObject3.addProperty("errorsFinal", errors);
                        System.out.println("pkts total; " + pkts);
                        jsonObject3.addProperty("pktsFinal", pkts);
                        jsonObject3.addProperty("dropsFinal", drops);
                        jsonArray.add(jsonObject3);
//                    }
                    }
                }
            }
        }
        return arrayLanWanWLanStatistics;

    }

    private JsonArray loadDataRamCpu(String performanceSettingId, Device[] deviceList, String type, String startTime, String endTime) {

        JsonArray arrayRamCpuStatistics = new JsonArray();

        if (deviceList.length > 0) {
            List<String> headers = new ArrayList<String>();
            String mode = type;
            if (type.equals("RAM")) {
                type = PARAM_RAM;
            } else {
                type = PARAM_CPU;
            }
            // left side
            for (int i = 0; i < deviceList.length; i++) {
                PerformanceStatisticsELK[] performanceStatisticsELKS = performanceSettingApiClient.searchPerformanceStatistics(deviceList[i].id,
                        performanceSettingId, startTime, endTime);
                double count = 0;
                int count1 = 0;
                JsonArray jsonArray = new JsonArray();
                for (int k = 0; k < performanceStatisticsELKS.length; k++) {

                    if (performanceStatisticsELKS[k].parameterNames.contains(type)) {
//                        if (headers.size() < performanceStatisticsELKS.length) {
                        headers.add(StringUtils.convertDateFromElk(performanceStatisticsELKS[k].timestamp,
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd HH:mm:ss"));
//                        }
                        JsonObject jsonObject1 = new Gson().fromJson(performanceStatisticsELKS[k].valueChanges, JsonObject.class);
                        String data = jsonObject1.get(type).toString().replaceAll("\"", "");
                        count += Integer.parseInt(data);

                        JsonObject jsonObjectRow = new JsonObject();
                        jsonObjectRow.addProperty("timeHeader", StringUtils.convertDateFromElk(performanceStatisticsELKS[k].timestamp,
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd HH:mm:ss"));
                        jsonObjectRow.addProperty("timeData", data);
                        jsonArray.add(jsonObjectRow);
                        count1++;
                    }

                }
                if (jsonArray.size() != 0) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("data", jsonArray);
                    jsonObject.addProperty("id", deviceList[i].id);
                    jsonObject.addProperty("manufacture", deviceList[i].parameters.get("_deviceId._Manufacturer"));
                    jsonObject.addProperty("modelName", deviceList[i].parameters.get("summary.modelName"));
                    jsonObject.addProperty("serialNumber", deviceList[i].parameters.get("_deviceId._SerialNumber"));

                    double roundOff = (double) Math.round((count / count1) * 100) / 100;
                    System.out.println("roundOff : "+roundOff);
                    jsonObject.addProperty("cpuRamPercent",new DecimalFormat("##.##").format(roundOff));
                    arrayRamCpuStatistics.add(jsonObject);
                }
            }

            // right side
            // header + row
            JsonObject jsonObject3 = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            java.util.Collections.sort(headers);
            List<String> headerList = new ArrayList<>();
            for (String header : headers) {
                jsonArray.add(header);
                headerList.add(header);

            }
            jsonObject3.add("header", jsonArray);
            arrayRamCpuStatistics.add(jsonObject3);

            // bottom side
            // paging

            int lastPage = 0;
            int indexPage = 1;
            if (deviceList.length == 0) {
                lastPage = 1;
            } else {
//                int page = deviceList.length % (requestParams.size() == 0 ? 20
//                        : Integer.valueOf(requestParams.get(PAGE_SIZE)));
//                int pageTotal = this.totalPages / (requestParams.size() == 0 ? 20
//                        : Integer.valueOf(requestParams.get(PAGE_SIZE)));
//                lastPage = page == 0 ? pageTotal : pageTotal + 1;
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("totalPage", deviceList.length);
            jsonObject.addProperty("lastPage", lastPage);
            jsonObject.addProperty("indexPage", indexPage);
            arrayRamCpuStatistics.add(jsonObject);

            setDataForChartRamCpu(arrayRamCpuStatistics, headerList, mode);

        }
        System.out.println(arrayRamCpuStatistics.toString());
        return arrayRamCpuStatistics;
    }

    private void setDataForChartRamCpu(JsonArray data, List<String> headerList, String type) {
        JsonArray arrayFinal = new JsonArray();
        String deviceName = "";
        JsonObject asJsonObject = data.get(0).getAsJsonObject();
        if (asJsonObject.get("data") != null) {
            JsonArray asJsonArray = asJsonObject.get("data").getAsJsonArray();
            JsonArray jsonArray = createTimeRamCpu(headerList, asJsonArray);
            JsonObject jsonObject = new JsonObject();
            deviceName += "," + asJsonObject.get("serialNumber").toString().replaceAll("\"", "");
            jsonObject.addProperty("name", asJsonObject.get("serialNumber").toString().replaceAll("\"", ""));
            jsonObject.add("data", jsonArray);
            arrayFinal.add(jsonObject);
        }

        StringBuilder sb = new StringBuilder("[");
        for (String a : headerList) {
            sb.append(String.format(",\"%s\"", a.replaceAll("T", " ").replaceAll("Z", "")));
        }
        if (headerList.size() != 0) {
            sb.deleteCharAt(1);
        }
        sb.append("]");

        String result = "";
        result += arrayFinal.toString();
        result += "|" + sb.toString();
        if (type.equals("cpu")) {
            result += "|" + "CPU traffic of devices";
        } else {
            result += "|" + "RAM traffic of devices";
        }
        if (!deviceName.equals("")) {
            deviceName = deviceName.substring(1);
        }
        result += "|" + deviceName;
        session.setAttribute("ramCpu", result);

    }

    private JsonArray createTimeRamCpu(List<String> headerList, JsonArray asJsonArray) {
        JsonArray jsonArray = new JsonArray();

        for (String a : headerList) {
            for (int k = 0; k < asJsonArray.size(); k++) {
                JsonObject asJsonObject1 = asJsonArray.get(k).getAsJsonObject();
                if (a.equals(asJsonObject1.get("timeHeader").toString().replaceAll("\"", ""))) {
                    jsonArray.add(Integer.valueOf(asJsonObject1.get("timeData").toString().replaceAll("\"", "")));
                }
            }
        }
        return jsonArray;
    }

}
