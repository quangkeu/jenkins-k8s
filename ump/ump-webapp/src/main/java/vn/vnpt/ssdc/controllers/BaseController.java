package vn.vnpt.ssdc.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import vn.vnpt.ssdc.api.client.*;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.api.model.DeviceType;
import vn.vnpt.ssdc.api.model.DeviceTypeVersion;
import vn.vnpt.ssdc.api.model.Tag;
import vn.vnpt.ssdc.models.Device;
import vn.vnpt.ssdc.utils.ObjectUtils;

import javax.servlet.http.HttpSession;
import java.util.*;

import static vn.vnpt.ssdc.utils.Constants.*;

/**
 * Created by vietnq on 11/8/16.
 */
public class BaseController {
    public static final String HEADER_DEVICES = "devices";
    public static final String HEADER_DATA_MODELS = "data-model";
    public static final String LIST_COLUMN = "Manufacturer,Product Class,Firmware version";
    public static final String LIST_SELECT_OPERATOR = "OR,AND";
    public static final String LIST_SELECT_COLUMN = "Last inform,Serial Number,Manufacturer,Firmware version,IP";
    public static final String LIST_SELECT_COMPARE = "=,#,<,<=,>,>=";

    public static final String ID = "_id";
    public static final String IP = "_ip";
    public static final String SERIAL_NUMBER = "_deviceId._SerialNumber";
    public static final String MANUFACTURER = "_deviceId._Manufacturer";
    public static final String OUI = "_deviceId._OUI";
    public static final String PRODUCT_CLASS = "_deviceId._ProductClass";
    public static final String FIRMWARE_VERSION = "summary.softwareVersion";
    public static final String LAST_INFORM = "_lastInform";
    public static final String LAST_BOOT = "_lastBoot";
    public static final String MAC_ADDRESS = "summary.mac";
    public static final String MODEL_NAME = "summary.modelName";
    public static final String LABEL = "_tags";
    public static final String CREATED = "_registered";
    public static final String HARDWARE_VERSION = "summary.hardwareVersion";
    public static final String CONNECTION_REQUEST_URL = "summary.connectionRequestURL";
    public static final String MACADDRESS = "summary.mac";
    public static final String PERIODIC_INFORM_INTERVAL = "summary.periodicInformInterval";

    public static final String LIST_COLUMN_DATA_MODEL = "Manufacturer,Model Name";

    public static final String LAN_STATISTIC_RECEIVED_LIST = "InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{j}.Stats.BytesReceived," +
            "InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{j}.Stats.PacketsReceived," +
            "InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{j}.Stats.X_BROADCOM_COM_RxErrors," +
            "InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{j}.Stats.X_BROADCOM_COM_RxDrops," +
            "InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{j}.X_BROADCOM_COM_IfName";

    public static final String LAN_STATISTIC_TRANSMITTED_LIST = "InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{j}.Stats.BytesSent," +
            "InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{j}.Stats.PacketsSent," +
            "InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{j}.Stats.X_BROADCOM_COM_TxErrors," +
            "InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{j}.Stats.X_BROADCOM_COM_TxDrops," +
            "InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{j}.X_BROADCOM_COM_IfName";

    public static final String WAN_STATISTIC_RECEIVED_LIST = "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANIPConnection.{j}.Stats.EthernetBytesReceived," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANPPPConnection.{j}.Stats.EthernetBytesReceived," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANIPConnection.{j}.Stats.EthernetPacketsReceived," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANPPPConnection.{j}.Stats.EthernetPacketsReceived," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANIPConnection.{j}.Stats.X_BROADCOM_COM_RxErrors," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANPPPConnection.{j}.Stats.EX_BROADCOM_COM_RxErrors," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANIPConnection.{j}.Stats.X_BROADCOM_COM_RxDrops," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANPPPConnection.{j}.Stats.X_BROADCOM_COM_RxDrops," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANIPConnection.{j}.X_BROADCOM_COM_IfName," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANPPPConnection.{j}.X_BROADCOM_COM_IfName";

    public static final String WAN_STATISTIC_TRANSMITTED_LIST = "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANIPConnection.{j}.Stats.EthernetBytesSent," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANPPPConnection.{j}.Stats.EthernetBytesSent," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANIPConnection.{j}.Stats.EthernetPacketsSent," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANPPPConnection.{j}.Stats.EthernetPacketSent," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANIPConnection.{j}.Stats.X_BROADCOM_COM_TxErrors," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANPPPConnection.{j}.Stats.X_BROADCOM_COM_TxErrors," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANIPConnection.{j}.Stats.X_BROADCOM_COM_TxDrops," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANPPPConnection.{j}.Stats.X_BROADCOM_COM_TxDrops," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANIPConnection.{j}.X_BROADCOM_COM_IfName," +
            "InternetGatewayDevice.WANDevice.5.WANConnectionDevice.{i}.WANPPPConnection.{j}.X_BROADCOM_COM_IfName";

    public static final String WLAN_STATISTIC_RECEIVED_LIST = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_BROADCOM_COM_WlanAdapter.WlVirtIntfCfg.{j}.Stats.TotalBytesReceived," +
            "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_BROADCOM_COM_WlanAdapter.WlVirtIntfCfg.{j}.Stats.TotalPacketsReceived," +
            "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_BROADCOM_COM_WlanAdapter.WlVirtIntfCfg.{j}.Stats.X_BROADCOM_COM_RxErrors," +
            "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_BROADCOM_COM_WlanAdapter.WlVirtIntfCfg.{j}.Stats.X_BROADCOM_COM_RxDrops," +
            "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_BROADCOM_COM_WlanAdapter.WlVirtIntfCfg.{j}.WlIfcname";

    public static final String WLAN_STATISTIC_TRANSMITTED_LIST = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_BROADCOM_COM_WlanAdapter.WlVirtIntfCfg.{j}.Stats.TotalBytesSent," +
            "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_BROADCOM_COM_WlanAdapter.WlVirtIntfCfg.{j}.Stats.TotalPacketsSent," +
            "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_BROADCOM_COM_WlanAdapter.WlVirtIntfCfg.{j}.Stats.X_BROADCOM_COM_TxErrors," +
            "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_BROADCOM_COM_WlanAdapter.WlVirtIntfCfg.{j}.Stats.X_BROADCOM_COM_TxDrops," +
            "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.X_BROADCOM_COM_WlanAdapter.WlVirtIntfCfg.{j}.WlIfcname";

    public static final String CPU_STATISTIC_LIST = "InternetGatewayDevice.CpuRam.CpuUsed";
    public static final String RAM_STATISTIC_LIST = "InternetGatewayDevice.CpuRam.RamUsedPer";


    @Autowired
    protected HttpSession session;

    @Autowired
    protected TagClient tagClient;

    @Autowired
    protected AcsApiClient acsApiClient;

    @Autowired
    protected DeviceTypeClient deviceTypeClient;

    @Autowired
    protected DeviceTypeVersionClient deviceTypeVersionClient;

    @Autowired
    private MessageSource messageSource;


//    private LinkedHashMap<String, String>() abc = new LinkedHashMap<String, String>();

    //returns a map containing friendly name and actual path for parameters shown in device index view
    //TODO move this to configuration
    protected Map<String, String> deviceIndexParams() {
        return new LinkedHashMap<String, String>() {{
            // Infor. map voi key trong file message
            put(ID, "ID");
            put(MANUFACTURER, "Infor.Manufacturer");
            put(MODEL_NAME, "Infor.ModelName");
            put(FIRMWARE_VERSION, "Infor.FirmwareVersion");
            put(LABEL, "Infor.Label");
            put(SERIAL_NUMBER, "Infor.SerialNumber");
            put(CREATED, "firmwares.created");
            put(LAST_INFORM, "Infor.Updated");
            put(PERIODIC_INFORM_INTERVAL, "");
        }};
    }

    protected Map<String, String> fileManagementIndexParams() {
        return new LinkedHashMap<String, String>() {{
            // Infor. map voi key trong file message
            put("_id", "ID");
            put("filename", "File.Filename");
            put("metadata.manufacturer", "File.Manufacturer");
            put("metadata.modelName", "File.ModelName");
            put("metadata.fileType", "File.FileType");
            put("metadata.uploadFileName", "uploadFileName");
            put("metadata.version", "File.Version");
            put("uploadDate", "File.UploadDate");
            put("length", "File.Length");
        }};
    }

    protected Map<String, String> backupFileIndexParams() {
        return new LinkedHashMap<String, String>() {{
            // Infor. map voi key trong file message
            put("_id", "ID");
            put("metadata.manufacturer", "File.Manufacturer");
            put("metadata.modelName", "File.ModelName");
            put("metadata.fileType", "File.FileType");
            put("filename", "File.Filename");
            put("metadata.version", "File.Version");
            put("uploadDate", "File.UploadDate");
            put("length", "File.Length");
        }};
    }

    protected Map<String, String> fileManagementIndexParamsForSelect() {
        return new LinkedHashMap<String, String>() {{
            put("_id", "id");
            put("filename", "filename");
            put("metadata.fileType", "fileType");
            put("metadata.oui", "oui");
            put("metadata.productClass", "productClass");
            put("metadata.version", "version");
        }};
    }

    protected Map<String, String> deviceInfoIndexParams() {
        return new LinkedHashMap<String, String>() {{
            put(ID, "ID");
            put(MODEL_NAME, "Infor.ModelName");
            put(HARDWARE_VERSION, "HardwareVersion");
            put(FIRMWARE_VERSION, "Infor.FirmwareVersion");
            put(CREATED, "firmwares.created");
            put(MANUFACTURER, "Infor.Manufacturer");
            put(OUI, "OUI");
            put(PRODUCT_CLASS, "PRODUCT_CLASS");
            put(SERIAL_NUMBER, "Infor.SerialNumber");
            put(CONNECTION_REQUEST_URL, "CONNECTION_REQUEST_URL");
            put(CREATED, "firmwares.created");
            put(LAST_INFORM, "Infor.Updated");
            put(LAST_BOOT, "Infor.Uptime");
            put(MACADDRESS, "MAC_ADDRESS");
            put(LABEL, "TAG");
            put(PERIODIC_INFORM_INTERVAL, "");
        }};
    }

    protected Map<String, String> deviceIndexParamsSearch() {
        return new LinkedHashMap<String, String>() {{
            // Infor. map voi key trong file message
            put(MANUFACTURER, "Infor.Manufacturer");
            put(MODEL_NAME, "Infor.ModelName");
            put(FIRMWARE_VERSION, "Infor.FirmwareVersion");
            put(LABEL, "Infor.Label");
            put(SERIAL_NUMBER, "Infor.SerialNumber");
        }};
    }

    protected Map<String, String> deviceModelIndexParams() {
        return new LinkedHashMap<String, String>() {{
            // Infor. map voi key trong file message
            put(ID, "ID");
            put(MANUFACTURER, "Infor.Manufacturer");
            put(MANUFACTURER, "Infor.OUI");
            put(MODEL_NAME, "Infor.ModelName");
            put(PRODUCT_CLASS, "Infor.ProductClass");
            put(FIRMWARE_VERSION, "Infor.FirmwareVersion");
            put(CREATED, "firmwares.created");

        }};
    }

    protected Map<String, String> deviceIndexParamsReverse() {
        return new HashMap<String, String>() {{
            put("Manufacturer", MANUFACTURER);
            put("ModelName", MODEL_NAME);
            put("FirmwareVersion", FIRMWARE_VERSION);
            put("SerialNumber", SERIAL_NUMBER);
        }};
    }

    protected Map<String, String> fileIndexParamsReverse() {
        return new HashMap<String, String>() {{
            put("Manufacturer", "metadata.manufacturer");
            put("ModelName", "metadata.modelName");
            put("FileType", "metadata.fileType");
        }};
    }

    protected Map<String, String> deviceGroupIndexParamsReverse() {
        return new HashMap<String, String>() {{
            put("Last inform", LAST_INFORM);
            put("Serial Number", SERIAL_NUMBER);
            put("Manufacturer", MANUFACTURER);
            put("Last Inform", LAST_INFORM);
            put("IP", IP);
            put("Firmware version", FIRMWARE_VERSION);
            put("MAC", MAC_ADDRESS);
        }};
    }

    protected Map<String, String> groupareaIndexParams() {
        final String[] params = {};
        String list_msg = messageSource.getMessage("grouptree.column.label", params, LocaleContextHolder.getLocale());
        String[] msg = list_msg.split(",");
        return new HashMap<String, String>() {{
            put("Name", msg[2]);
            put("Parent", msg[1]);
            put("Ranges", msg[0]);
            put("pID", "pID");
            put("ID", "ID");
        }};
    }

    protected Map<String, String> deviceGroupIndexParams() {
        final String[] params = {};
        String list_msg = messageSource.getMessage("groupdevice.column.label", params, LocaleContextHolder.getLocale());
        String[] msg = list_msg.split(",");
        return new LinkedHashMap<String, String>() {{
            put("Id", msg[0]);
            put("Name", msg[1]);
            put("Manufacturer", msg[2]);
            put("Model name", msg[3]);
            put("Firmware/Software version", msg[4]);
            put("Label", msg[5]);
        }};
    }

    protected Map<String, String> deviceDiagnosticsIndexParams() {
        return new LinkedHashMap<String, String>() {{
            put("Id", "Id");
            put("Diagnostics Type", "Diagnostics Type");
            put("Created", "Created");
            put("Completed", "Completed");
        }};
    }

    protected Map<String, String> alarmSettingIndexParams() {
        return new LinkedHashMap<String, String>() {{
            put("Id", "Id");
            put("Alarm Type", "Alarm Type");
            put("Alarm Name", "Alarm Name");
            put("Severity", "Severity");
            put("Group Filter", "Group Filter");
            put("Parameter", "Parameter");
            put("Notify", "Notify");
            put("Monitor", "Monitor");
            put("Aggregated Volume", "Aggregated Volume");
            put("Notify Aggregated", "Notify Aggregated");
        }};
    }

    protected Map<String, String> performanceSettingIndexParams() {
        return new LinkedHashMap<String, String>() {{
            put("Id", "Id");
            put("Traffic Statistic", "Traffic Statistic");
            put("Monitoring CPE", "Monitoring CPE");
            put("Start Date", "Start Date");
            put("End Date", "End Date");
        }};
    }


    protected Map<String, String> deviceDiagnosticsResult() {
        return new LinkedHashMap<String, String>() {{
            put("type", "Diagnostics type");
            put("interface", "Interface");
            put("name", "Name");
            put("host", "Host");
            put("repetitions", "Number of repetitions");
            put("dataSize", "Data Size (byte)");
            put("timeout", "Timeout (ms)");
        }};
    }

    protected Map<String, String> indexParamsDetail() {
        return new HashMap<String, String>() {{
            put(SERIAL_NUMBER, "Serial Number");
            put(MANUFACTURER, "Manufacturer");
            put(PRODUCT_CLASS, "Product Class");
            put(LAST_INFORM, "Last Inform");
            put(OUI, "OUI");
            put(ID, "ID");
            put(MAC_ADDRESS, "Mac Address");
            put(FIRMWARE_VERSION, "Software version");
            put(MODEL_NAME, "Model Name");
            put(PERIODIC_INFORM_INTERVAL, "");
        }};
    }

    protected Map<String, String> deviceNewLogIndexParams() {
        final String[] params = {};
        return new LinkedHashMap<String, String>() {{
            put("Id", "Id");
            put("Name", "Name");
            put("Time", "Time");
            put("Actor", "Actor");
            put("Affected", "Affected");
            put("TaskId", "TaskId");
        }};
    }

    protected Map<String, String> deviceLogIndexParams() {
        final String[] params = {};
        return new LinkedHashMap<String, String>() {{
            put("Id", "Id");
            put("SerialNumber", "SerialNumber");
            put("TaskName", "TaskName");
            put("Parameter", "Parameter");
            put("Created", "Created");
            put("Completed", "Completed");
            put("ErrorCode", "ErrorCode");
            put("ErrorText", "ErrorText");
        }};
    }


    protected Map<String, String> indexParamTasks() {
        return new HashMap<String, String>() {{
            put("name", "Task");
            put("device", "DeviceId");
            put("timestamp", "Time");
            put("fault.detail.Fault.FaultString", "Error");
            put(ID, "ID");
            put("retries", "Retries");
        }};
    }

    protected DeviceTypeVersion deviceTypeVersion(Device device) {
        DeviceTypeVersion version = null;
        DeviceType deviceType = deviceTypeClient.findByPk(device.manufacturer(),
                device.oui(), device.productClass());
        if (!ObjectUtils.empty(deviceType)) {
            version = deviceTypeVersionClient.findByPk(deviceType.id, device.softwareVersion());
        }
        return version;
    }

    protected Map<String, Set<String>> autoCompleteData() {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        map.put("manufacturers", new HashSet<String>());
        map.put("oui", new HashSet<String>());
        map.put("productClass", new HashSet<String>());
        map.put("firmwareVersion", new HashSet<String>());
        DeviceType[] deviceTypes = deviceTypeClient.findAll();
        for (DeviceType type : deviceTypes) {
            map.get("manufacturers").add(type.manufacturer);
            map.get("oui").add(type.oui);
            map.get("productClass").add(type.productClass);
        }
        DeviceTypeVersion[] deviceTypeVersion = deviceTypeVersionClient.findAll();
        for (DeviceTypeVersion typeVersion : deviceTypeVersion) {
            map.get("firmwareVersion").add(typeVersion.firmwareVersion);
        }
        return map;

    }

    protected Device getDevice(String deviceId) {
        Set<String> parameters = new HashSet<String>();
        parameters.addAll(indexParamsDetail().keySet());
        Map<String, String> acsQuery = new HashMap<String, String>();
        acsQuery.put("query", "{\"_id\":\"" + deviceId + "\"}");
        acsQuery.put("parameters", String.join(",", parameters));
        Device device = findDevice(acsQuery, parameters);
        return device;
    }

    private Device findDevice(Map<String, String> query, Set<String> parameters) {
        AcsResponse response = acsApiClient.findDevices(query);
        List<Device> devices = Device.fromJsonString(response.body, parameters);
        Device device = null;
        if (!devices.isEmpty()) {
            device = devices.get(0);
        }
        return device;
    }

    protected void showResult(Model model, Boolean result) {
        if (result) {
            model.addAttribute("status", "success");
            model.addAttribute("notification", "Request Success");
        } else {
            model.addAttribute("status", "error");
            model.addAttribute("notification", "Request Failed");
        }
    }

    public JsonObject checkListComboSearchDataModel(Object listComboSearch) {
        JsonObject response = new JsonObject();
        DeviceTypeVersion[] deviceTypeVersions = null;
        if (listComboSearch == null) {
            deviceTypeVersions = deviceTypeVersionClient.findAll();
            if (deviceTypeVersions.length > 0) {
                for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions) {
                    JsonArray jsonArray = new JsonArray();
                    if (!ObjectUtils.empty(deviceTypeVersion.manufacturer)) {
                        if (response.has(deviceTypeVersion.manufacturer)) {
                            JsonArray asJsonArray = response.getAsJsonArray(deviceTypeVersion.manufacturer);
                            if (!asJsonArray.contains(new JsonParser().parse(deviceTypeVersion.modelName))) {
                                asJsonArray.add(deviceTypeVersion.modelName);
                                response.add(deviceTypeVersion.manufacturer, asJsonArray);
                            }
                        } else {
                            jsonArray.add(deviceTypeVersion.modelName);
                            response.add(deviceTypeVersion.manufacturer, jsonArray);

                        }
                    }
                }
            }
            session.setAttribute("listComboSearch", response.toString());
//            System.out.println("response final DataModel : " + response.toString());
        } else {
            JsonParser parser = new JsonParser();
            response = parser.parse((String) listComboSearch).getAsJsonObject();
//            System.out.println("------------ : " + response.toString());
        }
        return response;
    }


    public JsonObject checkListComboSearchDevice(Object listComboSearch) {
        JsonObject response = new JsonObject();
        DeviceTypeVersion[] deviceTypeVersions = null;
        if (listComboSearch == null) {
            deviceTypeVersions = deviceTypeVersionClient.findAll();
            if (deviceTypeVersions.length > 0) {
                for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions) {
                    JsonArray jsonArray = new JsonArray();
                    if (!ObjectUtils.empty(deviceTypeVersion.manufacturer)) {
                        if (response.has(deviceTypeVersion.manufacturer)) {
                            // add fw vao model name
                            JsonArray asJsonArray = response.getAsJsonArray(deviceTypeVersion.manufacturer);
                            List<String> listjsonObj = new ArrayList<String>();
                            for (int i = 0; i < asJsonArray.size(); i++) {
                                JsonObject asJsonObject = asJsonArray.get(i).getAsJsonObject();
                                for (Map.Entry<String, JsonElement> entry : asJsonObject.entrySet()) {
                                    listjsonObj.add(entry.getKey());
                                }
                            }
                            if (!listjsonObj.contains(deviceTypeVersion.modelName)) {
                                // add fw <- model name chua ton tai
                                JsonObject jsonObject = new JsonObject();
                                JsonArray jsonArrayFW = new JsonArray();
                                jsonArrayFW.add(deviceTypeVersion.firmwareVersion);
                                jsonObject.add(deviceTypeVersion.modelName, jsonArrayFW);
                                asJsonArray.add(jsonObject);
                            } else {
                                // add fw <- model name da ton tai
                                for (int i = 0; i < asJsonArray.size(); i++) {
                                    JsonObject asJsonObject = asJsonArray.get(i).getAsJsonObject();
                                    for (Map.Entry<String, JsonElement> entry : asJsonObject.entrySet()) {
                                        if(entry.getKey().equals(deviceTypeVersion.modelName)){
                                            // add fw vao model name
                                            JsonArray jsonArrayFW = asJsonObject.getAsJsonArray(entry.getKey());
                                            if(!jsonArrayFW.contains(new JsonParser().parse(deviceTypeVersion.firmwareVersion))){
                                                // add fw moi
                                                jsonArrayFW.add(deviceTypeVersion.firmwareVersion);
                                                asJsonObject.add(deviceTypeVersion.modelName, jsonArrayFW);
                                            }

                                        }
                                    }
                                }
                            }

                        } else {
                            // add new model name
                            JsonObject jsonObject = new JsonObject();
                            JsonArray jsonArrayFW = new JsonArray();
                            jsonArrayFW.add(deviceTypeVersion.firmwareVersion);
                            jsonObject.add(deviceTypeVersion.modelName,jsonArrayFW);
                            jsonArray.add(jsonObject);
                            response.add(deviceTypeVersion.manufacturer, jsonArray);

                        }
                    }
                }
            }
            session.setAttribute("listComboSearch", response.toString());
//            System.out.println("response Device : " + response.toString());
        } else {
            JsonParser parser = new JsonParser();
            response = parser.parse((String) listComboSearch).getAsJsonObject();
//            System.out.println("------------ : " + response.toString());
        }
        return response;
    }
}
