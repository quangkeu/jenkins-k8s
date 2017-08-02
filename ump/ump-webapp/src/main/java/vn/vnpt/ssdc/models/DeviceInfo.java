package vn.vnpt.ssdc.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Lamborgini on 3/2/2017.
 */
public class DeviceInfo {
    private static final Logger logger = LoggerFactory.getLogger(Device.class);

    public Map<String, String> parameters = new HashMap<>();

    public boolean isOnline() {
        Boolean result = true;
        String lastInform = parameters.get("_lastInform");
        if("".equals(parameters.get("summary.periodicInformInterval"))){
            return true;
        }
        int periodicInformInterval = Integer.valueOf(parameters.get("summary.periodicInformInterval"));
        if(lastInform.isEmpty()) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date lastInformTime = sdf.parse(lastInform);
            Date now = new Date();
            if ((now.getTime() - lastInformTime.getTime()) > 1000*periodicInformInterval) {
                result = false;
            }
        } catch (ParseException e) {
            logger.error("Cannot parse time: {}", lastInform);
        }
        return result;
    }

    public String getModelName() {
        return parameters.get("modelName");
    }

    public String getHardwareVersion() {
        return parameters.get("hardwareVersion");
    }

    public String getFirmwareVersion() {
        return parameters.get("firmwareVersion");
    }

    public String getUptime() {
        return parameters.get("uptime");
    }

    public String getManufacturer() {
        return parameters.get("manufacturer");
    }

    public String getManufacturerOUI() {
        return parameters.get("manufacturerOUI");
    }

    public String getProductClass() {
        return parameters.get("productClass");
    }

    public String getSerialNumber() {
        return parameters.get("serialNumber");
    }

    public String getConnectionRequestURL() {
        return parameters.get("connectionRequestURL");
    }

    public String getRegistrationDate() {
        return parameters.get("registrationDate");
    }

    public String getLastConnection() {
        return parameters.get("lastConnection");
    }

    public String getMacAddress() {
        return parameters.get("macAddress");
    }
}
