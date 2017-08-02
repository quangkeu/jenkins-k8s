package vn.vnpt.ssdc.utils;

/**
 * Created by vietnq on 11/4/16.
 */
public class Constants {
    public static final String NEW_PARAMETERS = "NEW_PARAMETERS";
    public static final String REMOVED_PARAMETERS = "REMOVED_PARAMETERS";

    public static final int TAG_GROUP_OVERVIEW = 0;
    public static final int TAG_GROUP_CONFIGURATION = 1;
    public static final int TAG_GROUP_PROVISIONING = 2;


    public static final int ACTION_PROVISIONING_DELETE = 0;
    public static final int ACTION_PROVISIONING_CREATE = 1;
    public static final int ACTION_PROVISIONING_UPDATE = 2;
    public static final int ACTION_PROVISIONING_PROCESSING = 3;

    public static final String TAG_TYPE_PARAMS = "PARAMS";
    public static final String TAG_TYPE_OBJECT = "OBJECT";
    public static final String TAG_TYPE_PROVISIONED = "PROVISIONED";

    public static final int FORGOT_PASSWORD_EXPIRED_TIME = 24 * 60 * 60 * 1000; //1 day

}
