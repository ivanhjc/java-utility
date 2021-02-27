package net.ivanhjc.utility.db.model.params;

public class TableInfo {
    /**
     * Which connection the table is at
     */
    private String connection;
    /**
     * Which schema the table is in
     */
    private String schema;
    /**
     * Name of the table
     */
    private String table;
    /**
     * If the table columns have a common prefix then specify it, or else give it as null
     */
    private String prefix;

    public TableInfo() {
    }

    public TableInfo(String connection, String schema, String table) {
        this.connection = connection;
        this.schema = schema;
        this.table = table;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
