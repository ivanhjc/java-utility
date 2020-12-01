package net.ivanhjc.utility.auto;


import net.ivanhjc.utility.auto.enums.*;
import net.ivanhjc.utility.data.ListUtils;
import net.ivanhjc.utility.data.SplitRegex;
import net.ivanhjc.utility.data.StringUtils;
import net.ivanhjc.utility.file.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.sql.*;
import java.util.Date;
import java.util.*;

import static net.ivanhjc.utility.file.POIUtils.getCellValue;

/**
 * A code generating class for general web projects which adopt a client-server model. On the server side it provides methods for
 * generating JavaBean, controller, service, dao, and mapper files straight from a database connection, as well as a collection
 * of useful code snippets. For example, given a table named "CustomerCoupon" or "customer_coupon", you can generate these files:
 * CustomerCouponMapper.xml, CustomerCouponDao.java, CustomerCouponService.java, CustomerCouponServiceImpl.java, CustomerCouponController.java,
 * and many universal functional code snippets inside them.
 *
 * @author Ivan Huang on 2016/11/16
 */
public class Coder {

    private static final Logger LOG = LogManager.getLogger(Coder.class);

    private List<ColumnInfo> columns;
    private ColumnInfo primaryKey;
    private Connection connection;
    private File responseEntity;

    public final Config CONFIG;

    /**
     * Initializes a coder with the default configurations specified in "coder.properties"
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Coder() {
        this(null);
    }

    /**
     * Initializes a coder with the custom configurations specified in an external file
     *
     * @param config path of the file, absolute or relative to classpath, or null if to use default configurations
     */
    public Coder(String config) {
        if (config == null) {
            CONFIG = new Config();
        } else {
            CONFIG = new Config(config);
        }
    }

    /**
     * Connect to dababase using the specified config file
     */
    public void connectToDB() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(CONFIG.JDBC_URL, CONFIG.USERNAME, CONFIG.PASSWORD);
    }

    /**
     * Initialize all the necessary parameters for generating codes from a specified table.
     *
     * @param tableName supports both camel-casing and underscore-separated names
     */
    public Coder init(String tableName) throws SQLException {
        CONFIG.init(tableName);
        //Initialize columns
        columns = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement("SHOW FULL COLUMNS FROM " + tableName);
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
                info.setVarName(toVarName(info.getName()));
                info.setVarType(DataTypeMap.valueOf(info.getType()).javaType);
                columns.add(info);
            }
        }
        ps.close();
        List<ColumnInfo> cols = ColumnsOption.PK.getColumns(columns);
        primaryKey = cols == null || cols.size() == 0 ? null : cols.get(0);
        return this;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Generates MyBatis dynamic SQL snippets for finding one row of data from a table by Primary Key id
     */
    public String snippetSelectOne(SnippetType type) {
        String beanType = CONFIG.beanType;
        String snippet;
        switch (type) {
            case MAPPER:
                snippet = "" +
                        "    <select id=\"" + DaoMethod.SELECT0.NAME + "\" " + (CONFIG.mapUnderscoreToCamelCase ? "resultType" : "resultMap") + "=\"" + CONFIG.beanType + "\">\n" +
                        "        SELECT * FROM " + CONFIG.tableName + " WHERE id = #{id}\n" +
                        "    </select>\n\n";
                break;
            case BASE_DAO:
                beanType = CONFIG.BASE_TYPE;
            case DAO:
            case SERVICE:
                snippet = "" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT0.COMMENT + "\n" +
                        "     */\n" +
                        "    " + beanType + " " + DaoMethod.SELECT0.NAME + "(Object " + primaryKey.getVarName() + ");\n\n";
                break;
            case SERVICE_IMPL:
                snippet = "" +
                        "    @Override\n" +
                        "    public " + beanType + " " + DaoMethod.SELECT0.NAME + "(Object " + primaryKey.getVarName() + ") {\n" +
                        "        return " + CONFIG.daoVar + "." + DaoMethod.SELECT0.NAME + "(" + primaryKey.getVarName() + ");\n" +
                        "    }\n\n";
                break;
            case CONTROLLER:
                snippet = "" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT0.COMMENT + "\n" +
                        "     */\n" +
                        "    @RequestMapping(\"/info/{" + primaryKey.getVarName() + "}\")\n" +
                        "    public " + CONFIG.RESPONSE_ENTITY + " info(@PathVariable Object " + primaryKey.getVarName() + ") {\n" +
                        "        return " + CONFIG.RESPONSE_ENTITY + ".success(" + CONFIG.serviceVar + "." + DaoMethod.SELECT0.NAME + "(" + primaryKey.getVarName() + "));\n" +
                        "    }\n\n";
                break;
            default:
                snippet = "";
        }
        return snippet;
    }

    /**
     * Generates MyBatis dynamic SQL snippets for finding one row of data from a table by Unique Keys
     */
    public String snippetSelectOneByKey(ColumnsOption keyType, SnippetType snippetType) {
        StringBuilder snippets = new StringBuilder();
        List<ColumnInfo> cols = keyType.getColumns(columns);
        switch (snippetType) {
            case MAPPER:
                for (ColumnInfo col : cols) {
                    String snippet = "" +
                            "    <select id=\"" + DaoMethod.SELECT0.NAME + "By" + col.getVarNameCap() + "\" " + (CONFIG.mapUnderscoreToCamelCase ? "resultType" : "resultMap") + "=\"" + CONFIG.beanType + "\">\n" +
                            "        SELECT * FROM " + CONFIG.tableName + " WHERE " + col.getName() + " = #{" + col.getVarName() + "}\n" +
                            "    </select>\n\n";
                    snippets.append(snippet);
                }
                break;
            case CHILD_DAO:
            case DAO:
            case SERVICE:
                for (ColumnInfo col : cols) {
                    String snippet = "" +
                            "    /**\n" +
                            "     * Returns one row of data that matches the specified " + col.getKey() + " key " + col.getName() + "\n" +
                            "     */\n" +
                            "    " + CONFIG.beanType + " " + DaoMethod.SELECT0.NAME + "By" + col.getVarNameCap() + "(" + col.getVarType() + " " + col.getVarName() + ");\n\n";
                    snippets.append(snippet);
                }
                break;
            case SERVICE_IMPL:
                for (ColumnInfo col : cols) {
                    String snippet = "" +
                            "    " + CONFIG.beanType + " " + DaoMethod.SELECT0.NAME + "By" + col.getVarNameCap() + "(" + col.getVarType() + " " + col.getVarName() + ") {\n" +
                            "        return " + CONFIG.daoVar + "." + DaoMethod.SELECT0.NAME + "By" + col.getVarNameCap() + "(" + col.getVarName() + ");\n" +
                            "    }\n\n";
                    snippets.append(snippet);
                }
                break;
            default:

        }
        return snippets.toString();
    }

    /**
     * Generates MyBatis dynamic SQL snippet for finding multiple rows of data from a table
     *
     * @param columnsForTest columns that are possible conditions
     */
    public String snippetSelectList(ColumnsOption columnsForTest, SnippetType snippetType, ColumnConfigs columnConfigs) {
        String type = CONFIG.beanType;
        switch (snippetType) {
            case MAPPER:
                if (columnConfigs == null) {
                    columnConfigs = new ColumnConfigs();
                } else {
                    if (columnConfigs.getTestFieldList() == null && columnConfigs.getTestFieldListOption() != null) {
                        columnConfigs.setTestFieldList(columnConfigs.getTestFieldListOption().getColumns(columns));
                    }
                    if (columnConfigs.getTestFieldListStr() == null && columnConfigs.getTestFieldListStrOption() != null) {
                        columnConfigs.setTestFieldListStr(columnConfigs.getTestFieldListStrOption().getColumns(columns));
                    }
                }
                return "" +
                        "    <select id=\"" + DaoMethod.SELECT1.NAME + "\" " + (CONFIG.mapUnderscoreToCamelCase ? "resultType" : "resultMap") + "=\"" + CONFIG.beanType + "\">\n" +
                        "        SELECT * FROM " + CONFIG.tableName + " " + (columnConfigs.getColPrefix().isEmpty() ? "" : CONFIG.tableAcronym) + "\n" +
                        "        <where>\n" +
                        columnsForTest.getIfTest(columns, columnConfigs) +
                        "        </where>\n" +
                        "        <if test=\"" + MyBatisToken.SORT_FIELD.NAME + " != null and " + MyBatisToken.SORT_FIELD.NAME + ".trim() != '' and " + MyBatisToken.SORT_ORDER.NAME + " != null\">\n" +
                        "            ORDER BY " + columnConfigs.getColPrefix() + "${" + MyBatisToken.SORT_FIELD.NAME + "} <choose><when test=\"" + MyBatisToken.SORT_ORDER.NAME + " == 1\">DESC</when><otherwise>ASC</otherwise></choose>\n" +
                        "        </if>\n" +
                        "    </select>\n\n";
            case BASE_DAO:
                type = CONFIG.BASE_TYPE;
            case DAO:
                return "    /**\n" +
                        "     * " + DaoMethod.SELECT1.COMMENT + "\n" +
                        "     */\n" +
                        "    List<" + type + "> " + DaoMethod.SELECT1.NAME + "(Map<String, Object> filter);\n\n";
            case SERVICE:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT1.COMMENT + "\n" +
                        "     */\n" +
                        "    List<" + CONFIG.beanType + "> " + DaoMethod.SELECT1.NAME + "(Map<String, Object> filter);\n\n" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT1.COMMENT + " in paged format\n" +
                        "     */\n" +
                        "    PageInfo<" + CONFIG.beanType + "> " + DaoMethod.SELECT1.NAME + "(int pageNum, int pageSize, Map<String, Object> filter);\n\n";
            case SERVICE_IMPL:
                return "" +
                        "    @Override\n" +
                        "    public List<" + CONFIG.beanType + "> " + DaoMethod.SELECT1.NAME + "(Map<String, Object> filter) {\n" +
                        "        return " + CONFIG.daoVar + "." + DaoMethod.SELECT1.NAME + "(filter);\n" +
                        "    }\n\n" +
                        "    @Override\n" +
                        "    public PageInfo<" + CONFIG.beanType + "> " + DaoMethod.SELECT1.NAME + "(int pageNum, int pageSize, Map<String, Object> filter) {\n" +
                        "        return PageHelper.startPage(pageNum, pageSize).doSelectPageInfo(() -> " + CONFIG.daoVar + "." + DaoMethod.SELECT1.NAME + "(filter));\n" +
                        "    }\n\n";
            case CONTROLLER:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT1.COMMENT + " in paged format\n" +
                        "     */\n" +
                        "    @RequestMapping(\"/page\")\n" +
                        "    public " + CONFIG.RESPONSE_ENTITY + " page(@RequestParam(value = \"pageNum\", defaultValue = \"1\") int pageNum,\n" +
                        "                          @RequestParam(value = \"pageSize\", defaultValue = \"10\") int pageSize,\n" +
                        "                          @RequestParam Map<String, Object> filter) {\n" +
                        "        return " + CONFIG.RESPONSE_ENTITY + ".success(" + CONFIG.serviceVar + "." + DaoMethod.SELECT1.NAME + "(pageNum, pageSize, filter));\n" +
                        "    }\n\n";
            default:
                return "";
        }
    }

    /**
     * Generates MyBatis dynamic SQL snippet for finding multiple rows of data from a table, by which one can return only specified fields or columns
     *
     * @param columnsForView column/field names used for client view
     * @param snippetType    type of the generated snippet
     * @param resultType     result type the snippet returns [0:DB,1:Map] (DO stands for Database Bean)
     */
    public String snippetSelectOneCustom(ColumnsOption columnsForView, SnippetType snippetType, int resultType) {
        String fieldsName = "fields";
        String filterName = "filter";
        String alias = WordUtils.initials(CONFIG.tableName, '_');
        String colPrefix = org.apache.commons.lang3.StringUtils.isBlank(alias) ? "" : alias.concat(".");
        String resultTypeStr;
        String methodName;
        String returnType;
        if (resultType == 0) {
            resultTypeStr = CONFIG.mapUnderscoreToCamelCase ? "resultType=\"" + CONFIG.beanType + "\"" : "resultMap=\"" + CONFIG.beanType + "\"";
            methodName = DaoMethod.SELECT2.NAME + "ToDB";
            returnType = CONFIG.beanType;
        } else {
            resultTypeStr = "resultType=\"java.util.Map\"";
            methodName = DaoMethod.SELECT2.NAME + "ToMap";
            returnType = "Map<String, Object>";
        }

        switch (snippetType) {
            case MAPPER:
                return "" +
                        "    <select id=\"" + methodName + "\" " + resultTypeStr + ">\n" +
                        "        SELECT\n" +
                        "        <if test=\"fields != null\">\n" +
                        "            <foreach collection=\"" + fieldsName + "\" item=\"item\" index=\"id\" separator=\",\">\n" +
                        "                ${item}\n" +
                        "            </foreach>\n" +
                        "        </if>\n" +
                        "        FROM " + CONFIG.tableName + " " + alias + " WHERE " + alias + "." + primaryKey.getName() + " = #{" + primaryKey.getVarName() + "}\n" +
                        "    </select>\n\n";
            case BASE_DAO:
            case DAO:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT2.COMMENT + "\n" +
                        "     */\n" +
                        "    " + returnType + " " + methodName + "(@Param(\"" + fieldsName + "\") List<String> " + fieldsName + ", @Param(\"" + primaryKey.getVarName() + "\") Object " + primaryKey.getVarName() + ");\n\n";
            case SERVICE:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT2.COMMENT + "\n" +
                        "     */\n" +
                        "    " + returnType + " " + methodName + "(List<String> " + fieldsName + ", Object " + primaryKey.getVarName() + ");\n\n";
            case SERVICE_IMPL:
                return "" +
                        "    @Override\n" +
                        "    public " + returnType + " " + methodName + "(List<String> " + fieldsName + ", Object " + primaryKey.getVarName() + ") {\n" +
                        "        return " + CONFIG.daoVar + "." + methodName + "(" + fieldsName + ", " + primaryKey.getVarName() + ");\n" +
                        "    }\n\n";
            case CONTROLLER:
                StringBuilder builder = new StringBuilder();
                builder.append(String.format("" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT2.COMMENT + "\n" +
                        "     */\n" +
                        "    @RequestMapping(\"/%1$s\")\n" +
                        "    public AppResult %1$s() {\n", methodName));
                builder.append("" +
                        "        List<String> ").append(fieldsName).append(" = Arrays.asList(\n");
                columnsForView.getColumns(columns).forEach(col -> builder.append("                \"")
                        .append(col.getField(colPrefix)).append("\",\n"));
                builder.replace(builder.length() - 2, builder.length() - 1, ");\n");
                builder.append(String.format("" +
                        "        " + returnType + " %4$s = new HashMap<>();\n" +
                        "        return new AppResult(AppResponseCode.SUCCESS, %2$s.%1$s(%3$s, filter));\n" +
                        "    }", methodName, CONFIG.serviceVar, fieldsName, filterName));
                return builder.toString();
        }

        return "";
    }

    /**
     * Generates MyBatis dynamic SQL snippet for finding multiple rows of data from a table, by which one can return only specified fields or columns
     *
     * @param columnsForTest columns used for test in mapper
     * @param columnsForView column/field names used for client view
     * @param snippetType
     */
    public String snippetSelectListCustom(ColumnsOption columnsForTest, ColumnsOption columnsForView, SnippetType snippetType, ColumnConfigs columnConfigs) {
        String fieldsName = "fields";
        String filterName = "filter";
        String varPrefix = filterName.concat(".");
        switch (snippetType) {
            case MAPPER:
                if (columnConfigs == null) {
                    columnConfigs = new ColumnConfigs();
                }
                columnConfigs.setVarPrefix(varPrefix);
                String ifTest = columnsForTest.getIfTest(columns, columnConfigs);
                String sortField = varPrefix.concat(MyBatisToken.SORT_FIELD.NAME);
                String sortOrder = varPrefix.concat(MyBatisToken.SORT_ORDER.NAME);
                String limit = varPrefix.concat(MyBatisToken.LIMIT.NAME);
                return "" +
                        "    <select id=\"" + DaoMethod.SELECT3.NAME + "\" resultType=\"java.util.Map\">\n" +
                        "        SELECT\n" +
                        "        <if test=\"fields != null\">\n" +
                        "            <foreach collection=\"" + fieldsName + "\" item=\"item\" index=\"id\" separator=\",\">\n" +
                        "                ${item}\n" +
                        "            </foreach>\n" +
                        "        </if>\n" +
                        "        FROM " + CONFIG.tableName + " " + CONFIG.tableAcronym + "\n" +
                        "        <where>\n" +
                        ifTest +
                        "        </where>\n" +
                        "        <if test=\"" + sortField + " != null and " + sortField + ".trim() != '' and " + sortOrder + " != null\">\n" +
                        "            ORDER BY " + columnConfigs.getColPrefix() + "${" + sortField + "} <choose><when test=\"" + sortOrder + " == 1\">DESC</when><otherwise>ASC</otherwise></choose>\n" +
                        "        </if>\n" +
                        "        <if test=\"" + limit + " != null\">LIMIT #{" + limit + "}</if>\n" +
                        "    </select>\n\n";
            case BASE_DAO:
            case DAO:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT3.COMMENT + "\n" +
                        "     */\n" +
                        "    List<Map<String, Object>> " + DaoMethod.SELECT3.NAME + "(@Param(\"" + fieldsName + "\") List<String> " + fieldsName + ", @Param(\"" + filterName + "\") Map<String, Object> " + filterName + ");\n\n";
            case SERVICE:
                return "    /**\n" +
                        "     * " + DaoMethod.SELECT3.COMMENT + "\n" +
                        "     */\n" +
                        "    List<Map<String, Object>> " + DaoMethod.SELECT3.NAME + "(List<String> " + fieldsName + ", Map<String, Object> " + filterName + ");\n\n" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT3.COMMENT + "\n" +
                        "     */\n" +
                        "    PageInfo<Map<String, Object>> " + DaoMethod.SELECT3.NAME + "(int pageNum, int pageSize, List<String> " + fieldsName + ", Map<String, Object> " + filterName + ");\n\n";
            case SERVICE_IMPL:
                return String.format("" +
                        "    @Override\n" +
                        "    public List<Map<String, Object>> %1$s(List<String> %2$s, Map<String, Object> %3$s) {\n" +
                        "        return %4$s.%1$s(%2$s, %3$s);\n" +
                        "    }\n\n" +
                        "    @Override\n" +
                        "    public PageInfo<Map<String, Object>> %1$s(int pageNum, int pageSize, List<String> %2$s, Map<String, Object> %3$s) {\n" +
                        "        return PageHelper.startPage(pageNum, pageSize).doSelectPageInfo(() -> %4$s.%1$s(%2$s, %3$s));\n" +
                        "    }\n\n", DaoMethod.SELECT3.NAME, fieldsName, filterName, CONFIG.daoVar);
            case CONTROLLER:
                StringBuilder builder = new StringBuilder();
                builder.append(String.format("" +
                        "    /**\n" +
                        "     * " + DaoMethod.SELECT3.COMMENT + "\n" +
                        "     */\n" +
                        "    @RequestMapping(\"/%1$s\")\n" +
                        "    public AppResult %1$s() {\n", DaoMethod.SELECT3.NAME));
                builder.append("" +
                        "        List<String> ").append(fieldsName).append(" = Arrays.asList(\n");
                ColumnConfigs finalColumnConfigs = columnConfigs;
                columnsForView.getColumns(columns).forEach(col -> builder.append("                \"")
                        .append(col.getField(finalColumnConfigs.getColPrefix())).append("\",\n"));
                builder.replace(builder.length() - 2, builder.length() - 1, ");\n");
                builder.append(String.format("" +
                        "        Map<String, Object> %4$s = new HashMap<>();\n" +
                        "        return new AppResult(AppResponseCode.SUCCESS, %2$s.%1$s(%3$s, filter));\n" +
                        "    }", DaoMethod.SELECT3.NAME, CONFIG.serviceVar, fieldsName, filterName));
                return builder.toString();
        }
        return "";
    }

    /**
     * Generates MyBatis dynamic SQL snippet for inserting a new row to a table
     */
    public String snippetInsertOne(ColumnsOption option, SnippetType snippetType) {
        List<ColumnInfo> cols = option.getColumns(columns);
        String beanType = this.CONFIG.beanType;
        switch (snippetType) {
            case MAPPER:
                return "    <insert id=\"" + DaoMethod.INSERT0.NAME + "\" useGeneratedKeys=\"true\" keyProperty=\"" + primaryKey.getVarName() + "\" parameterType=\"" + (CONFIG.mapUnderscoreToCamelCase ? "" : CONFIG.PACKAGE_BEAN + ".") + beanType + "\">\n" +
                        "        INSERT INTO " + CONFIG.tableName + " (" + columnNames(cols, true) + ") VALUES (" + propertyNames(cols, null, true) + ")\n" +
                        "    </insert>\n\n";
            case BASE_DAO:
                beanType = CONFIG.BASE_TYPE;
            case DAO:
            case SERVICE:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.INSERT0.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of inserted rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.INSERT0.NAME + "(" + beanType + " " + CONFIG.beanName + ");\n\n";
            case SERVICE_IMPL:
                return "" +
                        "    @Override\n" +
                        "    public int " + DaoMethod.INSERT0.NAME + "(" + beanType + " " + CONFIG.beanName + ") {\n" +
                        "        return " + CONFIG.daoVar + "." + DaoMethod.INSERT0.NAME + "(" + CONFIG.beanName + ");\n" +
                        "    }\n\n";
            case CONTROLLER:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.INSERT0.COMMENT + "\n" +
                        "     */\n" +
                        "    @RequestMapping(\"/save\")\n" +
                        "    public " + CONFIG.RESPONSE_ENTITY + " save(@Valid " + CONFIG.beanType + " " + CONFIG.beanName + ") {\n" +
                        "        if (" + CONFIG.beanName + ".get" + primaryKey.getVarNameCap() + "() == null) {\n" +
                        "            " + CONFIG.serviceVar + "." + DaoMethod.INSERT0.NAME + "(" + CONFIG.beanName + ");\n" +
                        "        } else {\n" +
                        "            " + CONFIG.serviceVar + "." + DaoMethod.UPDATE0.NAME + "(" + CONFIG.beanName + ");\n" +
                        "        } \n" +
                        "        return " + CONFIG.RESPONSE_ENTITY + ".success();\n" +
                        "    }\n\n";
        }
        return "";
    }

    /**
     * Generates MyBatis dynamic SQL snippet for inserting a new row to a table, by which only non-null fields are inserted
     */
    public String snippetInsertNonNull(ColumnsOption option, SnippetType type) {
        String beanType = CONFIG.beanType;
        switch (type) {
            case MAPPER:
                List<ColumnInfo> cols = option.getColumns(columns);
                return "    <insert id=\"" + DaoMethod.INSERT1.NAME + "\" useGeneratedKeys=\"true\" keyProperty=\"" + primaryKey.getVarName() + "\" parameterType=\"" + (CONFIG.mapUnderscoreToCamelCase ? "" : CONFIG.PACKAGE_BEAN + ".") + beanType + "\">\n" +
                        "        INSERT INTO " + CONFIG.tableName + "\n" +
                        "        <trim prefix=\"(\" suffixOverrides=\",\" suffix=\")\">\n" +
                        optionalColumns("", cols) +
                        "        </trim>\n" +
                        "        VALUES\n" +
                        "        <trim prefix=\"(\" suffixOverrides=\",\" suffix=\")\">\n" +
                        optionalProperties("", cols) +
                        "        </trim>\n" +
                        "    </insert>\n\n";
            case BASE_DAO:
                beanType = CONFIG.BASE_TYPE;
            case SERVICE:
            case DAO:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.INSERT1.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of inserted rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.INSERT1.NAME + "(" + beanType + " " + CONFIG.beanName + ");\n\n";
            case SERVICE_IMPL:
                return "" +
                        "    @Override\n" +
                        "    public int " + DaoMethod.INSERT1.NAME + "(" + beanType + " " + CONFIG.beanName + ") {\n" +
                        "        return " + CONFIG.daoVar + "." + DaoMethod.INSERT1.NAME + "(" + CONFIG.beanName + ");\n" +
                        "    }\n\n";
        }
        return "";
    }

    /**
     * Generates MyBatis dynamic SQL snippet for inserting a new row to a table
     *
     * @param option see {@link ColumnsOption}
     */
    public String snippetInsertList(ColumnsOption option, SnippetType type) {
        String beanType = CONFIG.beanType;
        switch (type) {
            case MAPPER:
                List<ColumnInfo> cols = option.getColumns(columns);
                return "" +
                        "    <insert id=\"" + DaoMethod.INSERT2.NAME + "\" useGeneratedKeys=\"true\" keyProperty=\"" + primaryKey.getVarName() + "\" parameterType=\"java.util.List\" >\n" +
                        "        INSERT INTO " + CONFIG.tableName + " (" + columnNames(cols, true) + ") VALUES\n" +
                        "        <foreach collection=\"" + MyBatisToken.COLLECTION.NAME + "\" item=\"" + MyBatisToken.ITEM.NAME + "\" index=\"index\" open = \"(\" separator=\"),(\" close = \")\">\n" +
                        "            " + propertyNames(cols, MyBatisToken.ITEM.NAME.concat("."), true) + "\n" +
                        "        </foreach>\n" +
                        "    </insert>\n\n";
            case BASE_DAO:
                beanType = CONFIG.BASE_TYPE;
            case DAO:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.INSERT2.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of inserted rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.INSERT2.NAME + "(@Param(\"" + MyBatisToken.COLLECTION.NAME + "\") List<" + beanType + "> " + MyBatisToken.COLLECTION.NAME + ");\n\n";
            case SERVICE:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.INSERT2.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of inserted rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.INSERT2.NAME + "(List<" + beanType + "> " + MyBatisToken.COLLECTION.NAME + ");\n\n";
            case SERVICE_IMPL:
                return "" +
                        "    @Override\n" +
                        "    public int " + DaoMethod.INSERT2.NAME + "(List<" + beanType + "> " + MyBatisToken.COLLECTION.NAME + ") {\n" +
                        "        return " + CONFIG.daoVar + "." + DaoMethod.INSERT2.NAME + "(" + MyBatisToken.COLLECTION.NAME + ");\n" +
                        "    }\n\n";
            case CONTROLLER:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.INSERT2.COMMENT + "\n" +
                        "     */\n" +
                        "    @RequestMapping(\"/addList\")\n" +
                        "    public APIResult addList(@RequestBody List<" + CONFIG.beanType + "> list) {\n" +
                        "        " + CONFIG.serviceVar + "." + DaoMethod.INSERT2.NAME + "(list);\n" +
                        "        return APIResult.success();\n" +
                        "    }";
        }
        return "";
    }

    /**
     * Generates MyBatis dynamic SQL snippet for inserting multiple rows to a table, by which only non-null fields are inserted
     */
    public String snippetInsertListNonNull(ColumnsOption option, SnippetType type) {
        String beanType = CONFIG.beanType;
        switch (type) {
            case MAPPER:
                List<ColumnInfo> cols = option.getColumns(columns);
                return "    <insert id=\"" + DaoMethod.INSERT3.NAME + "\" useGeneratedKeys=\"true\" keyProperty=\"" + primaryKey.getVarName() + "\" parameterType=\"java.util.List\" >\n" +
                        "        <foreach collection=\"" + MyBatisToken.COLLECTION.NAME + "\" item=\"" + MyBatisToken.ITEM.NAME + "\" separator=\";\">\n" +
                        "        INSERT INTO " + CONFIG.tableName + "\n" +
                        "        <trim prefix=\"(\" suffixOverrides=\",\" suffix=\")\">\n" +
                        optionalColumns(MyBatisToken.ITEM.NAME + ".", cols) +
                        "        </trim>\n" +
                        "        VALUES\n" +
                        "        <trim prefix=\"(\" suffixOverrides=\",\" suffix=\")\">\n" +
                        optionalProperties(MyBatisToken.ITEM.NAME + ".", cols) +
                        "        </trim>\n" +
                        "        </foreach>\n" +
                        "    </insert>\n\n";
            case BASE_DAO:
                beanType = CONFIG.BASE_TYPE;
            case DAO:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.INSERT3.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of inserted rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.INSERT3.NAME + "(@Param(\"" + MyBatisToken.COLLECTION.NAME + "\") List<" + beanType + "> " + MyBatisToken.COLLECTION.NAME + ");\n\n";
            case SERVICE:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.INSERT3.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of inserted rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.INSERT3.NAME + "(List<" + beanType + "> " + MyBatisToken.COLLECTION.NAME + ");\n\n";
            case SERVICE_IMPL:
                return "" +
                        "    @Override\n" +
                        "    public int " + DaoMethod.INSERT3.NAME + "(List<" + beanType + "> " + MyBatisToken.COLLECTION.NAME + ") {\n" +
                        "        return " + CONFIG.daoVar + "." + DaoMethod.INSERT3.NAME + "(" + MyBatisToken.COLLECTION.NAME + ");\n" +
                        "    }\n\n";
        }
        return "";
    }

    /**
     * Generates MyBatis dynamic SQL snippet for updating a row
     */
    public String snippetUpdateOne(SnippetType type) {
        String beanType = this.CONFIG.beanType;
        switch (type) {
            case MAPPER:
                List<ColumnInfo> cols = ColumnsOption.NON_UNIQUE.getColumns(columns);
                StringBuilder ifTest = new StringBuilder();
                cols.forEach(col -> ifTest.append(col.getTestSnippetForUpdate("")));
                return "" +
                        "    <update id=\"" + DaoMethod.UPDATE0.NAME + "\" parameterType=\"" + (CONFIG.mapUnderscoreToCamelCase ? "" : CONFIG.PACKAGE_BEAN + ".") + beanType + "\">\n" +
                        "        UPDATE " + CONFIG.tableName + "\n" +
                        "        <trim prefix=\"SET\" suffixOverrides=\",\">\n" +
                        ifTest +
                        "        </trim>\n" +
                        "        WHERE " + primaryKey.getName() + " = #{" + primaryKey.getVarName() + "}\n" +
                        "    </update>\n\n";
            case BASE_DAO:
                beanType = CONFIG.BASE_TYPE;
            case DAO:
            case SERVICE:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.UPDATE0.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of updated rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.UPDATE0.NAME + "(" + beanType + " " + CONFIG.beanName + ");\n\n";
            case SERVICE_IMPL:
                return "" +
                        "    @Override\n" +
                        "    public int " + DaoMethod.UPDATE0.NAME + "(" + beanType + " " + CONFIG.beanName + ") {\n" +
                        "        return " + CONFIG.daoVar + "." + DaoMethod.UPDATE0.NAME + "(" + CONFIG.beanName + ");\n" +
                        "    }\n\n";
        }
        return "";
    }

    /**
     * Generates MyBatis dynamic SQL snippet for updating one row of data by PK or UK
     */
    public String snippetUpdateOneByKey(ColumnsOption keyType, SnippetType snippetType) {
        StringBuilder snippets = new StringBuilder();
        List<ColumnInfo> cols = keyType.getColumns(columns);
        String beanType = this.CONFIG.beanType;
        switch (snippetType) {
            case MAPPER:
                StringBuilder ifTest = new StringBuilder();
                ColumnsOption.NON_UNIQUE.getColumns(columns).forEach(col -> ifTest.append(col.getTestSnippetForUpdate("")));
                for (ColumnInfo col : cols) {
                    snippets.append(String.format("" +
                                    "    <update id=\"%5$sBy%6$s\" parameterType=\"%3$s.%4$s\">\n" +
                                    "        UPDATE %1$s\n" +
                                    "        <trim prefix=\"SET\" suffixOverrides=\",\">\n" +
                                    ifTest +
                                    "        </trim>\n" +
                                    "        WHERE %7$s = #{%8$s}\n" +
                                    "    </update>\n\n", CONFIG.tableName, CONFIG.beanVar, CONFIG.PACKAGE_BEAN, beanType, DaoMethod.UPDATE0.NAME,
                            col.getVarNameCap(), col.getName(), col.getVarName()));
                }
                break;
            case BASE_DAO:
                beanType = CONFIG.BASE_TYPE;
            case CHILD_DAO:
            case DAO:
                for (ColumnInfo col : cols) {
                    snippets.append("    /**\n")
                            .append("     * Updates one row of data that matches the specified ").append(col.getKey()).append(" key ").append(col.getName()).append("\n")
                            .append("     *\n")
                            .append("     * @return the number of updated rows\n")
                            .append("     */\n")
                            .append("    int ").append(DaoMethod.UPDATE0.NAME).append("By").append(col.getVarNameCap()).append("(").append(beanType).append(" ").append(CONFIG.beanName).append(");\n\n");
                }
                break;
        }
        return snippets.toString();
    }

    /**
     * Generates MyBatis dynamic SQL snippet for updating multiple rows
     */
    public String snippetUpdateList(SnippetType type) {
        String beanType = this.CONFIG.beanType;
        switch (type) {
            case MAPPER:
                List<ColumnInfo> cols = ColumnsOption.NON_UNIQUE.getColumns(columns);
                StringBuilder ifTest = new StringBuilder();
                cols.forEach(col -> ifTest.append(col.getTestSnippetForUpdate(MyBatisToken.ITEM.NAME.concat("."))));
                return "" +
                        "    <update id=\"" + DaoMethod.UPDATE1.NAME + "\" parameterType=\"java.util.List\" >\n" +
                        "        <foreach collection=\"" + MyBatisToken.COLLECTION.NAME + "\" item=\"" + MyBatisToken.ITEM.NAME + "\" index=\"index\" separator=\";\">\n" +
                        "        UPDATE " + CONFIG.tableName + "\n" +
                        "        <trim prefix=\"SET\" suffixOverrides=\",\">\n" +
                        ifTest +
                        "        </trim>\n" +
                        "        WHERE " + primaryKey.getName() + " = #{" + MyBatisToken.ITEM.NAME.concat(".").concat(primaryKey.getVarName()) + "}\n" +
                        "        </foreach>\n" +
                        "    </update>\n\n";
            case BASE_DAO:
                beanType = CONFIG.BASE_TYPE;
            case DAO:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.UPDATE1.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of updated rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.UPDATE1.NAME + "(@Param(\"" + MyBatisToken.COLLECTION.NAME + "\") List<" + beanType + "> " + MyBatisToken.COLLECTION.NAME + ");\n\n";
            case SERVICE:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.UPDATE1.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of updated rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.UPDATE1.NAME + "(List<" + beanType + "> " + MyBatisToken.COLLECTION.NAME + ");\n\n";
            case SERVICE_IMPL:
                return "" +
                        "    @Override\n" +
                        "    public int " + DaoMethod.UPDATE1.NAME + "(" + beanType + " " + CONFIG.beanName + ") {\n" +
                        "        return " + CONFIG.daoVar + "." + DaoMethod.UPDATE1.NAME + "(" + CONFIG.beanName + ");\n" +
                        "    }\n\n";
        }
        return "";
    }

    public String snippetInsertOrUpdateOne(ColumnsOption option, SnippetType type) {
        String beanType = this.CONFIG.beanType;
        switch (type) {
            case MAPPER:
                List<ColumnInfo> cols = option.getColumns(columns);
                return "    <insert id=\"" + DaoMethod.UPDATE2.NAME + "\" useGeneratedKeys=\"true\" keyProperty=\"" + primaryKey.getVarName() + "\" parameterType=\"" + (CONFIG.mapUnderscoreToCamelCase ? "" : CONFIG.PACKAGE_BEAN + ".") + beanType + "\">\n" +
                        "        INSERT INTO " + CONFIG.tableName + " (\n" +
                        columnNames(cols, false) +
                        "        ) VALUES (\n" +
                        propertyNames(cols, null, false) +
                        "        ) ON DUPLICATE KEY UPDATE\n" +
                        "        <trim suffixOverrides=\",\">\n" +
                        snippetIfTestForInsertOrUpdate(ColumnsOption.NON_UNIQUE.getColumns(cols)) +
                        "        </trim>\n" +
                        "    </insert>\n\n";
            case BASE_DAO:
                beanType = CONFIG.BASE_TYPE;
            case DAO:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.UPDATE2.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of affected rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.UPDATE2.NAME + "(" + beanType + " " + CONFIG.beanName + ");\n\n";
        }
        return "";
    }

    public String snippetInsertOrUpdateList(ColumnsOption option, SnippetType type) {
        String beanType = this.CONFIG.beanType;
        switch (type) {
            case MAPPER:
                List<ColumnInfo> cols = option.getColumns(columns);
                StringBuilder updateCols = new StringBuilder();
                ColumnsOption.NON_UNIQUE.getColumns(cols).forEach(columnInfo -> updateCols.append("            ").append(columnInfo.getName()).append(" = values(").append(columnInfo.getName()).append("),\n"));
                updateCols.deleteCharAt(updateCols.length() - 2);
                return "    <insert id=\"" + DaoMethod.UPDATE3.NAME + "\" parameterType=\"" + (CONFIG.mapUnderscoreToCamelCase ? "" : CONFIG.PACKAGE_BEAN + ".") + beanType + "\">\n" +
                        "        INSERT INTO " + CONFIG.tableName + " (\n" +
                        columnNames(cols, false) +
                        "        ) VALUES\n" +
                        "        <foreach collection=\"" + MyBatisToken.COLLECTION.NAME + "\" item=\"" + MyBatisToken.ITEM.NAME + "\" open = \"(\" separator=\"),(\" close = \")\">\n" +
                        propertyNames(cols, MyBatisToken.ITEM.NAME.concat("."), false) +
                        "        </foreach>\n" +
                        "        ON DUPLICATE KEY UPDATE\n" +
                        updateCols +
                        "    </insert>\n\n";
            case BASE_DAO:
                beanType = CONFIG.BASE_TYPE;
            case DAO:
                return "" +
                        "    /**\n" +
                        "     * " + DaoMethod.UPDATE3.COMMENT + "\n" +
                        "     *\n" +
                        "     * @return the number of affected rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.UPDATE3.NAME + "(@Param(\"" + MyBatisToken.COLLECTION.NAME + "\") List<" + beanType + "> " + MyBatisToken.COLLECTION.NAME + ");\n\n";
        }
        return "";
    }

    public String snippetIfTestForInsertOrUpdate(List<ColumnInfo> cols) {
        StringBuilder builder = new StringBuilder();
        cols.forEach(col -> builder.append(col.getTestSnippetForInsertOrUpdate()));
        return builder.toString();
    }

    public String snippetDeleteOneByPrimaryKey(SnippetType snippetType) {
        String snippet = "";
        if (primaryKey == null) {
            return snippet;
        }

        switch (snippetType) {
            case MAPPER:
                snippet = "" +
                        "    <delete id=\"" + DaoMethod.DELETE0.NAME + "\">\n" +
                        "        DELETE FROM " + CONFIG.tableName + " WHERE " + primaryKey.getName() + " = #{" + primaryKey.getVarName() + "}\n" +
                        "    </delete>\n\n";
                break;
            case BASE_DAO:
            case DAO:
                snippet = "" +
                        "    /**\n" +
                        "     * " + DaoMethod.DELETE0.COMMENT + "\n" +
                        "     * \n" +
                        "     * @return the number of deleted rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.DELETE0.NAME + "(Object " + primaryKey.getVarName() + ");\n\n";
                break;
        }
        return snippet;
    }

    public String snippetDeleteOneByKey(ColumnsOption keyType, SnippetType snippetType) {
        StringBuilder snippets = new StringBuilder();
        List<ColumnInfo> cols = keyType.getColumns(columns);
        switch (snippetType) {
            case MAPPER:
                cols.forEach(col -> snippets.append(String.format("" +
                        "    <delete id=\"%1$sBy%2$s\">\n" +
                        "        DELETE FROM %3$s WHERE %4$s = #{%5$s}\n" +
                        "    </delete>\n\n", DaoMethod.DELETE0.NAME, col.getVarNameCap(), CONFIG.tableName, col.getName(), col.getVarName())));
                break;
            case CHILD_DAO:
            case DAO:
                cols.forEach(col -> snippets.append(String.format("" +
                        "    /**\n" +
                        "     * Deletes one row of data that matches the specified %5$s key %6$s\n" +
                        "     * \n" +
                        "     * @return the number of deleted rows\n" +
                        "     */\n" +
                        "    int %1$sBy%2$s(%3$s %4$s);\n\n", DaoMethod.DELETE0.NAME, col.getVarNameCap(), col.getVarType(), col.getVarName(), col.getKey(), col.getName())));
                break;
        }
        return snippets.toString();
    }

    public String snippetDeleteList(SnippetType snippetType) {
        String snippet = "";
        switch (snippetType) {
            case MAPPER:
                snippet = "" +
                        "    <delete id=\"" + DaoMethod.DELETE1.NAME + "\">\n" +
                        "        DELETE FROM " + CONFIG.tableName + " WHERE id IN\n" +
                        "        <foreach collection=\"list\" item=\"item\" index=\"id\" open=\"(\" separator=\",\" close=\")\">\n" +
                        "            #{item}\n" +
                        "        </foreach>\n" +
                        "    </delete>\n\n";
                break;
            case BASE_DAO:
            case DAO:
                snippet = "" +
                        "    /**\n" +
                        "     * " + DaoMethod.DELETE1.COMMENT + "\n" +
                        "     * \n" +
                        "     * @return the number of deleted rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.DELETE1.NAME + "(List<Object> list);\n\n";
                break;
            case SERVICE:
                snippet = "" +
                        "    /**\n" +
                        "     * " + DaoMethod.DELETE1.COMMENT + "\n" +
                        "     * \n" +
                        "     * @return the number of deleted rows\n" +
                        "     */\n" +
                        "    int " + DaoMethod.DELETE1.NAME + "(List<Integer> list);\n\n";
                break;
            case SERVICE_IMPL:
                snippet = "" +
                        "    @Override\n" +
                        "    public int " + DaoMethod.DELETE1.NAME + "(List<Integer> list) {\n" +
                        "        return " + CONFIG.daoVar + "." + DaoMethod.DELETE1.NAME + "(list);\n" +
                        "    }\n";
                break;
        }
        return snippet;
    }

    /**
     * Get a MyBatis resultMap
     */
    public String snippetResultMap() {
        StringBuilder results = new StringBuilder();
        columns.forEach(column -> results.append(String.format("        <result column=\"%s\" property=\"%s\"/>\n", column.getName(), column.getVarName())));
        return "" +
                "    <resultMap id=\"" + CONFIG.beanType + "\" type=\"" + CONFIG.PACKAGE_BEAN + "." + CONFIG.beanType + "\">\n" +
                results +
                "    </resultMap>\n\n";
    }

    public String snippetTypeAlias() {
        return "        <typeAlias type=\"" + CONFIG.PACKAGE_BEAN + "." + CONFIG.beanType + "\" alias=\"" + CONFIG.beanType + "\"/>";
    }

    public String snippetTypeAliases(String tables) {
        StringBuilder aliases = new StringBuilder();
        Arrays.asList(tables.split(",")).forEach(table -> {
            CONFIG.init(table);
            aliases.append(snippetTypeAlias()).append("\n");
        });
        return "" +
                "    <typeAliases>\n" +
                aliases +
                "    </typeAliases>\n";
    }

    /**
     * Get a snippet for populating a bean object
     *
     * @param varName variable name of the object
     * @return
     */
    public String snippetBeanSetters(String varName) {
        StringBuilder setters = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo column = columns.get(i);
            String value = DataTypeMap.valueOf(column.getType()).value;
            String comment = getComment(column);
            setters.append(String.format("    %1$s.set%2$s(%4$s); %3$s%n",
                    varName, org.apache.commons.lang3.StringUtils.capitalize(column.getVarName()), comment, value));
        }
        return setters.toString();
    }

    /**
     * Get a snippet for populating a bean object with get-values of another bean object
     *
     * @param varName1 variable name of the to-be-populated object
     * @param varName2 variable name of the populating object
     * @return
     */
    public String snippetBeanSetters(String varName1, String varName2) {
        StringBuilder setters = new StringBuilder();
        for (ColumnInfo column : columns) {
            String comment = getComment(column);
            setters.append(String.format("    %1$s.set%2$s(%4$s.get%2$s()); %3$s%n",
                    varName1, org.apache.commons.lang3.StringUtils.capitalize(column.getVarName()), comment, varName2));
        }
        return setters.toString();
    }

    /**
     * Generate code for getting a bean instance
     */
    public String snippetGetBeanInstance(String varName) {
        return String.format("" +
                "public %1$s get%1$s() {\n" +
                "    %1$s %3$s = new %1$s();\n" +
                "%2$s" +
                "    return bean;\n" +
                "}", CONFIG.beanType, snippetBeanSetters(varName), varName);
    }

    /**
     * Creates multiple Enums from comments of table columns which have the following syntax: column description[id:name,id:name,...], e.g. state[0:normal,1:used,2:expired]
     *
     * @param colNames the target columns separated with comma, e.g. "type", "type,state", "type:0,state:1". You can append a :0 or :1 to the names to
     *                 specify which type of enum should be generated as shown in above example. The types differ in how their object names are represented,
     *                 type 0 using values in the comment and type 1 using the string "TYPE&lt;id>". Thus the enum generated from the above example is
     *                 `enum State{NORMAL,USED,EXPIRED}` in type 0 and `enum State{TYPE0,TYPE1,TYPE2}`in type 1. Type 1 is used by default if the types are not specified.
     * @throws IOException when an error occur while creating the file
     */
    public void createEnums(String colNames) throws IOException {
        String[] cols = colNames.split(",");
        for (String c : cols) {
            String enumName;
            int enumType;
            if (c.contains(":")) {
                String[] foos = c.split(":");
                enumName = foos[0];
                enumType = Integer.valueOf(foos[1]);
            } else {
                enumName = c;
                enumType = 1;
            }
            for (ColumnInfo info : columns) {
                if (enumName.equals(info.getName())) {
                    int firstOpen = info.getComment().indexOf('[');
                    int firstClose = info.getComment().indexOf("]");
                    String classComment = firstOpen > 0 ? info.getComment().substring(0, firstOpen) : "";
                    String className = CONFIG.tableNameCamelCase.concat(StringUtils.capitalize(info.getVarNameCap()));
                    String[] types = info.getComment().substring(firstOpen + 1, firstClose).split(",");
                    StringBuilder objects = new StringBuilder();
                    String data;
                    if (enumType == 0) {
                        for (String type : types) {
                            String[] pair = type.split(":");
                            String k = pair[0], v = pair[1];
                            objects.append("    /**\n")
                                    .append("     * ").append(k).append("-").append(v).append("\n")
                                    .append("     */\n")
                                    .append("    ").append(v.toUpperCase().replace(' ', '_')).append("(").append(k).append("),\n");
                        }
                        objects.replace(objects.length() - 2, objects.length(), ";\n\n");
                        data = "" +
                                "package " + CONFIG.PACKAGE_ENUM + ";\n\n" +
                                "/**\n" +
                                " * " + classComment + "\n" +
                                " *\n" +
                                " * @author " + CONFIG.AUTHOR + "\n" +
                                " */\n" +
                                "public enum " + className + " {\n" +
                                objects +
                                "    public final Integer ID;\n\n" +
                                "    " + className + "(Integer id) {\n" +
                                "        this.ID = id;\n" +
                                "    }\n" +
                                "}";
                    } else {
                        for (String type : types) {
                            String[] pair = type.split(":");
                            String k = pair[0], v = pair[1];
                            objects.append("    /**\n")
                                    .append("     * ").append(k).append("-").append(v).append("\n")
                                    .append("     */\n")
                                    .append("    TYPE").append(k).append("(").append(k).append(", \"").append(v).append("\"),\n");
                        }
                        objects.replace(objects.length() - 2, objects.length(), ";\n\n");
                        data = "" +
                                "package " + CONFIG.PACKAGE_ENUM + ";\n\n" +
                                "/**\n" +
                                " * " + classComment + "\n" +
                                " *\n" +
                                " * @author " + CONFIG.AUTHOR + "\n" +
                                " */\n" +
                                "public enum " + className + " {\n" +
                                objects +
                                "    public final Integer ID;\n" +
                                "    public final String NAME;\n\n" +
                                "    " + className + "(Integer id, String name) {\n" +
                                "        this.ID = id;\n" +
                                "        this.NAME = name;\n" +
                                "    }\n" +
                                "}";
                    }
                    writeToFile(CONFIG.DIR_ENUM.resolve(className.concat(".java")), data);
                }
            }
        }
    }

    public void createEnums() throws IOException {
        StringBuilder colNames = new StringBuilder();
        for (ColumnInfo c : columns) {
            if (c.getComment() != null && c.getComment().length() > 3 && "[E]".equals(c.getComment().substring(c.getComment().length() - 3))) {
                colNames.append(c.getName()).append(",");
            }
        }
        if (colNames.length() > 0) {
            createEnums(colNames.toString());
        }
    }

    public void createBean() throws IOException {
        boolean hasDate = false;
        boolean hasBigDecimal = false;
        StringBuilder fields = new StringBuilder();
        StringBuilder methods = new StringBuilder();
        for (ColumnInfo column : columns) {
            if (!hasDate && "Date".equals(column.getVarType())) {
                hasDate = true;
            }
            if (!hasBigDecimal && "BigDecimal".equals(column.getVarType())) {
                hasBigDecimal = true;
            }

            String comment = CONFIG.showComment ? column.getComment() : "";
            String javaDoc = "";
            if (comment != null && !comment.trim().isEmpty()) {
                javaDoc = "" +
                        "    /**\n" +
                        "     * " + comment + "\n" +
                        "     */\n";
            }

            String field = javaDoc + "    private " + column.getVarType() + " " + column.getVarName() + ";\n";
            fields.append(field);
            methods.append(String.format("" +
                            "%4$s" +
                            "    public void set%1$s(%2$s %3$s) {\n" +
                            "        this.%3$s = %3$s;\n" +
                            "    }\n\n" +
                            "%4$s" +
                            "    public %2$s get%1$s() {\n" +
                            "        return this.%3$s;\n" +
                            "    }\n\n",
                    org.apache.commons.lang3.StringUtils.capitalize(column.getVarName()), column.getVarType(), column.getVarName(), javaDoc));
        }

        String imports = "import java.io.Serializable;\n" +
                (hasDate ? "import java.util.Date;\n" : "") +
                (hasBigDecimal ? "import java.math.BigDecimal;\n" : "");

        writeToFile(CONFIG.DIR_BEAN.resolve(CONFIG.beanType + ".java"), "" +
                "package " + CONFIG.PACKAGE_BEAN + ";\n\n" +
                imports +
                "\n" +
                "/**\n" +
                " * @author " + CONFIG.AUTHOR + " on " + new Date() + "\n" +
                " */\n" +
                "public class " + CONFIG.beanType + " implements Serializable {\n" +
                fields + "\n" +
                methods +
                "}\n");
    }

    public void createDao(SnippetType type) throws IOException {
        StringBuilder code = new StringBuilder()
                .append("package ").append(CONFIG.PACKAGE_DAO).append(";\n\n");
        for (AnnotationOption anno : CONFIG.annotationOptions) {
            code.append(anno.importStr);
        }
        if (type != SnippetType.BASE_DAO) {
            code.append("import ").append(CONFIG.PACKAGE_BEAN).append(".").append(CONFIG.beanType).append(";\n");
        }
        if (type != SnippetType.CHILD_DAO) {
            code.append("import java.util.List;\n")
                    .append("import java.util.Map;\n");
        }
        code.append("\n")
                .append("/**\n")
                .append(" * @author ").append(CONFIG.AUTHOR).append(" on ").append(new Date()).append("\n")
                .append(" */\n")
                .append(AnnotationOption.REPOSITORY.getSpecified(CONFIG.annotationOptions).annotation);
        String daoType = CONFIG.daoType;
        String baseDao = "Base" + CONFIG.SUFFIX_DAO;
        String fileName = daoType;
        if (type == SnippetType.BASE_DAO) {
            daoType = baseDao + "<" + CONFIG.BASE_TYPE + ">";
            fileName = baseDao;
        } else if (type == SnippetType.CHILD_DAO) {
            daoType = CONFIG.daoType + " extends " + baseDao + "<" + CONFIG.beanType + ">";
        }
        code.append("public interface ").append(daoType).append(" {\n\n");
        if (type != SnippetType.CHILD_DAO) {
            code.append(snippetSelectOne(type))
                    .append(snippetSelectOneByKey(ColumnsOption.UNIQUE, type))
                    .append(snippetSelectList(ColumnsOption.ALL, type, null))
                    .append(snippetSelectOneCustom(ColumnsOption.ALL, SnippetType.DAO, 0))
                    .append(snippetSelectListCustom(ColumnsOption.ALL, ColumnsOption.ALL, SnippetType.DAO, null))
                    .append(snippetInsertOne(ColumnsOption.ALL, type))
                    .append(snippetInsertNonNull(ColumnsOption.ALL, type))
                    .append(snippetInsertList(ColumnsOption.ALL, type))
                    .append(snippetInsertListNonNull(ColumnsOption.ALL, type))
                    .append(snippetUpdateOne(type))
                    .append(snippetUpdateOneByKey(ColumnsOption.UNIQUE, type))
                    .append(snippetInsertOrUpdateOne(ColumnsOption.ALL, type))
                    .append(snippetInsertOrUpdateList(ColumnsOption.ALL, type))
                    .append(snippetDeleteOneByPrimaryKey(type))
                    .append(snippetDeleteOneByKey(ColumnsOption.UNIQUE, type))
                    .append(snippetDeleteList(type));
        }
        code.append("}\n");
        writeToFile(CONFIG.DIR_DAO.resolve(fileName + ".java"), code.toString());
    }

    public void createMapper() throws IOException {
        ColumnConfigs configs = new ColumnConfigs();
        configs.setColPrefix(CONFIG.tableAcronym.concat("."));
        configs.setIncludeDateRange(CONFIG.includeDateRange);
        configs.setTestFieldList(Collections.singletonList(primaryKey));
        configs.setTestFieldListStr(Collections.singletonList(primaryKey));
        writeToFile(CONFIG.DIR_MAPPER.resolve(CONFIG.mapperName + ".xml"), "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.apache.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"" + CONFIG.PACKAGE_DAO + "." + CONFIG.daoType + "\">\n\n" +
                (CONFIG.mapUnderscoreToCamelCase ? "" : snippetResultMap()) +
                snippetSelectOne(SnippetType.MAPPER) +
                snippetSelectOneByKey(ColumnsOption.UNIQUE, SnippetType.MAPPER) +
                snippetSelectList(ColumnsOption.ALL, SnippetType.MAPPER, configs) +
                snippetSelectOneCustom(ColumnsOption.ALL, SnippetType.MAPPER, 0) +
                snippetSelectListCustom(ColumnsOption.ALL, ColumnsOption.ALL, SnippetType.MAPPER, configs) +
                snippetInsertOne(ColumnsOption.EXCLUDE.setFilter("update_by,update_name,update_time,update_date"), SnippetType.MAPPER) +
                snippetInsertNonNull(ColumnsOption.EXCLUDE, SnippetType.MAPPER) +
                snippetInsertList(ColumnsOption.EXCLUDE, SnippetType.MAPPER) +
                snippetInsertListNonNull(ColumnsOption.EXCLUDE, SnippetType.MAPPER) +
                snippetUpdateOne(SnippetType.MAPPER) +
                snippetUpdateOneByKey(ColumnsOption.UNIQUE, SnippetType.MAPPER) +
                snippetInsertOrUpdateOne(ColumnsOption.ALL, SnippetType.MAPPER) +
                snippetInsertOrUpdateList(ColumnsOption.ALL, SnippetType.MAPPER) +
                snippetDeleteOneByPrimaryKey(SnippetType.MAPPER) +
                snippetDeleteOneByKey(ColumnsOption.UNIQUE, SnippetType.MAPPER) +
                snippetDeleteList(SnippetType.MAPPER) +
                "</mapper>\n");
    }

    public void createService() throws IOException {
        writeToFile(CONFIG.DIR_SERVICE.resolve(CONFIG.serviceType + ".java"), "" +
                "package " + CONFIG.PACKAGE_SERVICE + ";\n\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "import com.github.pagehelper.PageInfo;\n" +
                "import " + CONFIG.PACKAGE_BEAN + "." + CONFIG.beanType + ";\n\n" +
                "/**\n" +
                " * @author " + CONFIG.AUTHOR + " on " + new Date() + "\n" +
                " */\n" +
                "public interface " + CONFIG.serviceType + " {\n" +
                snippetSelectOne(SnippetType.SERVICE) +
                snippetSelectOneByKey(ColumnsOption.UNIQUE, SnippetType.SERVICE) +
                snippetSelectList(ColumnsOption.ALL, SnippetType.SERVICE, null) +
                snippetSelectOneCustom(ColumnsOption.ALL, SnippetType.SERVICE, 0) +
                snippetSelectListCustom(ColumnsOption.ALL, ColumnsOption.ALL, SnippetType.SERVICE, null) +
                snippetInsertOne(ColumnsOption.ALL, SnippetType.SERVICE) +
                snippetInsertNonNull(ColumnsOption.ALL, SnippetType.SERVICE) +
                snippetInsertList(ColumnsOption.ALL, SnippetType.SERVICE) +
                snippetInsertListNonNull(ColumnsOption.ALL, SnippetType.SERVICE) +
                snippetUpdateOne(SnippetType.SERVICE) +
                "}");
    }

    public void createSubService() throws IOException {
        writeToFile(CONFIG.DIR_SERVICE.resolve(CONFIG.serviceType + ".java"), "" +
                "package " + CONFIG.PACKAGE_SERVICE + ";\n\n" +
                "import " + CONFIG.PACKAGE_BEAN + "." + CONFIG.beanType + ";\n\n" +
                "/**\n" +
                " * @author " + CONFIG.AUTHOR + " on " + new Date() + "\n" +
                " */\n" +
                "public interface " + CONFIG.serviceType + " extends BaseService<" + CONFIG.beanType + "> {\n\n" +
                "}\n");
    }

    public void createServiceImpl() throws IOException {
        writeToFile(CONFIG.DIR_SERVICE_IMPL.resolve(CONFIG.serviceImplType + ".java"), "" +
                "package " + CONFIG.PACKAGE_SERVICE_IMPL + ";\n\n" +
                "import com.github.pagehelper.PageHelper;\n" +
                "import com.github.pagehelper.PageInfo;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "import " + CONFIG.PACKAGE_SERVICE + "." + CONFIG.serviceType + ";\n" +
                "import " + CONFIG.PACKAGE_BEAN + "." + CONFIG.beanType + ";\n" +
                "import " + CONFIG.PACKAGE_DAO + "." + CONFIG.daoType + ";\n\n" +
                "/**\n" +
                " * @author " + CONFIG.AUTHOR + " on " + new Date() + "\n" +
                " */\n" +
                "@Service(value = \"" + CONFIG.serviceVar + "\")\n" +
                "public class " + CONFIG.serviceImplType + " implements " + CONFIG.serviceType + " {\n\n" +
                "    private final " + CONFIG.daoType + " " + CONFIG.daoVar + ";\n\n" +
                "    " + CONFIG.serviceImplType + "(" + CONFIG.daoType + " " + CONFIG.daoVar + ") {\n" +
                "        this." + CONFIG.daoVar + " = " + CONFIG.daoVar + ";\n" +
                "    }\n\n" +
                snippetSelectOne(SnippetType.SERVICE_IMPL) +
                snippetSelectOneByKey(ColumnsOption.UNIQUE, SnippetType.SERVICE_IMPL) +
                snippetSelectList(ColumnsOption.ALL, SnippetType.SERVICE_IMPL, null) +
                snippetSelectOneCustom(ColumnsOption.ALL, SnippetType.SERVICE_IMPL, 0) +
                snippetSelectListCustom(ColumnsOption.ALL, ColumnsOption.ALL, SnippetType.SERVICE_IMPL, null) +
                snippetInsertOne(ColumnsOption.ALL, SnippetType.SERVICE_IMPL) +
                snippetInsertNonNull(ColumnsOption.ALL, SnippetType.SERVICE_IMPL) +
                snippetInsertList(ColumnsOption.ALL, SnippetType.SERVICE_IMPL) +
                snippetInsertListNonNull(ColumnsOption.ALL, SnippetType.SERVICE_IMPL) +
                snippetUpdateOne(SnippetType.SERVICE_IMPL) +
                "}\n");
    }

    public void createController() throws IOException {
        writeToFile(CONFIG.DIR_CONTROLLER.resolve(CONFIG.controllerType + ".java"), "" +
                "package " + CONFIG.PACKAGE_CONTROLLER + ";\n\n" +
                "import " + CONFIG.PACKAGE_BEAN + "." + CONFIG.beanType + ";\n" +
                "import " + CONFIG.PACKAGE_APIRESULT + "." + CONFIG.RESPONSE_ENTITY + ";\n" +
                "import " + CONFIG.PACKAGE_SERVICE + "." + CONFIG.serviceType + ";\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "import javax.validation.Valid;\n" +
                "import java.util.Map;\n\n" +
                "/**\n" +
                " * @author " + CONFIG.AUTHOR + " on " + new Date() + "\n" +
                " */\n" +
                "@RestController\n" +
                "@RequestMapping(\"api/" + CONFIG.tableAcronym + "\")\n" +
                "public class " + CONFIG.controllerType + " extends BaseController {\n\n" +
                "    private final " + CONFIG.serviceType + " " + CONFIG.serviceVar + ";\n\n" +
                "    public " + CONFIG.controllerType + "(" + CONFIG.serviceType + " " + CONFIG.serviceVar + ") {\n" +
                "        this." + CONFIG.serviceVar + " = " + CONFIG.serviceVar + ";\n" +
                "    }\n\n" +
                snippetSelectOne(SnippetType.CONTROLLER) +
                snippetSelectList(ColumnsOption.ALL, SnippetType.CONTROLLER, null) +
                snippetInsertOne(ColumnsOption.ALL, SnippetType.CONTROLLER) +
                "}\n");
    }

  /*  public void createConfigs() throws IOException {
        createConfigMybatis();
        createConfigJDBC();
        createConfigSpring();

        System.out.println("" +
                "        <!--Essentials-->\n" +
                "        <dependency>\n" +
                "            <groupId>org.springframework</groupId>\n" +
                "            <artifactId>spring-core</artifactId>\n" +
                "            <version>4.3.8.RELEASE</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.springframework</groupId>\n" +
                "            <artifactId>spring-context</artifactId>\n" +
                "            <version>4.3.8.RELEASE</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.springframework</groupId>\n" +
                "            <artifactId>spring-web</artifactId>\n" +
                "            <version>4.3.8.RELEASE</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.springframework</groupId>\n" +
                "            <artifactId>spring-jdbc</artifactId>\n" +
                "            <version>4.3.2.RELEASE</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.mybatis</groupId>\n" +
                "            <artifactId>mybatis</artifactId>\n" +
                "            <version>3.2.8</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>org.mybatis</groupId>\n" +
                "            <artifactId>mybatis-spring</artifactId>\n" +
                "            <version>1.2.2</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>mysql</groupId>\n" +
                "            <artifactId>mysql-connector-java</artifactId>\n" +
                "            <version>5.1.37</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>commons-pool</groupId>\n" +
                "            <artifactId>commons-pool</artifactId>\n" +
                "            <version>1.6</version>\n" +
                "        </dependency>\n" +
                "        <dependency>\n" +
                "            <groupId>commons-dbcp</groupId>\n" +
                "            <artifactId>commons-dbcp</artifactId>\n" +
                "            <version>1.4</version>\n" +
                "        </dependency>");
    }*/

    /*public void createConfigMybatis() throws IOException {
        String content = String.format("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\"\n" +
                "        \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n" +
                "<configuration>\n" +
                "    <properties>\n" +
                "        <property name=\"dialect\" value=\"mysql\"/>\n" +
                "    </properties>\n" +
                "    <settings>\n" +
                "        <setting name=\"logImpl\" value=\"STDOUT_LOGGING\"/>\n" +
                "    </settings>\n" +
                "    <typeAliases>\n" +
                "        <typeAlias alias=\"%1$s\" type=\"%2$s.bean.%1$s\"/>\n" +
                "    </typeAliases>\n" +
                "</configuration>", CONFIG.beanType, CONFIG.PACKAGE_BEAN);
        writeToFile(CONFIG.DIR_OTHER.resolve("MyBatisConfig.xml"), content);
    }*/

    /*public void createConfigJDBC() throws IOException {
        String content = String.format("" +
                "driverClass=com.mysql.jdbc.Driver\n" +
                "jdbcUrl=%1$s\n" +
                "username=%2$s\n" +
                "password=%3$s", CONFIG.JDBC_URL, CONFIG.USERNAME, CONFIG.PASSWORD);
        writeToFile(CONFIG.DIR_OTHER.resolve("JDBC.properties"), content);
    }*/

    /*public void createConfigSpring() throws IOException {
        String content = String.format("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "       xmlns:tx=\"http://www.springframework.org/schema/tx\"\n" +
                "       xmlns:p=\"http://www.springframework.org/schema/p\"\n" +
                "       xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\n" +
                "       http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-3.0.xsd\">\n" +
                "    <bean id=\"propertyConfigurer\" class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">\n" +
                "        <property name=\"locations\">\n" +
                "            <list>\n" +
                "                <value>classpath:conf/JDBC.properties</value>\n" +
                "            </list>\n" +
                "        </property>\n" +
                "    </bean>\n" +
                "    <bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" +
                "        <property name=\"driverClassName\" value=\"${driverClass}\" />\n" +
                "        <property name=\"url\" value=\"${jdbcUrl}\" />\n" +
                "        <property name=\"username\" value=\"${username}\" />\n" +
                "        <property name=\"password\" value=\"${password}\" />\n" +
                "        <property name=\"maxActive\" value=\"50\" />\n" +
                "        <property name=\"maxIdle\" value=\"3\" />\n" +
                "        <property name=\"maxWait\" value=\"1000\" />\n" +
                "        <property name=\"defaultAutoCommit\" value=\"true\" />\n" +
                "        <property name=\"removeAbandoned\" value=\"true\" />\n" +
                "        <property name=\"removeAbandonedTimeout\" value=\"60\" />\n" +
                "    </bean>\n" +
                "    <bean id=\"sqlSessionFactory\" class=\"org.mybatis.spring.SqlSessionFactoryBean\">\n" +
                "        <property name=\"dataSource\" ref=\"dataSource\" />\n" +
                "        <property name=\"configLocation\" value=\"classpath:conf/MyBatisConfig.xml\"/>\n" +
                "        <property name=\"mapperLocations\" value=\"classpath:tslease.mapper/*Mapper.xml\" />\n" +
                "    </bean>\n" +
                "    <bean class=\"org.mybatis.spring.tslease.mapper.MapperScannerConfigurer\"\n" +
                "          p:basePackage=\"%1$s.dao\" p:sqlSessionFactoryBeanName=\"sqlSessionFactory\" />\n" +
                "\n" +
                "    <bean id=\"transactionManager\"\n" +
                "          class=\"org.springframework.jdbc.datasource.DataSourceTransactionManager\">\n" +
                "        <property name=\"dataSource\" ref=\"dataSource\" />\n" +
                "    </bean>\n" +
                "\n" +
                "    <tx:annotation-driven transaction-manager=\"transactionManager\" />\n" +
                "\n" +
                "    <bean class=\"org.mybatis.spring.tslease.mapper.MapperScannerConfigurer\"\n" +
                "          p:basePackage=\"%1$s.dao\" p:sqlSessionFactoryBeanName=\"sqlSessionFactory\" />\n" +
                "</beans>\n", CONFIG.PACKAGE_BEAN);
        writeToFile(CONFIG.DIR_OTHER.resolve("applicationContext.xml"), content);
    }*/

    public void createAll() throws IOException {
        createBean();
        createDao(SnippetType.CHILD_DAO);
        createMapper();
        createService();
        createServiceImpl();
        createController();
    }

    /**
     * Creates multiple DAOs, mappers, etc as specified by createType and the names of the tables
     *
     * @param tables      names of the tables to create from
     * @param createTypes types of files to generate
     */
    public void create(String tables, CreateType... createTypes) throws NoSuchMethodException, SQLException, InvocationTargetException, IllegalAccessException {
        String[] tabs = tables.split(",");
        List<Method> methods = new ArrayList<>();
        for (CreateType type : createTypes) {
            String methodName = "create" + (type.name().contains("_") ? StringUtils.snakeToCamel(type.name()) : StringUtils.toTitleCase(type.name()));
            Method method;
            if ("createDao".equals(methodName)) {
                method = Coder.class.getDeclaredMethod(methodName, SnippetType.class);
            } else {
                method = Coder.class.getDeclaredMethod(methodName);
            }
            methods.add(method);
        }

        for (String table : tabs) {
            init(table);
            for (Method m : methods) {
                if ("createDao".equals(m.getName())) {
                    m.invoke(this, SnippetType.CHILD_DAO);
                } else {
                    m.invoke(this);
                }
            }
        }
    }

    /**
     * Generate code for getting a list which has beanNum beans
     *
     * @param beanNum
     */
    public void printSnippetGetBeanList(int beanNum) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < beanNum; i++) {
            String varName = "bean" + i;
            sb.append(String.format("" +
                    "    %1$s %3$s = new %1$s();\n" +
                    "%2$s" +
                    "    data.add(%3$s);\n\n", CONFIG.beanType, snippetBeanSetters(varName), varName));
        }
        String method = String.format("" +
                "public static List<%1$s> getList%1$s() {\n" +
                "    List<%1$s> data = new ArrayList<>();\n" +
                "%2$s" +
                "    return data;\n" +
                "}", CONFIG.beanType, sb);

        System.out.println(method);
        System.out.println();
    }

//    ----------------------------------------------------------------------------------------Spreadsheet util

    /**
     * Generate code for getting a list of beans which are stored in an Excel sheet
     * and are mapped to the table provided via <code>init()</code>
     */
    public void printSnippetGetBeanListFromExcelSheet() {
        StringBuilder setBean = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            setBean.append(String.format("bean.set%s(get%s(row, )); //%s%n",
                    org.apache.commons.lang3.StringUtils.capitalize(columns.get(i).getVarName()), columns.get(i).getVarType(), columns.get(i).getComment()));
        }
        String method = String.format("" +
                "public List<%1$s> get%1$sList(Sheet sheet) {\n" +
                "    List<%1$s> data = new ArrayList<>();\n" +
                "    for (int i = 0; i <= sheet.getLastRowNum(); i++) {\n" +
                "        Row row = sheet.getRow(i);\n" +
                "        %1$s bean = new %1$s();\n" +
                "        %2$s\n" +
                "        data.add(bean);\n" +
                "    }\n" +
                "    return data;\n" +
                "}", CONFIG.beanType, setBean);

        System.out.println(method);
        System.out.println();
    }

    public void printSnippetGetBeanListFromResultSet() {
        StringBuilder setBean = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo column = columns.get(i);
            setBean.append(String.format("        bean.set%s(rs.get%s(\"%s\"));\n",
                    org.apache.commons.lang3.StringUtils.capitalize(column.getVarName()),
                    org.apache.commons.lang3.StringUtils.capitalize(Objects.equals(column.getVarType(), "Integer") ? "int" : column.getVarType()),
                    column.getName()));
        }
        String method = String.format("" +
                "public List<%1$s> get%1$sList(ResultSet rs) throws SQLException {\n" +
                "    List<%1$s> data = new ArrayList<>();\n" +
                "    while (rs.next()) {\n" +
                "        %1$s bean = new %1$s();\n" +
                "%2$s" +
                "        data.add(bean);\n" +
                "    }\n" +
                "    return data;\n" +
                "}", CONFIG.beanType, setBean);

        System.out.println(method);
        System.out.println();
    }

    /**
     * Print code for converting an Excel sheet row into a bean instance
     *
     * @param startIndex
     */
    public void printSnippetRowToBean(int startIndex) {
        for (int i = 0; i < columns.size(); i++) {
            String var = columns.get(i).getVarName();
            String capVar = Character.toUpperCase(var.charAt(0)) + var.substring(1);
            String type = columns.get(i).getVarType();
            switch (type) {
                case "String":
                    System.out.printf("data.set%s(getStringCellValue(row, %d));%n", capVar, startIndex + i);
                    break;
                case "Integer":
                    System.out.printf("data.set%s(getIntegerCellValue(row, %d));%n", capVar, startIndex + i);
                    break;
                case "Double":
                    System.out.printf("data.set%s(getDoubleCellValue(row, %d));%n", capVar, startIndex + i);
                    break;
                case "Date":
                    System.out.printf("data.set%s(getDateCellValue(row, %d));%n", capVar, startIndex + i);
                    break;
                default:
                    throw new RuntimeException("Error: " + type);
            }
        }
    }

    public void printSnippetHeaderPropertyMap() {
        StringBuilder methods = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo column = columns.get(i);
            methods.append(String.format("    map.put(\"%s\", \"%s.%s\");\n", column.getVarName(), CONFIG.beanType, column.getVarName()));
        }
        String s = String.format("" +
                "public static LinkedHashMap<String, String> getHeaderPropertyMap%2$s() {\n" +
                "    LinkedHashMap<String, String> map = new LinkedHashMap<>();\n" +
                "%1$s" +
                "    return map;\n" +
                "}\n", methods, CONFIG.beanType);
        System.out.println(s);
    }

    public void printTableInfo() {
        System.out.println(CONFIG.tableName);
        for (int i = 0; i < columns.size(); i++) {
//            System.out.printf("%-3d%-20s%-20s%-20s%n", i, columns.get(i).getVarName(), columns.get(i).getVarType(), columns.get(i).getComment());
            System.out.printf("%-4d%s%n", i, columns.get(i));
        }
        System.out.println();
    }

    public <T> List<T> selectList(String sql, Class<T> type, Map<String, String> resultMap) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        List<T> objects = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (null != rs) {
            if (!rs.isBeforeFirst()) {
                throw new RuntimeException("Invalid query statement");
            }

            while (rs.next()) {
                T o = type.newInstance();
                for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                    type.getDeclaredField(entry.getKey()).set(o, rs.getObject(entry.getValue()));
                }
                objects.add(o);
            }
        }
        ps.close();
        return objects;
    }

    /**
     * Prints a method for instantiating a POJO class with calling its setters
     *
     * @param clazz   the class to instantiate
     * @param varName the name of the object variable
     */
    public static String snippetGetInstance(Class clazz, String varName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("" +
                "private static %1$s get%1$s() {\n" +
                "    %1$s %3$s = new %1$s();\n" +
                "%2$s" +
                "    return %3$s;\n" +
                "}\n", clazz.getSimpleName(), snippetBeanSetters(clazz, varName), varName));
        return sb.toString();
    }

    /**
     * Returns a snippet for instantiating a POJO class with calling its setters
     *
     * @param clazz   the class to instantiate
     * @param varName the name of the object variable
     */
    public static String snippetBeanSetters(Class clazz, String varName) {
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder setters = new StringBuilder();
        for (Field field : fields) {
            String fieldType = field.getType().getSimpleName();
            Object value = fieldType.equals("String") ? "\"Test\""
                    : fieldType.equals("Integer") ? 1
                    : fieldType.equals("Double") ? 0.1
                    : fieldType.equals("Long") ? "1L"
                    : fieldType.equals("BigDecimal") ? "BigDecimal.valueOf(0.1)"
                    : fieldType.equals("Date") ? "new Date()"
                    : null;
            setters.append(String.format("    %s.%s(%s);\n", varName, "set".concat(org.apache.commons.lang3.StringUtils.capitalize(field.getName())), value));
        }
        return setters.toString();
    }

    /**
     * Returns a sample object filled with sample data out of a POJO class. The POJO class can contain getter and setter methods or only member fields
     * no matter what their accessibility is. Java value types are also accepted for which it returns their corresponding sample values.
     *
     * @param type the template type out of which an object is created
     */
    public static <T> T sample(Class<T> type) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        switch (type.getSimpleName()) {
            case "Object":
            case "String":
                return type.cast("sample");
            case "Integer":
                return type.cast(1);
            case "Long":
                return type.cast(1L);
            case "Float":
                return type.cast(0.1f);
            case "Double":
                return type.cast(0.1);
            case "Date":
                return type.cast(new Date());
            case "Boolean":
                return type.cast(false);
            case "BigDecimal":
                return type.cast(BigDecimal.valueOf(0.1));
            default:
                if (type.isArray()) {
                    Object array = Array.newInstance(type.getComponentType(), 2);
                    Object val = sample(type.getComponentType()); // TODO: 5/21/19 Primitive types
                    Array.set(array, 0, val);
                    Array.set(array, 1, val);
                    return type.cast(array);
                }
        }

        T obj = type.newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object arg;
            switch (field.getType().getSimpleName()) {
                case "List":
                    String componentType = org.apache.commons.lang3.StringUtils.substringBetween(field.getGenericType().getTypeName(), "<", ">");
                    Object val;
                    if (componentType != null) {
                        Class<?> componentTypeClass = type.getClassLoader().loadClass(componentType);
                        val = sample(componentTypeClass);
                    } else {
                        val = sample(String.class);
                    }
                    arg = new ArrayList<>(Arrays.asList(val, val));
                    break;
                case "Map":
                    String componentTypes = org.apache.commons.lang3.StringUtils.substringBetween(field.getGenericType().getTypeName(), "<", ">");
                    Map map = new HashMap();
                    Class<?> keyType, valueType;
                    if (componentTypes != null) {
                        String[] types = StringUtils.splitAndTrim(componentTypes, ",", SplitRegex.DROPPED);
                        keyType = type.getClassLoader().loadClass(types[0]);
                        valueType = type.getClassLoader().loadClass(types[1]);
                    } else {
                        keyType = String.class;
                        valueType = String.class;
                    }
                    map.put(sample(keyType), sample(valueType));
                    arg = map;
                    break;
                default:
                    arg = sample(field.getType());
            }

            try {
                field.set(obj, arg);
            } catch (IllegalArgumentException e) {
                LOG.error(e.getMessage() + ", field: " + field.getGenericType().getTypeName() + " " + field.getName() + ", arg: " + arg.getClass().getName() + " " + arg.toString());
                throw e;
            }
        }
        return obj;
    }

    /**
     * Load a sample object out of a designated .class file.
     *
     * @param classPath the directory containing the .class file, e.g. "/home/user/downloads/classes/".
     * @param className the full name of the class, e.g. "com.company.Customer", and the "Customer.class" file should be put under "/home/user/downloads/classes/com/company/".
     * @return an object populated with sample values
     */
    public static Object sample(String classPath, String className) throws Exception {
        return sample(getClass(classPath, className));
    }

    public static Class getClass(String classPath, String className) throws Exception {
        File file = new File(classPath);
        URL url = file.toURI().toURL();
        URL[] urls = new URL[]{url};
        ClassLoader cl = new URLClassLoader(urls);
        return cl.loadClass(className);
    }

    public static List<Class> getClasses(List<String> classPaths, List<String> packages) throws Exception {
        List<Class> classes = new ArrayList<>();
        List<URL> urls = new ArrayList<>();
        for (int i = 0; i < classPaths.size(); i++) {
            urls.add(new File(classPaths.get(i)).toURI().toURL());
        }
        ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]));
        for (String classPath : classPaths) {
            for (String p : packages) {
                File dir = new File(classPath + "/" + p.replace(".", "/"));
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.isFile()) {
                            String className = p + "." + StringUtils.substringBefore(file.getName(), ".class");
                            classes.add(cl.loadClass(className));
                        }
                    }
                }
            }
        }
        return classes;
    }

    /**
     * Print information of the given table names
     *
     * @param tables
     */
    public static void printTableInfo(String... tables) {
        try {
            Coder automator = new Coder();
            automator.connectToDB();
            for (String table : tables) {
                automator.init(table);
                automator.printTableInfo();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getServletPaths(List<String> classPaths, List<String> packages) throws Exception {
        List<String> paths = new ArrayList<>();
        List<Class> classes = getClasses(classPaths, packages);
        for (Class cls : classes) {
            if (cls.isAnnotationPresent(Controller.class) || cls.isAnnotationPresent(RestController.class)) {
                Method[] methods = cls.getDeclaredMethods();
                for (Method method : methods) {
                    RequestMapping a = method.getAnnotation(RequestMapping.class);
                    paths.addAll(Arrays.asList(a.path()));
                }
            }
        }
        return paths;
    }

    // Private utility members go here below ===================================================

    private String columnNames(List<ColumnInfo> columns, boolean isOneLine) {
        StringBuilder builder = new StringBuilder();
        if (isOneLine) {
            columns.forEach(col -> builder.append(col.getName()).append(", "));
            builder.deleteCharAt(builder.length() - 1);
        } else {
            columns.forEach(col -> builder.append("            ").append(col.getName()).append(",\n"));
        }
        builder.deleteCharAt(builder.lastIndexOf(","));
        return builder.toString();
    }

    /**
     * Returns a comma-separated name list like <code>#{id},#{name},#{age}</code>
     */
    private String propertyNames(List<ColumnInfo> columns, String prefix, boolean isOneLine) {
        final String finalPrefix = prefix == null ? "" : prefix;
        StringBuilder builder = new StringBuilder();
        if (isOneLine) {
            if (CONFIG.handleTimeByDAO) {
                columns.forEach(col -> builder.append(ListUtils.contains(CONFIG.handleTimeByDAOOfTheseColumns, col.getName()) ? "now(), " : "#{" + finalPrefix + col.getVarName() + "}, "));
            } else {
                columns.forEach(col -> builder.append("#{" + finalPrefix + col.getVarName() + "}, "));
            }
            builder.delete(builder.length() - 2, builder.length());
        } else {
            if (CONFIG.handleTimeByDAO) {
                columns.forEach(col -> builder.append(ListUtils.contains(CONFIG.handleTimeByDAOOfTheseColumns, col.getName()) ? "            now(),\n" : "            #{" + finalPrefix + col.getVarName() + "},\n"));
            } else {
                columns.forEach(col -> builder.append("            #{" + finalPrefix + col.getVarName() + "},\n"));
            }
            builder.deleteCharAt(builder.lastIndexOf(","));
        }
        return builder.toString();
    }

    private String optionalColumns(String prefix, List<ColumnInfo> cols) {
        StringBuilder columnNames = new StringBuilder();
        if (CONFIG.handleTimeByDAO) {
            cols.forEach(col -> columnNames.append(ListUtils.contains(CONFIG.handleTimeByDAOOfTheseColumns, col.getName()) ? "            " + col.getName() + ",\n" : "            <if test=\"" + prefix + col.getVarName() + " != null\">" + col.getName() + ",</if>\n"));
        } else {
            cols.forEach(col -> columnNames.append("            <if test=\"" + prefix + col.getVarName() + " != null\">" + col.getName() + ",</if>\n"));
        }
        return columnNames.toString();
    }

    private String optionalProperties(String prefix, List<ColumnInfo> cols) {
        StringBuilder propertyNames = new StringBuilder();
        if (CONFIG.handleTimeByDAO) {
            cols.forEach(col -> propertyNames.append(ListUtils.contains(CONFIG.handleTimeByDAOOfTheseColumns, col.getName()) ? "            now(),\n" : "            <if test=\"" + prefix + col.getVarName() + " != null\">#{" + prefix + col.getVarName() + "},</if>\n"));
        } else {
            cols.forEach(col -> propertyNames.append("            <if test=\"" + prefix + col.getVarName() + " != null\">#{" + prefix + col.getVarName() + "},</if>\n"));
        }
        return propertyNames.toString();
    }

    private static String toVarName(String colName) {
        char[] varChars = colName.toCharArray();
        for (int i = 0; i < varChars.length; i++) {
            char c = varChars[i];
            if (c == '_') {
                varChars[i + 1] = String.valueOf(varChars[i + 1]).toUpperCase().charAt(0);
            }
        }
        return new String(varChars).replace("_", "");
    }

    private File writeToFile(Path path, String data) throws IOException {
        File file;
        if (CONFIG.filename == null) {
            file = path.toFile();
        } else {
            file = path.getParent().resolve(CONFIG.filename).toFile();
        }

        if (file.exists() && !CONFIG.overwrite) {
            System.out.printf("[Warn] File existing under \"%s\" and not created\n", path);
        } else {
            FileUtils.writeStringToFile(file, data, "utf-8");
            System.out.printf("[OK] File created under \"%s\"\n", path);
        }
        return file;
    }

    private String filePath(String dir) {
        return FileUtils.getProjectRoot().resolve("src/test/java").resolve(CONFIG.PACKAGE_BEAN).resolve(dir).toString();
    }

    private String getComment(ColumnInfo column) {
        return !CONFIG.showComment || column.getComment() == null || column.getComment().trim().isEmpty() ? "" : "//" + column.getComment();
    }

    /**
     * A convenient method for {@link #importFromFileToDatabase(String, String, String[], String)} for using with internal configuration file
     */
    public static void importFromFileToDatabase(String file, String table, String[] fields) throws SQLException, IOException, ClassNotFoundException {
        importFromFileToDatabase(file, table, fields, null);
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
     * @param file   the file to import from. The first row should be the table header. For .xlsx files you should append a sheet name to the file name to
     *               specify which sheet to use like this "test.xlsx.Sheet1"
     * @param table  the table to import to
     * @param fields the fields to import to, e.g. ["id", "name", "", "class_id[t_class.name->id]", "state[0:good,1:bad]"]. Note that the length
     *               of the fields provided must be equal to or greater than the number of columns in the file and the order of the fields must conform with
     *               the order of the columns. You can use empty strings "" to skip unwanted columns. You can also provide extra fields beyond those specified
     *               by the file columns, and the values of those extra fields are also provided in the form <code>field[value]</code>, such as
     *               <code>["id", "name", "", "class_id[t_class.name->id]", "state[0:good,1:bad]", "create_date[now()]"]</code>. Optionally,
     *               in case there are a huge amount of available values for the fields to be parsed and only a few values that you know should be parsed, you
     *               can specify those values like this "class_id[t_class.name[Class A,Class B,Class C,...]->id]", so the system will only look for those values
     *               for their corresponding codes, say, the ids of Class A, Class B, Class C, etc. to save time and space. No blank spaces allowed after comma
     *               in the strings provided in fields array.
     * @param config configuration file for {@link Coder#CONFIG}
     *
     */
    public static void importFromFileToDatabase(String file, String table, String[] fields, String config) throws SQLException, ClassNotFoundException, IOException {
        LOG.info("ImportFromFile started");
        Map<Integer, Map<String, Object>> fieldMap = new HashMap<>();
        String[] fieldsToImport = new String[fields.length];
        StringBuilder fieldsToImportStr = new StringBuilder();
        Coder coder = new Coder(config);
        coder.connectToDB();
        Connection connection = coder.getConnection();
        for (int i = 0; i < fields.length; i++) {
            String str = fields[i];
            if (str.isEmpty()) {
                fieldsToImport[i] = null;
                continue;
            }

            if (str.contains(".")) {
                String field1 = org.apache.commons.lang3.StringUtils.substringBefore(str, "[");
                String table2 = org.apache.commons.lang3.StringUtils.substringBetween(str, "[", ".");
                String field2;
                StringBuilder whereClause = new StringBuilder();
                if (str.contains("]->")) {
                    field2 = str.substring(str.indexOf(".") + 1, str.lastIndexOf("["));
                    String vals = str.substring(str.lastIndexOf("[") + 1, str.indexOf("]"));
                    String sqlVals = StringUtils.encloseSubstrings(vals, ",", SplitRegex.DROPPED, "'", ",");
                    whereClause.append(" WHERE ").append(field2).append(" IN (").append(sqlVals).append(")");
                } else {
                    field2 = org.apache.commons.lang3.StringUtils.substringBetween(str, ".", "-");
                }
                String field3 = org.apache.commons.lang3.StringUtils.substringBetween(str, ">", "]");
                PreparedStatement ps = connection.prepareStatement("SELECT " + field2 + ", " + field3 + " FROM " + table2 + whereClause);
                ResultSet rs = ps.executeQuery();
                Map<String, Object> map1 = new HashMap<>();
                while (rs.next()) {
                    map1.put(rs.getString(field2), rs.getObject(field3));
                }
                fieldMap.put(i, map1);
                fieldsToImport[i] = field1;
                rs.close();
                ps.close();
            } else if (str.contains(",")) {
                String[] pairs = org.apache.commons.lang3.StringUtils.substringBetween(str, "[", "]").split(",");
                Map<String, Object> map1 = new HashMap<>();
                for (String s : pairs) {
                    String[] pair = s.split(":");
                    map1.put(pair[1], Integer.valueOf(pair[0]));
                }
                fieldMap.put(i, map1);
                fieldsToImport[i] = org.apache.commons.lang3.StringUtils.substringBefore(str, "[");
            } else if (str.contains("[")) {
                fieldsToImport[i] = org.apache.commons.lang3.StringUtils.substringBefore(str, "[");
            } else {
                fieldsToImport[i] = str;
            }
            fieldsToImportStr.append(fieldsToImport[i]).append(",");
        }
        fieldsToImportStr.deleteCharAt(fieldsToImportStr.length() - 1);

        StringBuilder result = new StringBuilder("ImportFromFile conclusion:\n");
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ").append(table).append("(").append(fieldsToImportStr).append(") VALUES \n");
        int skippedRows = 0, importedRows = 0;
        if (file.contains(".xlsx")) {
            String filename = org.apache.commons.lang3.StringUtils.substringBeforeLast(file, ".");
            String sheetName = org.apache.commons.lang3.StringUtils.substringAfterLast(file, ".");
            Sheet sheet = new XSSFWorkbook(filename).getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Unspecified sheet for spreadsheet. Please specify sheet by appending sheet name to file name like this \"example.xlsx.Sheet1\"");
            }

            row:
            for (int i = 1; i < sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    skippedRows++;
                    result.append("Skipped row ").append(i).append(": Empty row\n");
                    continue;
                }
                StringBuilder rowBuilder = new StringBuilder();
                int lastCellNum = row.getLastCellNum();
                for (int j = 0; j < fieldsToImport.length; j++) {
                    String field = fieldsToImport[j];
                    if (field == null) {
                        continue;
                    }

                    String value;
                    if (j < lastCellNum) {
                        value = getCellValue(row.getCell(j));
                    } else {
                        value = org.apache.commons.lang3.StringUtils.substringBetween(fields[j], "[", "]");
                    }

                    if (value == null) {
                        value = "null";
                    } else if (!NumberUtils.isParsable(value)) {
                        Map<String, Object> map = fieldMap.get(j);
                        if (map != null) {
                            Object val = map.get(value);
                            if (val == null) {
                                skippedRows++;
                                result.append("Skipped row ").append(i).append(": No mapping found for field ").append("\"").append(field).append("\" value \"").append(value).append("\"\n");
                                continue row;
                            } else if (val instanceof String) {
                                value = "'" + val + "'";
                            } else {
                                value = val.toString();
                            }
                        } else if (!value.equals("now()")) {
                            value = "'" + value + "'";
                        }
                    }
                    rowBuilder.append(value).append(",");
                }
                sqlBuilder.append("(").append(rowBuilder.deleteCharAt(rowBuilder.length() - 1)).append("),\n");
                importedRows++;
            }
            sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length()).append(";");
        } else {
            throw new IllegalArgumentException("Unsupported file type!");
        }

        result.append("Rows imported: ").append(importedRows).append("\nRows skipped: ").append(skippedRows).append("\n");

        try (PreparedStatement ps = connection.prepareStatement(sqlBuilder.toString())) {
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException e) {
            LOG.debug(e.getMessage() + "\n" + sqlBuilder.toString());
            LOG.debug(result.toString());
            throw e;
        }

        connection.close();

        LOG.info("ImportFromFile finished");
        LOG.info(result.toString());
    }
}
