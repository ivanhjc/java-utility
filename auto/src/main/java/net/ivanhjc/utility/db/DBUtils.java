package net.ivanhjc.utility.db;

import com.google.gson.Gson;
import net.ivanhjc.utility.auto.ColumnInfo;
import net.ivanhjc.utility.auto.RandomGenerator;
import net.ivanhjc.utility.auto.enums.SQLDataTypes;
import net.ivanhjc.utility.data.StringUtils;
import net.ivanhjc.utility.db.model.params.ConnectionConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Database utility class
 */
public class DBUtils {
    private static final Logger LOG = LogManager.getLogger();
    private static final Map<String, Connection> CONNECTIONS = new HashMap<>();

    /**
     * Connect to databases using the specified configuration file. The file is placed under the resources directory so
     * you only need to provide the name of the file.
     *
     * @param conf name of the file
     * @throws SQLException if a database access error occurs or the url is {@code null}
     * @throws ClassNotFoundException if the class {@link ConnectionConfiguration} cannot be located
     * @throws IOException if an I/O error occurs
     */
    public void connect(String conf) throws SQLException, ClassNotFoundException, IOException {
        connect(new Gson().fromJson(IOUtils.toString(this.getClass().getResourceAsStream("/" + conf)),
                ConnectionConfiguration[].class));
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
     * properties as keys and their corresponding database columns as values. Only properties in the resultMap will be
     * set with values.
     *
     * @param connection the connection to use
     * @param sql        the query statement
     * @param type       the Class object of the type of the objects to create
     * @param resultMap  the property-column mapping map
     * @param <T>        the type of the objects to create
     * @return a list of objects of the specified type
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     */
    public <T> List<T> query(Connection connection, String sql, Class<T> type, Map<String, String> resultMap)
            throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
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
     * the database columns is automatic and determined by the method {@link #isSameField(String, String, String)}. All
     * properties considered corresponding to a column by {@link #isSameField(String, String, String)} will be populated
     * with the value.
     *
     * @param connection the connection to use
     * @param sql        the query statement
     * @param prefix     If the columns have a prefix it should be specified, or else null.
     * @param type       the Class object of the type of the objects to create
     * @param <T>        the type of the objects to create
     * @return a list of objects of the specified type
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     */
    public <T> List<T> query(Connection connection, String sql, String prefix, Class<T> type) throws SQLException,
            IllegalAccessException, InstantiationException, NoSuchFieldException {
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
     * Query for a String of a formatted table
     *
     * @param connection The connection to use
     * @param sql        The query statement
     * @return the formatted table
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     */
    public String query(Connection connection, String sql) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (!rs.isBeforeFirst()) {
                    LOG.info("Invalid query statement: " + sql);
                    return null;
                }
                return StringUtils.formatTable(rs);
            }
            return null;
        }
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
    public static String queryColumns(Connection connection, String schema, String tableName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SHOW FULL COLUMNS FROM " + schema + "." + tableName)) {
            return StringUtils.formatTable(ps.executeQuery());
        }
    }

    /**
     * Convert a ResultSetMetaData object to a property-column mapping map with object properties as keys and their
     * corresponding database columns as values using the specified type.
     *
     * @param rsmd   The ResultSetMetaData object
     * @param type   A POJO Class
     * @param prefix If the columns have prefix it should be specified, or else null.
     * @param <T>    The specified type
     * @return The result map
     * @throws SQLException
     */
    public static <T> Map<String, String> getResultMap(ResultSetMetaData rsmd, Class<T> type, String prefix)
            throws SQLException {
        Map<String, String> resultMap = new HashMap<>();
        Field[] fields = type.getDeclaredFields();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            String columnName = rsmd.getColumnName(i);
            resultMap.putAll(Arrays.stream(fields).filter(field -> isSameField(field.getName(), columnName, prefix))
                    .collect(toMap(Field::getName, field -> columnName)));
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
    public <T> Map<String, String> getResultMap(Connection connection, String schema, String table, String prefix,
                                                Class<T> type) throws SQLException {
        List<ColumnInfo> columns = getColumns(connection, schema, table, prefix);
        return Arrays.stream(type.getDeclaredFields()).collect(toMap(Field::getName, field -> columns.stream()
                .filter(col -> Stream.of(col.getName(), col.getVarName()).anyMatch(name ->
                        name.equalsIgnoreCase(field.getName()))).limit(1).findFirst().map(ColumnInfo::getName).get()));
    }

    /**
     * Convert a ResultSet object to a list of objects of the specified type
     *
     * @param rs        the ResultSet object
     * @param type      a POJO Class
     * @param resultMap A property-column mapping map with object properties as keys and their corresponding database
     *                  columns as values
     * @param <T>       the specified type
     * @return
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     * @throws IllegalAccessException if the class or its nullary constructor is not accessible
     * @throws InstantiationException if this Class represents an abstract class, an interface, an array class, a
     * primitive type, or void; or if the class has no nullary constructor; or if the instantiation fails for some other
     * reason.
     * @throws NoSuchFieldException if a field with the specified name is not found
     */
    public <T> List<T> getList(ResultSet rs, Class<T> type, Map<String, String> resultMap) throws SQLException,
            IllegalAccessException, InstantiationException, NoSuchFieldException {
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

    public List<ColumnInfo> getColumns(Connection connection, String schema, String tableName, String prefix)
            throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = '" + schema + "' AND TABLE_NAME = '" + tableName + "'")) {
            ResultSet rs = ps.executeQuery();
            if (null != rs) {
                if (!rs.isBeforeFirst()) {
                    throw new RuntimeException(String.format("Table \"%s\" not found", tableName));
                }

                while (rs.next()) {
                    ColumnInfo column = new ColumnInfo();
                    column.setName(rs.getString("COLUMN_NAME"));
                    column.setNullable(rs.getString("IS_NULLABLE").equals("YES"));
                    column.setType(rs.getString("DATA_TYPE"));
                    column.setCharacterMaximumLength(rs.getLong("CHARACTER_MAXIMUM_LENGTH"));
                    column.setKey(rs.getString("COLUMN_KEY"));
                    column.setComment(rs.getString("COLUMN_COMMENT"));
                    column.setVarName(toVarName(column.getName(), prefix));
                    column.setJavaType(SQLDataTypes.valueOf(column.getType().toUpperCase()).JAVA_TYPE);
                    column.setVarType(column.getJavaType().getSimpleName());
                    columns.add(column);
                }
            }
            return columns;
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
     * Generate a list of objects of the given POJO class type with each field assigned with a random value regarding
     * the field's database column properties such as data type, maximum character length, etc.
     *
     * @param type    the POJO class type
     * @param size    number of objects to generate
     * @param columns corresponding database columns of the class fields
     * @param <T>     type parameter
     * @return the generated list
     */
    public <T> List<T> sampleList(Class<T> type, int size, List<ColumnInfo> columns) throws IllegalAccessException, InstantiationException {
        Map<String, RandomGenerator> generators = columns.stream().collect(toMap(ColumnInfo::getVarName, ColumnInfo::getRandomGenerator));
        Field[] fields = Arrays.stream(type.getDeclaredFields()).filter(field -> {
            boolean matchColumn = generators.get(field.getName()) != null;
            if (matchColumn) {
                field.setAccessible(true);
            }
            return matchColumn;
        }).toArray(Field[]::new);
        List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            T obj = type.newInstance();
            for (Field field : fields) {
                field.set(obj, generators.get(field.getName()).generate());
            }
            list.add(obj);
        }
        return list;
    }
}
