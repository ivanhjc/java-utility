package net.ivanhjc.utility.auto;

import net.ivanhjc.utility.auto.enums.AnnotationOption;
import net.ivanhjc.utility.data.StringUtils;
import net.ivanhjc.utility.file.PropertyLoader;
import org.apache.commons.text.WordUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Administrator on 2019/1/31 15:15.
 */
public class Config {
    final String JDBC_URL;
    final String USERNAME;
    final String PASSWORD;
    final String AUTHOR;
    final String PACKAGE_BEAN;
    final String PACKAGE_DAO;
    final String PACKAGE_SERVICE;
    final String PACKAGE_SERVICE_IMPL;
    final String PACKAGE_CONTROLLER;
    final String PACKAGE_COMMON;
    final String PACKAGE_ENUM;
    final String PACKAGE_APIRESULT;
    final Path PROJECT_ROOT;
    final Path DIR_BEAN;
    final Path DIR_MAPPER;
    final Path DIR_DAO;
    final Path DIR_SERVICE;
    final Path DIR_SERVICE_IMPL;
    final Path DIR_CONTROLLER;
    final Path DIR_COMMON;
    final Path DIR_ENUM;
    final Path DIR_APIRESULT;
    final String SUFFIX_BEAN;
    final String SUFFIX_MAPPER;
    final String SUFFIX_DAO;
    final String SUFFIX_SERVICE;
    final String SUFFIX_SERVICE_IMPL;
    final String SUFFIX_CONTROLLER;
    final String RESPONSE_ENTITY = "APIResult";
    final String PREFIX_TABLE;
    final String BASE_TYPE = "T";

    String tableName;
    String tableNameCamelCase;
    String tableAcronym;
    String beanType;
    String daoType; //Data access object
    String mapperName;
    String serviceType;
    String serviceImplType;
    String controllerType;
    String beanVar;
    String daoVar;
    String serviceVar;
    String beanName = "bean";
    String filename;
    String handleTimeByDAOOfTheseColumns = "create_time,create_date,update_time,update_date";

    boolean dropPrefix;
    boolean showComment;
    boolean overwrite;
    boolean includeDateRange;
    boolean mapUnderscoreToCamelCase;
    boolean handleTimeByDAO;

    AnnotationOption[] annotationOptions = {};

    Config() {
        this("coder.properties");
    }

    Config(String file) {
        Properties properties = PropertyLoader.loadProperties(file);
        JDBC_URL = properties.getProperty("datasource.url");
        USERNAME = properties.getProperty("datasource.username");
        PASSWORD = properties.getProperty("datasource.password");

        AUTHOR = properties.getProperty("author");
        PROJECT_ROOT = Paths.get(properties.getProperty("project.root"));

        String[] module = {""};
        PACKAGE_BEAN = getPackage(properties, "package.bean", module);
        DIR_BEAN = getDir(module[0], PACKAGE_BEAN);
        PACKAGE_DAO = getPackage(properties, "package.dao", module);
        DIR_DAO = getDir(module[0], PACKAGE_DAO);
        PACKAGE_SERVICE = getPackage(properties, "package.service", module);
        DIR_SERVICE = getDir(module[0], PACKAGE_SERVICE);
        PACKAGE_SERVICE_IMPL = getPackage(properties, "package.service.impl", module);
        DIR_SERVICE_IMPL = getDir(module[0], PACKAGE_SERVICE_IMPL);
        PACKAGE_CONTROLLER = getPackage(properties, "package.controller", module);
        DIR_CONTROLLER = getDir(module[0], PACKAGE_CONTROLLER);
        PACKAGE_COMMON = getPackage(properties, "package.common", module);
        DIR_COMMON = getDir(module[0], PACKAGE_COMMON);
        PACKAGE_ENUM = getPackage(properties, "package.enum", module);
        DIR_ENUM = getDir(module[0], PACKAGE_ENUM);
        PACKAGE_APIRESULT = getPackage(properties, "package.APIResult", module);
        DIR_APIRESULT = getDir(module[0], PACKAGE_APIRESULT);

        String resDir = "src/main/resources";
        String mapperDir = properties.getProperty("resources.mapper");
        if (mapperDir.contains(":")) {
            String[] temp = mapperDir.split(":");
            DIR_MAPPER = PROJECT_ROOT.resolve(temp[0]).resolve(resDir).resolve(temp[1]);
        } else {
            DIR_MAPPER = PROJECT_ROOT.resolve(resDir).resolve(mapperDir);
        }

        SUFFIX_BEAN = getProperty(properties, "suffix.bean");
        SUFFIX_MAPPER = getProperty(properties, "suffix.mapper");
        SUFFIX_DAO = getProperty(properties, "suffix.dao");
        SUFFIX_SERVICE = getProperty(properties, "suffix.service");
        SUFFIX_SERVICE_IMPL = getProperty(properties, "suffix.service.impl");
        SUFFIX_CONTROLLER = getProperty(properties, "suffix.controller");

        PREFIX_TABLE = getProperty(properties, "prefix.table");
    }

    private String getPackage(Properties properties, String property, String[] module) {
        String pkg = properties.getProperty(property);
        if (pkg.contains(":")) {
            String[] a = pkg.split(":");
            module[0] = a[0];
            return a[1];
        }
        return pkg;
    }

    private Path getDir(String module, String pkg) {
        return PROJECT_ROOT.resolve(module).resolve("src/main/java").resolve(pkg.replace('.', '/'));
    }

    private String getProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (org.apache.commons.lang3.StringUtils.isBlank(value))
            value = "";
        return value;
    }

    void init(String tableName) {
        this.tableName = tableName;
        this.tableNameCamelCase = StringUtils.snakeToCamel(dropPrefix && tableName.substring(0, PREFIX_TABLE.length()).equals(PREFIX_TABLE) ? tableName.substring(PREFIX_TABLE.length()) : tableName);
        this.tableAcronym = getValidAlias(WordUtils.initials(tableName, '_'));

        beanType = tableNameCamelCase + SUFFIX_BEAN;
        daoType = tableNameCamelCase + SUFFIX_DAO;
        mapperName = tableNameCamelCase + SUFFIX_MAPPER;
        serviceType = tableNameCamelCase + SUFFIX_SERVICE;
        serviceImplType = tableNameCamelCase + SUFFIX_SERVICE_IMPL;
        controllerType = tableNameCamelCase + SUFFIX_CONTROLLER;

        beanVar = org.apache.commons.lang3.StringUtils.uncapitalize(beanType);
        daoVar = org.apache.commons.lang3.StringUtils.uncapitalize(daoType);
        serviceVar = org.apache.commons.lang3.StringUtils.uncapitalize(serviceType);
    }

    /**
     * Removing a substring from the beginning of the table name. The prefix to be removed is specified by "prefix.table" property.
     * Should be used before calling {@link Coder#init(String)} or it won't take effect.
     */
    public Config dropPrefix(boolean b) {
        this.dropPrefix = b;
        return this;
    }

    /**
     * Whether or not showing comments from table columns for bean properties, default to false.
     */
    public Config showComment(boolean b) {
        this.showComment = b;
        return this;
    }

    /**
     * Whether or not to overwrite an existing file with the generated file if their names are the same, default to false.
     */
    public Config overwrite(boolean b) {
        this.overwrite = b;
        return this;
    }

    /**
     * Whether or not include searching in date range conditions. Supported date range types include finding records: <br>
     * 1. before or after a given date or between two given dates <br>
     * 2. in the last N days before a given date, including this date <br>
     * 3. in certain year, month, or on a day
     */
    public Config includeDateRange(boolean b) {
        this.includeDateRange = b;
        return this;
    }

    /**
     * Setting mapUnderscoreToCamelCase true means there's no need for resultMaps to convert snake-cased columns to camel-cased properties when making queries
     */
    public Config mapUnderscoreToCamelCase(boolean b) {
        this.mapUnderscoreToCamelCase = b;
        return this;
    }

    /**
     * Let DAOs handle the insertion of date or time values by using the current time so that users don't need to set these values in Java codes,
     * such as the time of creating or updating a row. By default columns named "create_time,create_date,update_time,update_date" are used to testify
     * if they should be handled automatically. You can specify other columns names to be handled this way by providing different names to the columns
     * parameter.
     *
     * @param b       true: use DAO, false: use Java
     * @param columns comma-separated list
     */
    public Config handleTimeByDAO(boolean b, String columns) {
        this.handleTimeByDAO = b;
        if (columns != null) {
            this.handleTimeByDAOOfTheseColumns = columns;
        }
        return this;
    }

    /**
     * Specifies the annotations used in the generated DAOs, such as "@Repository", "@Mapper", etc. in order to generate the codes properly.
     * If not specified no annotation will be used.
     *
     * @param annotations the annotation used for the DAOs
     */
    public Config daoAnnotations(AnnotationOption... annotations) {
        this.annotationOptions = annotations;
        return this;
    }

    /**
     * Sets a different name for the generated files
     */
    public Config setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    private boolean isKeyword(String str) {
        switch (str) {
            case "to":
            case "by":
                return true;
        }
        return false;
    }

    private String getValidAlias(String str) {
        return isKeyword(str) ? str.concat("1") : str;
    }
}
