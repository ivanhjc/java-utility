package net.ivanhjc.utility.auto.enums;

/**
 * @author Administrator on 2018/10/20 17:26.
 */
public enum DaoMethod {
    SELECT0("select", "Returns one row of data by primary key."),
    SELECT1("selectList", "Returns multiple rows of data that match the specified conditions provided in a map."),
    SELECT2("selectCustom", "Returns one row of data by primary key and returns only the columns specified by fields."),
    SELECT3("selectListCustom", "Returns multiple rows of data that match the specified conditions provided in filter, and returns only the columns specified by fields."),
    INSERT0("insert", "Inserts one row of data into the table."),
    INSERT1("insertNonNull", "Inserts one row of data into the table by excluding null-valued columns in the generated SQL. This is useful when certain columns require non-null values and have default values if not provided. So when you insert data you don't have to provide values for those columns but use their default instead."),
    INSERT2("insertList", "Inserts multiple rows of data into the table."),
    INSERT3("insertListNonNull", "Inserts multiple rows of data into the table. Only columns with non-null values are inserted."),
    UPDATE0("update", "Updates one row of data by primary key"),
    UPDATE1("updateList", "Updates multiple rows of data by primary key"),
    UPDATE2("insertOrUpdate", "Inserts or updates one row of data according to whether or not a unique key is present in the parameters."),
    UPDATE3("insertOrUpdateList", "Inserts or updates multiple rows of data according to whether or not a unique key is present in the parameters."),
    DELETE0("delete", "Deletes one row of data by primary key."),
    DELETE1("deleteList", "Deletes multiple rows of data by primary key.");

    public final String NAME;
    public final String COMMENT;

    DaoMethod(String name, String comment) {
        this.NAME = name;
        this.COMMENT = comment;
    }
}
