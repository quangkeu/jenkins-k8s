package vn.vnpt.ssdc.api.model;

import java.util.List;

/**
 * Created by Huy Hieu on 11/29/2016.
 */
public class UmpApiResponse<T extends SsdcEntity<Long>> {

    public T item;
    public List<T> items;
    public String errorCode;
    public String errorMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
