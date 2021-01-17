package net.ivanhjc.utility.db;

import com.google.gson.Gson;
import net.ivanhjc.utility.auto.ColumnInfo;
import net.ivanhjc.utility.auto.enums.DataTypeMap;
import net.ivanhjc.utility.data.StringUtils;
import net.ivanhjc.utility.db.model.params.ConnectionConfiguration;
import net.ivanhjc.utility.db.model.params.DBTableLocator;
import net.ivanhjc.utility.db.model.params.ImportFromFileConfiguration;
import net.ivanhjc.utility.file.FileUtils;
import net.ivanhjc.utility.file.POIUtils;
import net.ivanhjc.utility.file.PropertyLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class DBUtils {
    private static final Logger LOG = LogManager.getLogger();
    private static final Map<String, Connection> CONNECTIONS = new HashMap<>();

    public void connect(String conf) throws SQLException, ClassNotFoundException, IOException {
        connect(new Gson().fromJson(IOUtils.toString(this.getClass().getResourceAsStream("/" + conf)), ConnectionConfiguration[].class));
    }

    /**
     * Connect to multiple databases using the specified configurations
     *
     * @param configurations The database connection configuration objects
     */
    public void connect(ConnectionConfiguration... configurations) throws ClassNotFoundException, SQLException {
        for (ConnectionConfiguration conf : configurations) {
            if (CONNECTIONS.get(conf.getName()) == null) {
                Connection connection = null;
                if ("mysql".equals(StringUtils.substringBetween(conf.getUrl(), "jdbc:", ":"))) {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection(conf.getUrl(), conf.getUsername(), conf.getPassword());
                }
                if (connection != null) {
                    CONNECTIONS.put(conf.getName(), connection);
                }
            }
        }
    }

    /**
     * Return an existing database connection
     *
     * @param name name of the configuration file
     * @return
     */
    public static Connection getConnection(String name) {
        return CONNECTIONS.get(name);
    }

    /**
     * Query for a list of objects of the specified type by specifying a property-column mapping map with object
     * properties as keys and their corresponding database columns as values.
     *
     * @param connection The connection to use
     * @param sql        The query statement
     * @param type       The Class object of the type of the objects to create
     * @param resultMap  The property-column mapping map
     * @param <T>        The type of the objects to create
     * @return A list of objects of the specified type
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     */
    public <T> List<T> query(Connection connection, String sql, Class<T> type, Map<String, String> resultMap) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (null != rs) {
                if (!rs.isBeforeFirst()) {
                    throw new RuntimeException("Invalid query statement");
                }
                return getList(rs, type, resultMap);
            }
            return Collections.emptyList();
        }
    }

    /**
     * Query for a list of objects of the specified type. With this method the mapping between the object properties and
     * the database columns is automatic and determined by the method {@link #isSameField(String, String, String)}.
     *
     * @param connection The connection to use
     * @param sql        The query statement
     * @param prefix     If the columns have a prefix it should be specified
     * @param type       The Class object of the type of the objects to create
     * @param <T>        The type of the objects to create
     * @return A list of objects of the specified type
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     */
    public <T> List<T> query(Connection connection, String sql, String prefix, Class<T> type) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (null != rs) {
                if (!rs.isBeforeFirst()) {
                    throw new RuntimeException("Invalid query statement");
                }
                return getList(rs, type, getResultMap(rs.getMetaData(), type, prefix));
            }
            return Collections.emptyList();
        }
    }

    /**
     * Convert a ResultSetMetaData object to a property-column mapping map with object properties as keys and their
     * corresponding database columns as values using the specified type.
     *
     * @param rsmd   The ResultSetMetaData object
     * @param type   A POJO Class
     * @param prefix If the columns have a prefix it should be specified
     * @param <T>    The specified type
     * @return The result map
     * @throws SQLException
     */
    public static <T> Map<String, String> getResultMap(ResultSetMetaData rsmd, Class<T> type, String prefix) throws SQLException {
        Map<String, String> resultMap = new HashMap<>();
        Field[] fields = type.getDeclaredFields();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            String columnName = rsmd.getColumnName(i);
            resultMap.putAll(Arrays.stream(fields).filter(field -> isSameField(field.getName(), columnName, prefix)).collect(toMap(Field::getName, field -> columnName)));
        }
        return resultMap;
    }

    /**
     * Create a property-column mapping map with object properties as keys and their corresponding database columns as
     * values using the specified type and the {@code SHOW FULL COLUMNS ...} query statement.
     *
     * @param connection The connection to use
     * @param schema     The schema to use
     * @param table      The table to map
     * @param prefix     If the columns have a prefix it should be specified
     * @param type       The Class object of the type of the objects to create
     * @param <T>        The type of the objects to create
     * @return The result map
     * @throws SQLException
     */
    public <T> Map<String, String> getResultMap(Connection connection, String schema, String table, String prefix, Class<T> type) throws SQLException {
        List<ColumnInfo> columns = getColumns(connection, schema, table, prefix);
        return Arrays.stream(type.getDeclaredFields()).collect(toMap(Field::getName, field -> columns.stream().filter(col -> Stream.of(col.getName(), col.getVarName()).anyMatch(name -> name.equalsIgnoreCase(field.getName()))).limit(1).findFirst().map(ColumnInfo::getName).get()));
    }

    /**
     * Convert a ResultSet object to a list of objects of the specified type
     *
     * @param rs        the ResultSet object
     * @param type      a POJO Class
     * @param resultMap A property-column mapping map with object properties as keys and their corresponding database columns as values
     * @param <T>       the specified type
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     */
    public <T> List<T> getList(ResultSet rs, Class<T> type, Map<String, String> resultMap) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        List<T> objects = new ArrayList<>();
        while (rs.next()) {
            T o = type.newInstance();
            for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                Field field = type.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(o, rs.getObject(entry.getValue()));
            }
            objects.add(o);
        }
        return objects;
    }

    public List<ColumnInfo> getColumns(Connection connection, String schema, String tableName, String prefix) throws SQLException {
        List<ColumnInfo> fields = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SHOW FULL COLUMNS FROM " + schema + "." + tableName)) {
            ResultSet rs = ps.executeQuery();
            if (null != rs) {
                if (!rs.isBeforeFirst()) {
                    throw new RuntimeException(String.format("Table \"%s\" not found", tableName));
                }

                while (rs.next()) {
                    ColumnInfo info = new ColumnInfo();
                    info.setName(rs.getString("Field"));
                    info.setType(DataTypeMap.getDataType(rs.getString("Type")));
                    info.setIsNullable(rs.getString("Null"));
                    info.setKey(rs.getString("Key"));
                    info.setComment(rs.getString("Comment"));
                    info.setVarName(toVarName(info.getName(), prefix));
                    info.setVarType(DataTypeMap.valueOf(info.getType()).javaType);
                    fields.add(info);
                }
            }
            return fields;
        }
    }

    /**
     * Convert a column name to a Java variable name
     *
     * @param colName column name
     * @param prefix  column prefix. It should be null if there's no prefix to mind.
     * @return the converted variable name
     */
    public static String toVarName(String colName, String prefix) {
        return StringUtils.uncapitalize(StringUtils.snakeToCamel(prefix == null || prefix.length() >= colName.length()
                || !prefix.equalsIgnoreCase(colName.substring(0, prefix.length())) ? colName :
                colName.substring(prefix.length())));
    }

    /**
     * Test if the given object property and database column is the same field. They are the same as long as they contain
     * the same sequence of characters ignoring case, underlines, and the specified prefix. For example, "batchNumber",
     * "batchNuMber" and "Batch_number" are considered the same. "f_batch_number" is also the same if "f_" is specified
     * as the prefix.
     *
     * @param property One of the object's property name
     * @param column   One of the database column name
     * @param prefix   If the columns have a prefix it should be specified
     */
    private static boolean isSameField(String property, String column, String prefix) {
        return toVarName(column, prefix).equalsIgnoreCase(toVarName(property, prefix));
    }

    /**
     * Return a formatted printing representation of a table
     *
     * @param connection The connection to use
     * @param schema     The schema to use
     * @param tableName  The table to use
     * @return The formatted string
     * @throws SQLException
     */
    public static String printColumns(Connection connection, String schema, String tableName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SHOW FULL COLUMNS FROM " + schema + "." + tableName)) {
            return StringUtils.formatTable(ps.executeQuery());
        }
    }

    /**
     * Imports data from an external file (.xlsx, .csv, .txt, etc.) to a database (MySQL is supported currently). You only need to specify the file to import from (@param file),
     * which table of the database to import to (@param table), and the fields to import to corresponding to the columns in the file in the same order (@param fields).
     * Sometimes some columns in the file may need to be dealt with specially. There are two situations for such columns. One is
     * when the field corresponding to the column requires a foreign key reference value instead of the value present in the column. The field doesn't have to have a
     * FK constraint but it does have a reference relationship, say, logically, with another field in another table. The other is when the field stores a representational
     * code instead of a meaningful string. For example, for the following table
     * <br><br>
     * <p>
     * <code>CREATE TABLE student (id INT, name VARCHAR(10), class_id INT COMMENT 'FK t_class.id', state TINYINT COMMENT '0:good,1:bad');</code>
     * </p>
     * <br>
     * the data stored in the file to import may look like this
     * <p>
     * <table>
     * <tr><th>ID</th><th>Name</th><th>Class</th><th>State</th></tr>
     * <tr><td>1</td><td>Ivan</td><td>Class A</td><td>Good</td></tr>
     * <tr><td>2</td><td>Jack</td><td>Class B</td><td>Bad</td></tr>
     * </table>
     * </p>
     * Apparently the data in Class and State columns cannot be imported directly and need converting to their corresponding ids or codes first. Callers of this method
     * can specify such kinds of fields using designated syntax. For situations like Class use <code>field1[table.field2->field3]</code>, where field1 is the field to
     * import to and stores the reference values, field2 is the field that stores the values present in the file for human reading and needs parsing into values for
     * field1, field3 stores the values referenced by field1, and table is the table that holds field2 and field3. For situations like State use
     * <code>field[code:value,code:value,...]</code>, where field is the field of the table to import to that stores the code values, and the string of "code:value" pairs
     * is a comprehensive enumeration of all possible codes and their corresponding meanings. An example for importing data from the above table should be like
     * this: <code>("file", "Sheet1", "student", new String[]{"id", "name", "class_id[class.name->id]", "state[0:good,1:bad]"})</code>
     *
     * @param file
     * @param table  the table to import to, "schema.table"
     * @param fields
     * @param config configuration file for {@link Coder#CONFIG}
     */
//    public static void importFromFileToDatabase(ImportFromFileConfiguration... configurations) throws SQLException, ClassNotFoundException, IOException {
//        LOG.info("Import started. Importing...");
//        //Verification
//        for (ImportFromFileConfiguration configuration : configurations) {
//            for (DBTableLocator table : configuration.getTargets()) {
//
//                ResultSet rs = ps.executeQuery();
//
//            }
//        }
//
//        Map<Integer, Map<String, Object>> fieldMap = new HashMap<>();
//        String[] fieldsToImport = new String[fields.length];
//        StringBuilder fieldsToImportStr = new StringBuilder();
////        Coder coder = new Coder(config);
//        coder.connectToDB();
//        Connection connection = coder.getConnection();
//        for (int i = 0; i < fields.length; i++) {
//            String str = fields[i];
//            if (str.isEmpty()) {
//                fieldsToImport[i] = null;
//                continue;
//            }
//
//            if (str.contains(".")) {
//                String field1 = org.apache.commons.lang3.StringUtils.substringBefore(str, "[");
//                String table2 = org.apache.commons.lang3.StringUtils.substringBetween(str, "[", ".");
//                String field2;
//                StringBuilder whereClause = new StringBuilder();
//                if (str.contains("]->")) {
//                    field2 = str.substring(str.indexOf(".") + 1, str.lastIndexOf("["));
//                    String vals = str.substring(str.lastIndexOf("[") + 1, str.indexOf("]"));
//                    String sqlVals = StringUtils.encloseSubstrings(vals, ",", SplitRegex.DROPPED, "'", ",");
//                    whereClause.append(" WHERE ").append(field2).append(" IN (").append(sqlVals).append(")");
//                } else {
//                    field2 = org.apache.commons.lang3.StringUtils.substringBetween(str, ".", "-");
//                }
//                String field3 = org.apache.commons.lang3.StringUtils.substringBetween(str, ">", "]");
//                PreparedStatement ps = connection.prepareStatement("SELECT " + field2 + ", " + field3 + " FROM " + table2 + whereClause);
//                ResultSet rs = ps.executeQuery();
//                Map<String, Object> map1 = new HashMap<>();
//                while (rs.next()) {
//                    map1.put(rs.getString(field2), rs.getObject(field3));
//                }
//                fieldMap.put(i, map1);
//                fieldsToImport[i] = field1;
//                rs.close();
//                ps.close();
//            } else if (str.contains(",")) {
//                String[] pairs = org.apache.commons.lang3.StringUtils.substringBetween(str, "[", "]").split(",");
//                Map<String, Object> map1 = new HashMap<>();
//                for (String s : pairs) {
//                    String[] pair = s.split(":");
//                    map1.put(pair[1], Integer.valueOf(pair[0]));
//                }
//                fieldMap.put(i, map1);
//                fieldsToImport[i] = org.apache.commons.lang3.StringUtils.substringBefore(str, "[");
//            } else if (str.contains("[")) {
//                fieldsToImport[i] = org.apache.commons.lang3.StringUtils.substringBefore(str, "[");
//            } else {
//                fieldsToImport[i] = str;
//            }
//            fieldsToImportStr.append(fieldsToImport[i]).append(",");
//        }
//        fieldsToImportStr.deleteCharAt(fieldsToImportStr.length() - 1);
//
//        StringBuilder result = new StringBuilder("ImportFromFile conclusion:\n");
//        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ").append(table).append("(").append(fieldsToImportStr).append(") VALUES \n");
//        int skippedRows = 0, importedRows = 0;
//        if (file.contains(".xlsx")) {
//            String filename = org.apache.commons.lang3.StringUtils.substringBeforeLast(file, ".");
//            String sheetName = org.apache.commons.lang3.StringUtils.substringAfterLast(file, ".");
//            Sheet sheet = new XSSFWorkbook(filename).getSheet(sheetName);
//            if (sheet == null) {
//                throw new IllegalArgumentException("Unspecified sheet for spreadsheet. Please specify sheet by appending sheet name to file name like this \"example.xlsx.Sheet1\"");
//            }
//
//            row:
//            for (int i = 1; i < sheet.getLastRowNum(); i++) {
//                Row row = sheet.getRow(i);
//                if (row == null) {
//                    skippedRows++;
//                    result.append("Skipped row ").append(i).append(": Empty row\n");
//                    continue;
//                }
//                StringBuilder rowBuilder = new StringBuilder();
//                int lastCellNum = row.getLastCellNum();
//                for (int j = 0; j < fieldsToImport.length; j++) {
//                    String field = fieldsToImport[j];
//                    if (field == null) {
//                        continue;
//                    }
//
//                    String value;
//                    if (j < lastCellNum) {
//                        value = POIUtils.getCellValue(row.getCell(j));
//                    } else {
//                        value = org.apache.commons.lang3.StringUtils.substringBetween(fields[j], "[", "]");
//                    }
//
//                    if (value == null) {
//                        value = "null";
//                    } else if (!NumberUtils.isParsable(value)) {
//                        Map<String, Object> map = fieldMap.get(j);
//                        if (map != null) {
//                            Object val = map.get(value);
//                            if (val == null) {
//                                skippedRows++;
//                                result.append("Skipped row ").append(i).append(": No mapping found for field ").append("\"").append(field).append("\" value \"").append(value).append("\"\n");
//                                continue row;
//                            } else if (val instanceof String) {
//                                value = "'" + val + "'";
//                            } else {
//                                value = val.toString();
//                            }
//                        } else if (!value.equals("now()")) {
//                            value = "'" + value + "'";
//                        }
//                    }
//                    rowBuilder.append(value).append(",");
//                }
//                sqlBuilder.append("(").append(rowBuilder.deleteCharAt(rowBuilder.length() - 1)).append("),\n");
//                importedRows++;
//            }
//            sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length()).append(";");
//        } else {
//            throw new IllegalArgumentException("Unsupported file type!");
//        }
//
//        result.append("Rows imported: ").append(importedRows).append("\nRows skipped: ").append(skippedRows).append("\n");
//
//        try (PreparedStatement ps = connection.prepareStatement(sqlBuilder.toString())) {
//            ps.executeUpdate();
//        } catch (SQLSyntaxErrorException e) {
//            LOG.debug(e.getMessage() + "\n" + sqlBuilder.toString());
//            LOG.debug(result.toString());
//            throw e;
//        }
//
//        connection.close();
//
//        LOG.info("ImportFromFile finished");
//        LOG.info(result.toString());
//    }
//
}
