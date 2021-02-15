package net.ivanhjc.utility.db.model.params;

public class DBTableLocator {
    private String connection;
    private String schema;
    private String table;

    public DBTableLocator() {
    }

    public DBTableLocator(String connection, String schema, String table) {
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
}
