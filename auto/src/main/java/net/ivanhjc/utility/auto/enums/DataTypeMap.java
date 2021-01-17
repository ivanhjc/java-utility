package net.ivanhjc.utility.auto.enums;

public enum DataTypeMap {
    INT("INT", "Integer", "42"),
    DOUBLE("DOUBLE", "Double", "42.01"),
    BIGINT("BIGINT", "Long", "42L"),
    TINYINT("TINYINT", "Integer", "0"),
    DECIMAL("DECIMAL", "BigDecimal", "BigDecimal.ZERO"),
    VARCHAR("VARCHAR", "String", "\"test\""),
    CHAR("CHAR", "String", "test"),
    TEXT("TEXT", "String", "test"),
    LONGTEXT("LONGTEXT", "String", "test"),
    DATE("DATE", "Date", "new Date()"),
    DATETIME("DATETIME", "Date", "new Date()"),
    BOOLEAN("BOOLEAN", "Boolean", "false"),
    JSON("JSON", "String", "test");

    public final String sqlType;
    public final String javaType;
    public final String value;

    DataTypeMap(String sqlType, String javaType, String value) {
        this.sqlType = sqlType;
        this.javaType = javaType;
        this.value = value;
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
