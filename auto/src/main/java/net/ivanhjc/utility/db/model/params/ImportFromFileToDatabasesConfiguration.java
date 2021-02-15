package net.ivanhjc.utility.db.model.params;

import java.util.Arrays;
import java.util.List;

public class ImportFromFileToDatabasesConfiguration {
    /**
     * The absolute path of the file to import from. Currently supported file types include: xlsx. For {@code xlsx}
     * files a sheet name should be appended to the file name to specify which sheet to work on, e.g. "test.xlsx.Sheet1".
     */
    private String source;

    /**
     * The target tables to import to. They may be in different connections and schemas, and should supposedly have the
     * same definition. Currently supported database types: MySQL.
     */
    private DBTableLocator[] targets;

    /**
     * The fields of the database to import to. If fields isn't specified (null) then the first row in the source file
     * will be parsed as the database fields.The number of the provided fields must be equal to or greater than the
     * number of columns in the source file, and the order of the fields must correspond to the order of the columns
     * in the source file. If you want to keep certain columns from being imported use empty strings "" in their places.
     * Each field may contain a special syntax for converting one value to another on the fly, or in other words, it
     * specifies how to obtain the value for the field. The special syntax is enclosed in square brackets following the
     * field. There can be a number, string, or function in the brackets, such as {@code name[unknown], create_date[now()]}.
     * A field formatted like {@code field1[other_table.field2->field3]} means the value for field1 is from field3 by
     * looking up field2 in other_table. With this syntax there are might be a large number of found values for a field,
     * and you can narrow down the values by specifying the field like this
     * {@code person_id[t_person.name[John,Alex,Bella,...]->id]} so that the program will only look up those values for
     * their other corresponding values to save time and space. In this case, the id-s of John, Alex, Bella, ... A field
     * in the format {@code field1[0:name0,1:name1]} has value from the enumeration. "name0" and "name1" are values
     * stored in the source file, while 0 and 1 are values stored in the database. There can be extra fields other than
     * those corresponding to the columns in the source file and they can also be specified in the mentioned formats.
     * Spaces are not allowed after comma in all formats.
     */
    private List<String> fields;

    /**
     * Which row of data in source file the importing should start with. First row is zero and default.
     */
    private Integer startRow = 0;

    public String getSource() {
        return source;
    }

    public ImportFromFileToDatabasesConfiguration setSource(String source) {
        this.source = source;
        return this;
    }

    public DBTableLocator[] getTargets() {
        return targets;
    }

    public ImportFromFileToDatabasesConfiguration setTargets(DBTableLocator[] targets) {
        this.targets = targets;
        return this;
    }

    /**
     * A convenient method for setting target tables. A target is composed of connection name, schema name, and table
     * name. Multiple targets are separated by commas. For example, "connection1.schema1.table1,connection2.schema2.table2".
     *
     * @param targets the string representation of the target tables
     * @return this configuration
     */
    public ImportFromFileToDatabasesConfiguration setTargets(String targets) {
        this.targets = Arrays.stream(targets.split(",")).map(target -> {
            String[] temp = target.split("\\.");
            return new DBTableLocator(temp[0], temp[1], temp[2]);
        }).toArray(DBTableLocator[]::new);
        return this;
    }

    public List<String> getFields() {
        return fields;
    }

    public ImportFromFileToDatabasesConfiguration setFields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    public Integer getStartRow() {
        return startRow;
    }

    public ImportFromFileToDatabasesConfiguration setStartRow(Integer startRow) {
        this.startRow = startRow;
        return this;
    }
}
