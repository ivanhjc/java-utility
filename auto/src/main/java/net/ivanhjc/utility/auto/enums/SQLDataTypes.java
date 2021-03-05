package net.ivanhjc.utility.auto.enums;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public enum SQLDataTypes {
    INT("INT", Integer.class, "42"),
    DOUBLE("DOUBLE", Double.class, "42.01"),
    BIGINT("BIGINT", Long.class, "42L"),
    TINYINT("TINYINT", Integer.class, "0"),
    DECIMAL("DECIMAL", BigDecimal.class, "BigDecimal.ZERO"),
    VARCHAR("VARCHAR", String.class, "\"test\""),
    CHAR("CHAR", String.class, "test"),
    TEXT("TEXT", String.class, "test"),
    LONGTEXT("LONGTEXT", String.class, "test"),
    DATE("DATE", DateTime.class, "new Date()"),
    DATETIME("DATETIME", DateTime.class, "new Date()"),
    BOOLEAN("BOOLEAN", Boolean.class, "false"),
    JSON("JSON", String.class, "test");

    public final String SQL_TYPE;
    public final Class JAVA_TYPE;
    public final String DEFAULT_VALUE;

    SQLDataTypes(String SQL_TYPE, Class JAVA_TYPE, String DEFAULT_VALUE) {
        this.SQL_TYPE = SQL_TYPE;
        this.JAVA_TYPE = JAVA_TYPE;
        this.DEFAULT_VALUE = DEFAULT_VALUE;
    }

    public static boolean isDate(String type) {
        return valueOf(type.toUpperCase()) == DATETIME || valueOf(type.toUpperCase()) == DATE;
    }

    public static String getDataType(String rawType) {
        String temp = rawType;
        if ("TINYINT(1)".equalsIgnoreCase(rawType)) {
            temp = "BOOLEAN";
        } else if (rawType.contains("(")) {
            temp = rawType.substring(0, rawType.indexOf('('));
        }
        return temp.toUpperCase();
    }
}
