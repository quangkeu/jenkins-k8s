package vn.vnpt.ssdc.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import vn.vnpt.ssdc.api.model.Filter;
import vn.vnpt.ssdc.models.DevicePaginator;
import vn.vnpt.ssdc.models.FileManagementPaginator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static vn.vnpt.ssdc.controllers.BaseController.*;

/**
 * Created by vietnq on 10/25/16.
 */
public class StringUtils {

    private static Gson gson = new Gson();
    private static String queryAND = "#";
    private static String queryOR = "@";

    /**
     * returns url query with placeholders to use when call RestTemplate
     *
     * @param map a Map, for ex: {"limit":"5","offset":"0"}
     * @return url query, for ex: limit={limit}&offset={offset}
     */
    public static String queryStringFromMap(Map<String, String> map) {
        if (map.size() > 0) {
            StringBuilder sb = new StringBuilder("");
            for (String key : map.keySet()) {
                sb.append(String.format("&%s={%s}", key, key));
            }
            //remove first "&" char
            return sb.substring(1);
        }
        return "";

    }

    //TODO handle operators ($gt, $eq, ...)
    public static String advancedQuery(Map<String, String> searchOptions) {
        JsonObject query = new JsonObject();
        JsonArray queryElements = new JsonArray();
        for (String key : searchOptions.keySet()) {
            JsonObject el = new JsonObject();
            el.addProperty(key, String.format("/%s/", searchOptions.get(key))); //like %value%
            queryElements.add(el);
        }
        query.add("$and", queryElements);
        return gson.toJson(query);
    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean checkParameterSearch(Map<String, String> searchParams) {
        for (Map.Entry<String, String> key : searchParams.entrySet()) {
            if (!("").equals(key.getValue())) {
                return true;
            }
        }
        return false;
    }

    //creat query Search
    public static String querySearchObject(Map<String, String> searchParams, Map<String, String> deviceIndex) {
        if (searchParams.size() > 0) {
            String prefix = searchParams.get("prefix");
            searchParams.remove("prefix");

            StringBuilder sb = new StringBuilder("{");
            for (Map.Entry<String, String> key : searchParams.entrySet()) {
                if (!("").equals(key.getValue())) {
                    if (("onlineState").equals(key.getKey())) {
                        if (("Online").equals(key.getValue())) {
                            sb.append(String.format(",\"%s\":%s", "_lastInform", "{\"$gt\":\"" + getIsoDate() + "\"}"));
                        } else if (("Offline").equals(key.getValue())) {
                            sb.append(String.format(",\"%s\":%s", "_lastInform", "{\"$lt\":\"" + getIsoDate() + "\"}"));
                        }
                    } else
                        sb.append(String.format(",\"%s\":\"/%s/\"", deviceIndex.get(key.getKey()), searchParams.get(key.getKey())));
                }
            }

            if (null != prefix && !("").equals(prefix)) {
                StringBuilder sb1 = new StringBuilder(sb.toString());
                StringBuilder sb2 = new StringBuilder("{\"$or\":[");
                sb1.append(String.format(",\"%s\":\"/%s/\"", "InternetGatewayDevice.ManagementServer.ConnectionRequestURL._value", prefix));
                sb.append(String.format(",\"%s\":\"/%s/\"", "_deviceId._SerialNumber", prefix));
                sb.append("}");
                sb.deleteCharAt(1);
                sb1.append("}");
                sb1.deleteCharAt(1);
                sb2.append(sb.toString());
                sb2.append("," + sb1.toString() + "]}");
                sb = new StringBuilder(sb2.toString());
            } else {
                sb.append("}");
                sb.deleteCharAt(1);
                return sb.toString();
            }
            return sb.toString();
        }
        return "";
    }

    //creat query device group
    public static String querySearchDeviceGroup(Filter filters, Map<String, String> deviceIndex) {
        boolean flag = false;
        boolean exitOR = false;
        boolean firstOR = false;
        StringBuilder sb = new StringBuilder("{");
        StringBuilder sb2 = new StringBuilder("{\"$or\":[");
        sb.append(String.format("\"%s\":\"/%s/\"", "_deviceId._OUI", filters.getOui()));
        sb.append(String.format(",\"%s\":\"/%s/\"", "_deviceId._ProductClass", filters.getProductClass()));
        if (!("").equals(filters.getValue().trim())) {
            String[] columns = filters.getColumnName().split(",");
            String[] values = filters.getValue().split(",");
            String[] compares = filters.getCompare().split(",");
            String[] operators = filters.getOperator().split(",");
            for (int i = 0; i < columns.length; i++) {
                if (("AND").equals(operators[i])) {
                    sb.append(createByCondition(compares[i], values[i], columns[i], deviceIndex));
                    flag = true;
                }
            }
            for (int i = 0; i < columns.length; i++) {
                if (("OR").equals(operators[i])) {
                    if (!flag) {
                        StringBuilder sb1 = new StringBuilder(sb.toString());
                        sb1.append(createByCondition(compares[i], values[i], columns[i], deviceIndex));
                        sb1.append("}");
                        if (!firstOR) {
                            sb2.append(sb1.toString());
                        } else {
                            sb2.append("," + sb1.toString());
                        }
                        if (i == columns.length - 1) {
                            sb2.append("]}");
                            return sb2.toString();
                        }
                        firstOR = true;
                    } else {
                        StringBuilder sb1 = new StringBuilder(sb.toString());
                        sb1.append(String.format(",\"%s\":\"/%s/\"", deviceIndex.get(columns[i]), values[i]));
                        sb1.append("}");
                        if (!exitOR) sb2.append(sb1.toString());
                        else sb2.append("," + sb1.toString());
                        exitOR = true;
                    }
                }
            }
        }
        if (exitOR) {
            sb2.append("]}");
            return sb2.toString();
        }
        sb.append("}");
//        sb.deleteCharAt(1);
        sb2.append(sb.toString());
        sb2.append("]}");
        return sb2.toString();
    }

    public static String createByCondition(String compare, String condition, String column, Map<String, String> deviceIndex) {
        String query = String.format(",\"%s\":%s", deviceIndex.get(column), "{\"$gt\":\"" + condition + "\"}");
        if (("<").equals(compare)) query = query.replace("$gt", "$lt");
        else if (("<=").equals(compare)) query = query.replace("$gt", "$lte");
        else if ((">=").equals(compare)) query = query.replace("$gt", "$gte");
        else if (("#").equals(compare)) query = query.replace("$gt", "$ne");
        else if (("=").equals(compare)) query = String.format(",\"%s\":\"/%s/\"", deviceIndex.get(column), condition);
        return query;
    }

    public static String decodeUTF8PostBody(String body) throws UnsupportedEncodingException {
        return URLDecoder.decode(body, "UTF-8");

    }

    private static String getIsoDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        Date nowTime = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(nowTime);
    }

    public static String queryDeviceSearchObject(Map<String, String> searchParams, Map<String, String> deviceIndex) {
        StringBuilder sb = new StringBuilder("{");
        if (searchParams.get(DevicePaginator.PAGE_MANUFACTURER) != null
                && !searchParams.get(DevicePaginator.PAGE_MANUFACTURER).equals("")) {
            sb.append(String.format(",\"%s\":\"/%s/\"", deviceIndex.get(DevicePaginator.PAGE_MANUFACTURER), searchParams.get(DevicePaginator.PAGE_MANUFACTURER)));
        }
        if (searchParams.get(DevicePaginator.PAGE_MODEL_NAME) != null
                && !searchParams.get(DevicePaginator.PAGE_MODEL_NAME).equals("")) {
            sb.append(String.format(",\"%s\":\"/%s/\"", deviceIndex.get(DevicePaginator.PAGE_MODEL_NAME), searchParams.get(DevicePaginator.PAGE_MODEL_NAME)));
        }
        if (searchParams.get(DevicePaginator.PAGE_FIRMWARE_VERSION) != null
                && !searchParams.get(DevicePaginator.PAGE_FIRMWARE_VERSION).equals("")) {
            sb.append(String.format(",\"%s\":\"/%s/\"", deviceIndex.get(DevicePaginator.PAGE_FIRMWARE_VERSION), searchParams.get(DevicePaginator.PAGE_FIRMWARE_VERSION)));
        }
        if (searchParams.get(DevicePaginator.PAGE_SERIAL_NUMBER) != null
                && !searchParams.get(DevicePaginator.PAGE_SERIAL_NUMBER).equals("")) {
            sb.append(String.format(",\"%s\":\"/%s/\"", deviceIndex.get(DevicePaginator.PAGE_SERIAL_NUMBER), searchParams.get(DevicePaginator.PAGE_SERIAL_NUMBER)));
        }
        if (!sb.toString().equals("{")) {
            sb.deleteCharAt(1);
        }
        sb.append("}");
        return sb.toString();
    }

    public static String queryDeviceSearchFull(Map<String, String> searchParams, Map<String, String> deviceIndex) {
        StringBuilder sb = new StringBuilder("{\"$or\":[");
        for (Map.Entry<String, String> entry : deviceIndex.entrySet()) {
            sb.append(String.format(",{\"%s\":\"/%s/\"}", entry.getKey(), searchParams.get(DevicePaginator.PAGE_SEARCH_ALL)));
        }
        if (!sb.toString().equals("{")) {
            sb.deleteCharAt(8);
        }
        sb.append("]}");
        return sb.toString();
    }

    public static String queryDeviceSearchLabel(Map<String, String> searchParams, Map<String, String> stringStringMap) {
        StringBuilder sb = new StringBuilder("{\"$and\":[");
        if (searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL) != null
                && !searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL).equals("")) {

            if (!searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL).contains(" AND ")
                    && !searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL).contains(" OR ")) {
                // ko and ko or
                sb = new StringBuilder("{\"$or\":[");
                String label = searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL);
                label = label.replaceAll("\"", "");
                sb.append(String.format(",{\"_tags\":\"%s\"}", label));
                if (!("").equals(searchParams.get(OUI))) sb.append(String.format(",{\"%s\":\"%s\"}", OUI, searchParams.get(OUI)));
                if (!("").equals(searchParams.get(PRODUCT_CLASS))) sb.append(String.format(",{\"%s\":\"%s\"}", PRODUCT_CLASS, searchParams.get(PRODUCT_CLASS)));
                sb.deleteCharAt(8);
                sb.append("]}");
            } else if (searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL).contains(" AND ")
                    && !searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL).contains(" OR ")) {
                // co and
                String label = searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL);
                label = label.replaceAll(" AND ", queryAND).replaceAll("\"", "");
                String[] labelArray = label.split(queryAND);
                if (labelArray.length > 0) {
                    for (int i = 0; i < labelArray.length; i++) {
                        sb.append(String.format(",{\"_tags\":\"%s\"}", labelArray[i]));
                    }
                    if (!("").equals(searchParams.get(OUI))) sb.append(String.format(",{\"%s\":\"%s\"}", OUI, searchParams.get(OUI)));
                    if (!("").equals(searchParams.get(PRODUCT_CLASS))) sb.append(String.format(",{\"%s\":\"%s\"}", PRODUCT_CLASS, searchParams.get(PRODUCT_CLASS)));
                    sb.deleteCharAt(9);
                    sb.append("]}");
                }
            } else if (!searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL).contains(" AND ")
                    && searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL).contains(" OR ")) {
                // co or
                sb = new StringBuilder("{\"$or\":[");
                String label = searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL);
                label = label.replaceAll(" OR ", queryOR).replaceAll("\"", "");
                String[] labelArray = label.split(queryOR);
                if (labelArray.length > 0) {
                    for (int i = 0; i < labelArray.length; i++) {
                        sb.append(String.format(",{\"_tags\":\"%s\"}", labelArray[i]));
                    }
                    if (!("").equals(searchParams.get(OUI))) sb.append(String.format(",{\"%s\":\"%s\"}", OUI, searchParams.get(OUI)));
                    if (!("").equals(searchParams.get(PRODUCT_CLASS))) sb.append(String.format(",{\"%s\":\"%s\"}", PRODUCT_CLASS, searchParams.get(PRODUCT_CLASS)));
                    sb.deleteCharAt(8);
                    sb.append("]}");
                }
            } else if (searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL).contains(" AND ")
                    && searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL).contains(" OR ")) {
                // ca and or
                String label = searchParams.get(DevicePaginator.PAGE_SEARCH_LABEL);
                label = label.replaceAll(" AND ", queryAND);
                label = label.replaceAll(" OR ", queryOR);

                if (label.contains("\"")) {
                    label = label.replaceAll("\"", "");
                }
                StringBuilder sb1 = new StringBuilder(",\"$or\":[");
                String totalORTmp = "";

                String[] partAND = label.split(queryAND);
                for (int i = 0; i < partAND.length; i++) {
                    if (partAND[i].contains(queryOR)) {
                        totalORTmp += "," + partAND[i];
                    } else {
                        sb.append(String.format(",{\"_tags\":\"%s\"}", partAND[i]));
                        if (!("").equals(searchParams.get(OUI))) sb.append(String.format(",{\"%s\":\"%s\"}", OUI, searchParams.get(OUI)));
                        if (!("").equals(searchParams.get(PRODUCT_CLASS))) sb.append(String.format(",{\"%s\":\"%s\"}", PRODUCT_CLASS, searchParams.get(PRODUCT_CLASS)));
                    }
                }

                String[] split = totalORTmp.substring(1).split(",");
                for (int i = 0; i < split.length; i++) {
                    String[] partOR = split[i].split(queryOR);
                    for (int j = 0; j < partOR.length; j++) {
                        String abc = queryAND + partOR[j].replaceAll(",", "");
                        if (label.contains(abc)) {
                            sb.append(String.format(",{\"_tags\":\"%s\"}", partOR[j]));
                        } else {
                            sb1.append(String.format(",{\"_tags\":\"%s\"}", partOR[j]));
                        }
                    }
                }

                sb1.deleteCharAt(8);
                sb.append("]");
                sb1.append("]");
                sb.append(sb1.toString());
                sb.deleteCharAt(9);
                sb.append("}");

                System.out.println("Total : " + sb.toString());

            }
        } else {
            sb = new StringBuilder("{}");
        }
        return sb.toString();
    }

    public static String queryFileManagement(Map<String, String> searchParams, Map<String, String> deviceIndex) {
        StringBuilder sb = new StringBuilder("");
        if (searchParams.get(FileManagementPaginator.PAGE_MANUFACTURER) != null
                && !searchParams.get(FileManagementPaginator.PAGE_MANUFACTURER).equals("")) {
            sb.append(String.format(",\"%s\":\"%s\"", deviceIndex.get(FileManagementPaginator.PAGE_MANUFACTURER), searchParams.get(FileManagementPaginator.PAGE_MANUFACTURER)));
        }
        if (searchParams.get(FileManagementPaginator.PAGE_MODEL_NAME) != null
                && !searchParams.get(FileManagementPaginator.PAGE_MODEL_NAME).equals("")) {
            sb.append(String.format(",\"%s\":\"%s\"", deviceIndex.get(FileManagementPaginator.PAGE_MODEL_NAME), searchParams.get(FileManagementPaginator.PAGE_MODEL_NAME)));
        }
        if (searchParams.get(FileManagementPaginator.PAGE_FILE_TYPE) != null
                && !searchParams.get(FileManagementPaginator.PAGE_FILE_TYPE).equals("")) {
            sb.append(String.format(",\"%s\":\"%s\"", deviceIndex.get(FileManagementPaginator.PAGE_FILE_TYPE), searchParams.get(FileManagementPaginator.PAGE_FILE_TYPE)));
        }
        if (!sb.toString().equals("")) {
            sb.deleteCharAt(0);
        } else {
            sb.append("{}");
        }
//        sb.append("}");
        return sb.toString();
    }

    public static String queryDeleteDM(String params) {
        String[] split = params.split("#");
        StringBuilder sb = new StringBuilder("{");
        sb.append(String.format("\"%s\":\"/%s/\"", "_deviceId._OUI", split[0]));
        sb.append(String.format(",\"%s\":\"/%s/\"", "_deviceId._ProductClass", split[1] ));
        sb.append(String.format(",\"%s\":\"/%s/\"", "InternetGatewayDevice.DeviceInfo.SoftwareVersion",split[2]));
        sb.append("}");
        return sb.toString();
    }

    public static String toZoneDateTime(String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(value, formatter);

        // Add zone time
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime.toLocalDate(), dateTime.toLocalTime(), ZoneId.systemDefault());
        return zonedDateTime.toOffsetDateTime().toString();
    }

    public static String toSampleDate(String dateTime) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime);
        DateFormat df = new SimpleDateFormat("dd/MM/yyy HH:mm");

        return df.format(Date.from(zonedDateTime.toInstant()));
    }


    public static String convertDateWithTimeZone(String date, String fromFormat, TimeZone fromTimeZone, String toFormat, TimeZone toTimeZone) {
        String result = null;
        try {

            // Declare date
            DateFormat dateFormat = new SimpleDateFormat(fromFormat);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(toFormat);

            // Set time zone
            dateFormat.setTimeZone(fromTimeZone);
            simpleDateFormat.setTimeZone(toTimeZone);

            // Convert date time
            Date dateDf = dateFormat.parse(date);
            result = simpleDateFormat.format(dateDf);

        } catch (ParseException e) {
            result = null;
            e.printStackTrace();
        }

        return result;
    }

    public static String convertDateFromElk(String date, String fromFormat, String toFormat) {
        return convertDateWithTimeZone(date, fromFormat, TimeZone.getTimeZone("GMT+0"), toFormat, TimeZone.getDefault());
    }
}
