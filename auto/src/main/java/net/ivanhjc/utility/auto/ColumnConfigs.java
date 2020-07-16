package net.ivanhjc.utility.auto;

import net.ivanhjc.utility.auto.enums.ColumnsOption;

import java.util.List;

public class ColumnConfigs {
    /**
     * Prefix that should be used before colName
     */
    private String colPrefix;
    /**
     * Prefix that should be used before varName
     */
    private String varPrefix;
    /**
     * Whether or not include searching in date range conditions. Supported date range types include finding records: <br>
     * 1. before or after a given date or between two given dates <br>
     * 2. in the last N days before a given date, including this date <br>
     * 3. in certain year, month, or on a day
     */
    private boolean includeDateRange;
    /**
     * Which fields should include IN test condition
     */
    private List<ColumnInfo> testFieldList;
    /**
     * Which fields should include FIND_IN_SET test condition
     */
    private List<ColumnInfo> testFieldListStr;
    /**
     * Which fields should include IN test condition
     */
    private ColumnsOption testFieldListOption;
    /**
     * Which fields should include FIND_IN_SET test condition
     */
    private ColumnsOption testFieldListStrOption;

    public String getColPrefix() {
        return colPrefix == null ? "" : colPrefix;
    }

    public void setColPrefix(String colPrefix) {
        this.colPrefix = colPrefix;
    }

    public String getVarPrefix() {
        return varPrefix == null ? "" : varPrefix;
    }

    public void setVarPrefix(String varPrefix) {
        this.varPrefix = varPrefix;
    }

    public boolean isIncludeDateRange() {
        return includeDateRange;
    }

    public void setIncludeDateRange(boolean includeDateRange) {
        this.includeDateRange = includeDateRange;
    }

    public List<ColumnInfo> getTestFieldList() {
        return testFieldList;
    }

    public void setTestFieldList(List<ColumnInfo> testFieldList) {
        this.testFieldList = testFieldList;
    }

    public List<ColumnInfo> getTestFieldListStr() {
        return testFieldListStr;
    }

    public void setTestFieldListStr(List<ColumnInfo> testFieldListStr) {
        this.testFieldListStr = testFieldListStr;
    }

    public ColumnsOption getTestFieldListOption() {
        return testFieldListOption;
    }

    public void setTestFieldListOption(ColumnsOption testFieldListOption) {
        this.testFieldListOption = testFieldListOption;
    }

    public ColumnsOption getTestFieldListStrOption() {
        return testFieldListStrOption;
    }

    public void setTestFieldListStrOption(ColumnsOption testFieldListStrOption) {
        this.testFieldListStrOption = testFieldListStrOption;
    }
}
