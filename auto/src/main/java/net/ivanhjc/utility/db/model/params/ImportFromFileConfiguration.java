package net.ivanhjc.utility.db.model.params;

public class ImportFromFileConfiguration {
    /**
     * The path of the file to import from. The first row should be the table header. For .xlsx files you should append
     * a sheet name to the file name to specify which sheet to use like this "test.xlsx.Sheet1"
     */
    private String source;
    /**
     * The target tables to import to. They may be in different connections and schemas, and should supposedly have the
     * same definition.
     */
    private DBTableLocator[] targets;
    /**
     * The fields to import to. Consider the following table:
     *
     * <pre><code>
     * CREATE TABLE t_student (
     *     id       BIGINT,
     *     name     VARCHAR(50),
     *     class_id BIGINT
     * )
     * </code></pre>
     *
     * The provided fields could be in the e.g. ["id", "name", "", "class_id[t_class.name->id]", "state[0:good,1:bad]"]. Note that the length
     * of the fields provided must be equal to or greater than the number of columns in the file and the order of the fields must conform with
     * the order of the columns. You can use empty strings "" to skip unwanted columns. You can also provide extra fields beyond those specified
     * by the file columns, and the values of those extra fields are also provided in the form <code>field[value]</code>, such as
     * <code>["id", "name", "", "class_id[t_class.name->id]", "state[0:good,1:bad]", "create_date[now()]"]</code>. Optionally,
     * in case there are a huge amount of available values for the fields to be parsed and only a few values that you know should be parsed, you
     * can specify those values like this "class_id[t_class.name[Class A,Class B,Class C,...]->id]", so the system will only look for those values
     * for their corresponding codes, say, the ids of Class A, Class B, Class C, etc. to save time and space. No blank spaces allowed after comma
     * in the strings provided in fields array.
     */
//    private String[] fields;
    private ColumnFieldMapping[] fields;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public DBTableLocator[] getTargets() {
        return targets;
    }

    public void setTargets(DBTableLocator[] targets) {
        this.targets = targets;
    }

    public ColumnFieldMapping[] getFields() {
        return fields;
    }

    public void setFields(ColumnFieldMapping[] fields) {
        this.fields = fields;
    }
}
