package net.ivanhjc.utility.auto;

import net.ivanhjc.utility.auto.enums.CustomColumnType;
import net.ivanhjc.utility.auto.enums.SQLDataTypes;
import net.ivanhjc.utility.auto.enums.MyBatisToken;
import net.ivanhjc.utility.auto.enums.RandomGenerators;
import net.ivanhjc.utility.data.EscapeTypes;
import net.ivanhjc.utility.data.ListUtils;
import net.ivanhjc.utility.data.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Properties of a database column
 */
public class ColumnInfo {
    /**
     * Column name
     */
    private String name;
    /**
     * Column type
     */
    private String type;
    /**
     * If it's nullable
     */
    private Boolean isNullable;
    /**
     * Maximum allowed characters if the column is of varchar type
     */
    private Long characterMaximumLength;
    /**
     * The constraint key of the column. May be null.
     */
    private String key;
    /**
     * The comment of the column. May be null.
     */
    private String comment;
    /**
     * The variable name of the column used in Java
     */
    private String varName;
    /**
     * The variable type of the column used in Java
     */
    private String varType;
    /**
     * The Java type of this column
     */
    private Class javaType;
    /**
     * A generator for generating a proper random value for this column according to its properties such as type,
     * nullable, length, etc.
     */
    private RandomGenerator randomGenerator;
    /**
     * Whether this column contains characters of multiple languages
     */
    private boolean isMultiLang;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCharacterMaximumLength() {
        return characterMaximumLength;
    }

    public void setCharacterMaximumLength(Long characterMaximumLength) {
        this.characterMaximumLength = characterMaximumLength;
    }

    public Boolean getNullable() {
        return isNullable;
    }

    public void setNullable(Boolean nullable) {
        isNullable = nullable;
    }

    public Boolean isNullable() {
        return isNullable;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getVarType() {
        return varType;
    }

    public void setVarType(String varType) {
        this.varType = varType;
    }

    public Class getJavaType() {
        return javaType;
    }

    public void setJavaType(Class javaType) {
        this.javaType = javaType;
    }

    public String getVarNameCap() {
        return StringUtils.capitalize(varName);
    }

    public String getField(String colPrefix) {
        return SQLDataTypes.isDate(type) ? "DATE_FORMAT(" + colPrefix.concat(name) + ", '%Y-%m-%d %H:%i:%s') AS " + varName :
                Objects.equals(name, varName) ? colPrefix.concat(name) : colPrefix.concat(name) + " AS " + varName;
    }

    public String getTestSnippetForFind(ColumnConfigs configs) {
        String prefixedCol = configs.getColPrefix().concat(name);
        String prefixedVar = configs.getVarPrefix().concat(varName);
        String prefixedStartPoint = configs.getVarPrefix().concat(varName.concat(MyBatisToken.START_POINT.NAME));
        String prefixedLastDays = configs.getVarPrefix().concat(varName.concat(MyBatisToken.LAST_DAYS.NAME));
        String snippet;
        switch (varType) {
            case "String":
                if (comment.startsWith(CustomColumnType.STRING_ARRAY.NAME)) {
                    String element = org.apache.commons.lang3.StringUtils.substringBetween(comment, "[", "]");
                    prefixedVar = configs.getVarPrefix().concat(element);
                    snippet = "            <if test=\"" + prefixedVar + " != null and " + prefixedVar + " != ''\">AND FIND_IN_SET(#{" + prefixedVar + "}, " + prefixedCol + ")</if>\n";
                } else {
                    snippet = "            <if test=\"" + prefixedVar + " != null and " + prefixedVar + " != ''\">AND " + prefixedCol + " LIKE '%${" + prefixedVar + "}%'</if>\n";
                }
                break;
            case "Date":
                snippet = "" +
                        "            <if test=\"" + prefixedVar + " != null\">AND " + prefixedCol + " LIKE '%${" + prefixedVar + "}%'</if>\n" + (configs.isIncludeDateRange() ?
                        "            <if test=\"" + prefixedVar + "1 != null\">AND " + prefixedCol + " >= #{" + prefixedVar + "1}</if>\n" +
                                "            <if test=\"" + prefixedVar + "2 != null\">AND " + prefixedCol + " &lt;= #{" + prefixedVar + "2}</if>\n" +
                                "            <if test=\"" + prefixedStartPoint + " != null\">AND DATEDIFF(#{" + prefixedStartPoint + "}, " + prefixedCol + ") &lt; #{" + prefixedLastDays + "} AND DATEDIFF(#{" + prefixedStartPoint + "}, " + prefixedCol + ") >= 0</if>\n"
                        : "");
                break;
            default:
                snippet = "            <if test=\"" + prefixedVar + " != null\">AND " + prefixedCol + " = #{" + prefixedVar + "}</if>\n";
        }

        if (configs.getTestFieldList() != null && !configs.getTestFieldList().isEmpty() && configs.getTestFieldList().contains(this)) {
            snippet += "            <if test=\"" + prefixedVar + "List != null and !" + prefixedVar + "List.isEmpty()\">AND " + prefixedCol + " IN <foreach collection=\"" + prefixedVar + "List\" item=\"" + varName + "\" open=\"(\" separator=\",\" close=\")\">#{" + varName + "}</foreach></if>\n";
        }

        if (configs.getTestFieldListStr() != null && !configs.getTestFieldListStr().isEmpty() && configs.getTestFieldListStr().contains(this)) {
            snippet += "            <if test=\"" + prefixedVar + "ListStr != null and " + prefixedVar + "ListStr != ''\">AND find_in_set(" + prefixedCol + ", #{" + prefixedVar + "ListStr})</if>\n";
        }
        return snippet;
    }

    public String getTestSnippetForUpdate(String prefix) {
        if (ListUtils.contains("createBy,createName,createTime,createDate", varName)) {
            return "";
        }

        if (ListUtils.contains("updateTime,updateDate", varName)) {
            return "            " + name + " = now(),\n";
        }

        switch (varType) {
            case "String":
                return String.format("            <if test=\"%1$s != null and %1$s != ''\">%2$s = #{%1$s},</if>\n", prefix + varName, name);
            default:
                return String.format("            <if test=\"%1$s != null\">%2$s = #{%1$s},</if>\n", prefix + varName, name);
        }
    }

    public String getTestSnippetForInsertOrUpdate() {
        String format;
        switch (varType) {
            case "String":
                format = "%3$12s<if test=\"%1$s != null and %1$s != ''\">%2$s = values(%2$s),</if>\n";
                break;
            default:
                format = "%3$12s<if test=\"%1$s != null\">%2$s = values(%2$s),</if>\n";
        }
        return String.format(format, varName, name, "");
    }

    public String getTestSnippetForInsertOrUpdateList() {
        return String.format("%3$12s%2$s = values(%2$s)\n", varName, name, "");
    }

    public RandomGenerator getRandomGenerator() {
        if (randomGenerator != null) {
            return randomGenerator;
        }

        return randomGenerator = () -> {
            boolean generate = !isNullable || Math.random() > 0.2;
            if (generate) {
                if (javaType == String.class) {
                    RandomStringGenerator generator;
                    if (isMultiLang) {
                        generator = RandomGenerators.RANDOM_STRING_SELECT_FROM_FILE_GENERATOR;
                    } else {
                        generator = new RandomStringGenerator.Builder()
                                .withinRange(new char[]{'0', '9'}, new char[]{'A', 'z'})
                                .build();
                    }
                    return generator.generate(1, characterMaximumLength.intValue());
                }
                return RandomGenerators.TYPE_GENERATOR_MAP.get(javaType).generate();
            }
            return null;
        };
    }

    public void setRandomGenerator(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public boolean isMultiLang() {
        return isMultiLang;
    }

    public void setMultiLang(boolean multiLang) {
        isMultiLang = multiLang;
    }

    private DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public String randomSQLValue() {
        Object value = getRandomGenerator().generate();
        if (value instanceof String) {
            return "'" + EscapeTypes.SQL.escape((String) value) + "'";
        } else if (value instanceof DateTime) {
            return "'" + dateTimeFormatter.print((DateTime) value) + "'";
        } else {
            return String.valueOf(value);
        }
    }
}
