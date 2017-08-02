package vn.vnpt.ssdc.api.model;

import org.springframework.beans.factory.annotation.Autowired;
import vn.vnpt.ssdc.api.client.AcsApiClient;
import vn.vnpt.ssdc.models.FileManagement;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoggingDeviceActivity extends SsdcEntity<String> {
    public String taskId;
    public String taskName;
    public String parameter;
    public String createdTime;
    public String completedTime;
    public String errorCode;
    public String errorText;

    public String taskNameView;
    public String parameterView;

    public void convertDataToView(AcsApiClient acsApiClient) {

        switch (taskName) {
            case "reboot":
                taskNameView = "Reboot";
                parameterView = "Reboot";
                break;
            case "factoryReset":
                taskNameView = "Factory Reset";
                parameterView = "Factory Reset";
                break;
            case "addObject":
                taskNameView = "Add Object";
                parameterView = parameter;
                break;
            case "deleteObject":
                taskNameView = "Delete Object";
                parameterView = parameter;
                break;
            case "refreshObject":
                taskNameView = "Refresh Object";
                parameterView = "Refresh Object";
                break;
            case "getParameterValues":
                // Template value: ["InternetGatewayDevice.ManagementServer.ConnectionRequestURL","InternetGatewayDevice.ManagementServer.URL","InternetGatewayDevice.ManagementServer.Username","InternetGatewayDevice.ManagementServer.Password"]
                taskNameView = "Refresh parameter";
                if (parameter != null) {
                    parameterView = parameter.replaceAll("[\\]\\[\"]", "");
                    parameterView = parameterView.replaceAll("[,]", ", ");
                }
                break;
            case "setParameterValues":
                // Template value: [["InternetGatewayDevice.ManagementServer.ConnectionRequestPassword","root","xsd:string"],["InternetGatewayDevice.ManagementServer.Username","root","xsd:string"],["InternetGatewayDevice.ManagementServer.ConnectionRequestUsername","root","xsd:string"],["InternetGatewayDevice.ManagementServer.Password","root","xsd:string"]]
                taskNameView = "Update Parameter";
                if (parameter != null) {
                    try {
                        Set<String> paths = new LinkedHashSet<>();
                        for (String subParameter : parameter.split("],")) {
                            int startStr = subParameter.indexOf("\"") + 1;
                            int endStr = subParameter.indexOf("\"", startStr);
                            String currentPath = subParameter.substring(startStr, endStr).trim();
                            paths.add(currentPath);
                        }
                        parameterView = String.join(", ", paths);
                    } catch (Exception e) {
                        parameterView = "Undefined";
                        e.printStackTrace();
                    }
                }
                break;
            case "download":
                // Template value: ["url","http://wiki.vnpt-technology.vn","1 Firmware Upgrade Image"]
                // Template value: ["file","59538b1ec8b6495a2bb3a1e6"]
                taskNameView = "Download";
                if (parameter != null) {
                    try {
                        if (parameter.contains("\"url\"")) {
                            String[] paths = parameter.split("\",");
                            parameterView = paths[1].substring(
                                    paths[1].indexOf("\"") + 1,
                                    paths[1].length()).trim();

                            taskNameView = paths[2].substring(
                                    paths[2].indexOf("\"") + 1,
                                    paths[2].lastIndexOf("\"")).trim();
                            taskNameView = taskNameView.replace(taskNameView.substring(0, taskNameView.indexOf(" ")), "Download");

                        } else if (parameter.contains("\"file\"")) {
                            String[] paths = parameter.split("\",");
                            String filename = paths[1].substring(
                                    paths[1].indexOf("\"") + 1,
                                    paths[1].lastIndexOf("\"")).trim();

                            Map<String, String> indexParams = new LinkedHashMap<String, String>() {{
                                put("_id", "id");
                                put("filename", "filename");
                                put("metadata.fileType", "fileType");
                            }};
                            Map<String, String> acsQuery = new HashMap<>();
                            acsQuery.put("query",  String.format("{\"filename\":\"%s\"}", filename));
                            acsQuery.put("parameters", String.join(",", indexParams.keySet()));
                            AcsResponse response = acsApiClient.getAllFiles(acsQuery);
                            List<FileManagement> fileManagements = FileManagement.fromJsonString(response.body, indexParams.keySet());
                            if (fileManagements.size() > 0) {
                                parameterView = fileManagements.get(0).parameters.get("filename");
                                taskNameView = fileManagements.get(0).parameters.get("metadata.fileType");
                                taskNameView = taskNameView.replace(taskNameView.substring(0, taskNameView.indexOf(" ")), "Download");
                            } else {
                                parameterView = "Undefined";
                            }

                        }
                    } catch (Exception e) {
                        parameterView = "Undefined";
                        e.printStackTrace();
                    }
                }
                break;
            case "upload":
                taskNameView = "Upload";
                if (parameter != null) {
                    // ["http://10.84.20.139:7557/backup-files/1-1500430831972-G4.16A.03RC3-a06518-968380GERG-VNPT00a532c2","1 Vendor Configuration File"]
                    String[] paths = parameter.split("\",");
                    try {
                        parameterView = paths[0].substring(
                                paths[0].indexOf("\"") + 1,
                                paths[0].length()).trim();

                        taskNameView = paths[1].substring(
                                paths[1].indexOf("\"") + 1,
                                paths[1].lastIndexOf("\"")).trim();
                        taskNameView = taskNameView.replace(taskNameView.substring(0, taskNameView.indexOf(" ")), "Upload");
                    } catch (Exception e) {
                        parameterView = "Undefined";
                        e.printStackTrace();
                    }
                }
                break;
            default:
                taskNameView = taskName;
                parameterView = parameter;
                break;
        }

    }

}