package vn.vnpt.ssdc.api.model;

/**
 * Created by thangnc on 07-Feb-17.
 */
public class Filter {
    public String name;
    public String oui;
    public String productClass;
    public String operator;
    public String columnName;
    public String compare;
    public String value;

    public String getName() {
        return name;
    }

    public String getOui() {
        return oui;
    }

    public String getProductClass() {
        return productClass;
    }

    public String getOperator() {
        return operator;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getCompare() {
        return compare;
    }

    public String getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOui(String oui) {
        this.oui = oui;
    }

    public void setProductClass(String productClass) {
        this.productClass = productClass;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setCompare(String compare) {
        this.compare = compare;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

