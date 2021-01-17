package net.ivanhjc.utility.db.model.params;

public class ColumnFieldMapping {
    /**
     * The field's corresponding column index in the source file
     */
    private Integer columnIndex;
    /**
     * The name of the field in the target table
     */
    private String field;

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
