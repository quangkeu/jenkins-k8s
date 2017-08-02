package vn.vnpt.ssdc.api.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by SSDC on 11/14/2016.
 */
public class Diagnostic extends SsdcEntity<String> {

    public enum DIAGNOSTIC_TYPE {
        PING,
        TRACERT,
        TESTDOWNLOAD,
        TESTUPLOAD
    }

    private static final Logger logger = LoggerFactory.getLogger(Diagnostic.class);
    private static final String VALUE_KEY = "value";

    public String _id;

    public Map<String, String> parameters = new HashMap<>(); //holds device (parameter,value) mapping
    public Map<String, String> request;
    public String diagnosticsName;
    public String deviceId;

    public static Diagnostic fromJsonString(JsonArray array, DIAGNOSTIC_TYPE type) {
        Diagnostic diagnostic = new Diagnostic();
        if (DIAGNOSTIC_TYPE.PING.equals(type)) {
            diagnostic.parameters.put("InternetGatewayDevice.IPPingDiagnostics.DiagnosticsState", "Requested");
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                if (object.get("name").toString().contains("_diagnosticInterface")) {
                    diagnostic.parameters.put("InternetGatewayDevice.IPPingDiagnostics.Interface", object.get(VALUE_KEY) != null ? object.get(VALUE_KEY).getAsString() : "");
                    if (object.get(VALUE_KEY).toString().contains("Any Avaiable")) {
                        diagnostic.parameters.remove("InternetGatewayDevice.IPPingDiagnostics.Interface");
                    }
                }
                if (object.get("name").toString().contains("_diagnosticHost"))
                    diagnostic.parameters.put("InternetGatewayDevice.IPPingDiagnostics.Host", object.get(VALUE_KEY) != null ? object.get(VALUE_KEY).getAsString() : "");
                if (object.get("name").toString().contains("_diagnosticTimeOut"))
                    diagnostic.parameters.put("InternetGatewayDevice.IPPingDiagnostics.Timeout", object.get(VALUE_KEY) != null ? object.get(VALUE_KEY).getAsString() : "");
                if (object.get("name").toString().contains("_diagnosticBlockSize"))
                    diagnostic.parameters.put("InternetGatewayDevice.IPPingDiagnostics.DataBlockSize", object.get(VALUE_KEY) != null ? object.get(VALUE_KEY).getAsString() : "");
                if (object.get("name").toString().contains("_diagnosticNumberOfRepetitions"))
                    diagnostic.parameters.put("InternetGatewayDevice.IPPingDiagnostics.NumberOfRepetitions", object.get(VALUE_KEY) != null ? object.get(VALUE_KEY).getAsString() : "");
                if (object.get("name").toString().contains("deviceId"))
                    diagnostic._id = object.get(VALUE_KEY).getAsString();
            }
        }
        return diagnostic;
    }

    public static Diagnostic getResultDiagnostic(String input, DIAGNOSTIC_TYPE type) {
        Diagnostic diagnostic = new Diagnostic();
        if (DIAGNOSTIC_TYPE.PING.equals(type)) {
            String[] nameObject = {
                    "DiagnosticsState",
                    "Interface",
                    "Host",
                    "NumberOfRepetitions",
                    "Timeout",
                    "DataBlockSize",
                    "DSCP",
                    "SuccessCount",
                    "FailureCount",
                    "AverageResponseTime",
                    "MinimumResponseTime",
                    "MaximumResponseTime"};
            JsonArray array = new Gson().fromJson(input, JsonArray.class);
            JsonObject object1 = array.get(0).getAsJsonObject();
            JsonObject ping = object1.get("InternetGatewayDevice").getAsJsonObject().get("IPPingDiagnostics").getAsJsonObject();
            for (int i = 0; i < nameObject.length; i++) {
                if (ping.get(nameObject[i]).getAsJsonObject().get("_value") != null) {
                    diagnostic.parameters.put(nameObject[i], ping.get(nameObject[i]).getAsJsonObject().get("_value").getAsString());
                }
            }
        }
        logger.info("Diagnostic result: {}", diagnostic.toString());
        return diagnostic;
    }

}
