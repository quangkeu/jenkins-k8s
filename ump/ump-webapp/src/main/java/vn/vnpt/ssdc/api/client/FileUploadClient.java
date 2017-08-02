package vn.vnpt.ssdc.api.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import vn.vnpt.ssdc.models.Device;
import vn.vnpt.ssdc.utils.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by vietnq on 11/13/16.
 */
@Component
public class FileUploadClient {

    private static final String FIRMWARE_FILE_TYPE = "1 Firmware Upgrade Image";

    private static final Logger logger = LoggerFactory.getLogger(FileUploadClient.class);

    @Value("${fileEndpointUrl}")
    private String fileEndpointUrl;

    @Value("${acsEndpointUrl}")
    private String acsEndpointUrl;

    @Autowired
    private RestTemplate restTemplate;

    public Boolean uploadFirmware(MultipartFile file, String name,
                                  String oui, String productClass, String version) {
        Boolean success = true;
        String url = String.format("%s/%s", fileEndpointUrl, name);
        HttpHeaders headers = new HttpHeaders();
        headers.add("fileType", FIRMWARE_FILE_TYPE);
        headers.add("oui", oui);
        headers.add("productClass", productClass);
        headers.add("version", version);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        try {
            URI uri = new URI(url);
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("file", file.getBytes());

            RequestEntity<MultiValueMap<String, Object>> requestEntity = new RequestEntity<>(map, headers, HttpMethod.PUT, uri);
            ResponseEntity<String> responseEntity = this.restTemplate.exchange(requestEntity, String.class);
            if (responseEntity.getStatusCodeValue() != 201) {
                success = false;
            }
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
            logger.error("Cannot instantiate uri from url {}", url);
            success = false;
        } catch (IOException e) {
            logger.error(e.getMessage());
            logger.error("Cannot get bytes from file {}", name);
            success = false;
        }  catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("Cannot get bytes from file {}", name);
            success = false;
        }
        return success;
    }

    public String getFirmwareFileIdByFileName(String filename) {
        Map<String, String> queryParams = new HashMap();
        queryParams.put("filename", filename);
        String queryString = StringUtils.queryStringFromMap(queryParams);
        String url = this.fileEndpointUrl + "?" + queryString;
        try {
            String firmwareFileId = "";
            String resultJson = this.restTemplate.getForObject(url, String.class, queryParams);
            JsonElement jelement = new JsonParser().parse(resultJson);
            JsonArray jonArray = jelement.getAsJsonArray();
            for (int i = 0; i < jonArray.size(); i++) {
                JsonObject jsonObject = jonArray.get(i).getAsJsonObject();
                String fileNameJsonObject = jsonObject.get("filename").getAsString();
                if (filename.equals(fileNameJsonObject)) {
                    firmwareFileId = jsonObject.get("_id").getAsString();
                    break;
                }
            }
            return firmwareFileId;
        } catch (RestClientException e) {
            logger.error(e.getMessage());
            return "";
        }
    }

    public Map<String, String> getListFileVersions(String deviceId, String type) {
        Map<String, String> listFileVersions = new LinkedHashMap<>();
        Map<String, String> queryParams = new HashMap();
//        queryParams.put("filename", filename);
        String queryString = StringUtils.queryStringFromMap(queryParams);
        String url = this.fileEndpointUrl + "?" + queryString;
        try {
            String resultJson = this.restTemplate.getForObject(url, String.class, queryParams);
            JsonElement jelement = new JsonParser().parse(resultJson);
            JsonArray jonArray = jelement.getAsJsonArray();
            for (int i = 0; i < jonArray.size(); i++) {
                try {
                    JsonObject jsonObject = jonArray.get(i).getAsJsonObject();
                    String oui = jsonObject.get("metadata").getAsJsonObject().get("oui").getAsString();
                    String productClass = jsonObject.get("metadata").getAsJsonObject().get("productClass").getAsString();
                    String fileType = jsonObject.get("metadata").getAsJsonObject().get("fileType").getAsString();
                    if (oui.equals(deviceId.split("-")[0]) && productClass.equals(deviceId.split("-")[1])
                            && fileType.equals(type)) {
                        listFileVersions.put(jsonObject.get("_id").getAsString(), jsonObject.get("metadata").getAsJsonObject().get("version").getAsString());
                    }
                } catch (Exception ex) {}
            }
        } catch (RestClientException e) {
            logger.error("getListFileVersions  ", e.getMessage());
        }
        return listFileVersions;
    }

    public Boolean uploadFile(MultipartFile file, String name,
                                  String manufacturer, String modelName, String fileType, String version,
                                  String oui, String productClass, String uploadFileName) {
        Boolean success = true;
        String url = String.format("%s/%s", fileEndpointUrl, name);
        HttpHeaders headers = new HttpHeaders();
        headers.add("filetype", fileType);
        headers.add("manufacturer", manufacturer);
        headers.add("modelName", modelName);
        headers.add("version", version);
        headers.add("oui", oui);
        headers.add("productclass", productClass);
        headers.add("uploadFileName", uploadFileName);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        try {
            URI uri = new URI(url);
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("files", file.getBytes());

            RequestEntity<MultiValueMap<String, Object>> requestEntity = new RequestEntity<>(map, headers, HttpMethod.PUT, uri);
            ResponseEntity<String> responseEntity = this.restTemplate.exchange(requestEntity, String.class);
            if (responseEntity.getStatusCodeValue() != 201) {
                success = false;
            }
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
            logger.error("Cannot instantiate uri from url {}", url);
            success = false;
        } catch (IOException e) {
            logger.error(e.getMessage());
            logger.error("Cannot get bytes from file {}", name);
            success = false;
        } catch (Exception e) {
            logger.error(e.getMessage());
            success = false;
        }

        return success;
    }

    public Boolean updateFile(String name,
                                  String manufacturer, String modelName, String fileType, String version,
                                  String oui, String productClass) {
        Boolean success = true;
        String url = String.format("%s/%s", this.acsEndpointUrl + "/update-files", name);
        HttpHeaders headers = new HttpHeaders();
        headers.add("filetype", fileType);
        headers.add("manufacturer", manufacturer);
        headers.add("modelname", modelName);
        headers.add("version", version);
        headers.add("oui", oui);
        headers.add("productclass", productClass);

        try {
            URI uri = new URI(url);
            RequestEntity<MultiValueMap<String, Object>> requestEntity = new RequestEntity<>(headers, HttpMethod.PUT, uri);
            ResponseEntity<String> responseEntity = this.restTemplate.exchange(requestEntity, String.class);
            if (responseEntity.getStatusCodeValue() != 200) {
                success = false;
            }
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
            logger.error("Cannot instantiate uri from url {}", url);
            success = false;
        } catch (Exception e) {
            logger.error(e.getMessage());
            success = false;
        }

        return success;
    }



}
