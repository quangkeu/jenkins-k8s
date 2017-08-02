package vn.vnpt.ssdc.models;

import vn.vnpt.ssdc.api.client.DeviceTypeClient;
import vn.vnpt.ssdc.api.model.DeviceType;
import vn.vnpt.ssdc.api.model.DeviceTypeVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds information to display device types with firmware version <br/>
 * Tree format:
 * -Manufacturer + OUI (level 1)
 * --Name + Product Class (level 2)
 * ---Firmare Version (level 3)
 * <p>
 * //TODO use cache and initialize when app starts
 * Created by vietnq on 11/6/16.
 */
public class DeviceTypeTree {
    private DeviceTypeClient deviceTypeClient;

    public Map<String, List<DeviceType>> productClassesMap;
    public Map<Long, List<DeviceTypeVersion>> deviceVersionsMap;
    public Boolean hasVersionNode;
    public DeviceType firstDeviceType;
    public DeviceTypeVersion firstDeviceTypeVersion;

    public DeviceTypeTree() {
        this.productClassesMap = new LinkedHashMap<String, List<DeviceType>>();
        this.deviceVersionsMap = new LinkedHashMap<Long, List<DeviceTypeVersion>>();
    }

    public DeviceTypeTree(DeviceTypeClient client, Boolean hasVersionNode) {
        this();
        this.deviceTypeClient = client;
        this.hasVersionNode = hasVersionNode;
    }


    public void load() {
        DeviceType[] deviceTypes = deviceTypeClient.findAll();

        if (deviceTypes.length > 0) {
            firstDeviceType = deviceTypes[0];
        }

        for (DeviceType deviceType : deviceTypes) {
            String keyL1 = deviceType.manufacturer + " - " + deviceType.oui;
            if (!productClassesMap.containsKey(keyL1)) {
                productClassesMap.put(keyL1, new ArrayList<DeviceType>());
            }
            productClassesMap.get(keyL1).add(deviceType);

            if (hasVersionNode) {
                DeviceTypeVersion[] versions = deviceTypeClient.findVersions(deviceType.id);
                if (deviceType.id.equals(firstDeviceType.id) && versions.length > 0) {
                    firstDeviceTypeVersion = versions[0];
                }
                if (firstDeviceTypeVersion == null && versions.length > 0) {
                    firstDeviceTypeVersion = versions[0];
                }

                sortVersion(versions);
                for (DeviceTypeVersion version : versions) {
                    if (!deviceVersionsMap.containsKey(deviceType.id)) {
                        deviceVersionsMap.put(deviceType.id, new ArrayList<DeviceTypeVersion>());
                    }
                    deviceVersionsMap.get(deviceType.id).add(version);
                }
            }
        }
    }

    public void loadTreeByDeviceType(DeviceType oneDeviceType) {

        DeviceType[] deviceTypes = new DeviceType[1];
        deviceTypes[0] = oneDeviceType;

        if (deviceTypes.length > 0) {
            firstDeviceType = deviceTypes[0];
        }

        for (DeviceType deviceType : deviceTypes) {
            String keyL1 = deviceType.manufacturer + " - " + deviceType.oui;
            if (!productClassesMap.containsKey(keyL1)) {
                productClassesMap.put(keyL1, new ArrayList<DeviceType>());
            }
            productClassesMap.get(keyL1).add(deviceType);

            if (hasVersionNode) {
                DeviceTypeVersion[] versions = deviceTypeClient.findVersions(deviceType.id);
                if (deviceType.id.equals(firstDeviceType.id) && versions.length > 0) {
                    firstDeviceTypeVersion = versions[0];
                }
                if (firstDeviceTypeVersion == null && versions.length > 0) {
                    firstDeviceTypeVersion = versions[0];
                }
                for (DeviceTypeVersion version : versions) {
                    if (!deviceVersionsMap.containsKey(deviceType.id)) {
                        deviceVersionsMap.put(deviceType.id, new ArrayList<DeviceTypeVersion>());
                    }
                    deviceVersionsMap.get(deviceType.id).add(version);
                }
            }
        }
    }

    private void sortVersion(DeviceTypeVersion[] versions) {
        Arrays.sort(versions, new Comparator<DeviceTypeVersion>() {
            @Override
            public int compare(DeviceTypeVersion o1, DeviceTypeVersion o2) {
                return o1.created < o2.created ? -1 : 1;
            }
        });
    }
}
