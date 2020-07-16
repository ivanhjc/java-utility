package net.ivanhjc.utility.auto.enums;

import net.ivanhjc.utility.auto.ColumnConfigs;
import net.ivanhjc.utility.auto.ColumnInfo;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specifies which columns to be included in the result
 */
public enum ColumnsOption {
    /**
     * All columns
     */
    ALL,

    /**
     * No columns
     */
    NONE,

    /**
     * Only columns that have a primary key constraint
     */
    PK,

    /**
     * Only columns that have a unique key constraint
     */
    UNIQUE,

    /**
     * Only columns that do not have a unique constraint
     */
    NON_UNIQUE,

    /**
     * Include these columns. Used together with {@link #setFilter(String)}
     */
    INCLUDE,

    /**
     * Include all but these columns. Used together with {@link #setFilter(String)}
     */
    EXCLUDE,

    /**
     * Include all but these types of columns (provided as Java types). Used together with {@link #setFilter(String)}
     */
    EXCLUDE_TYPE;

    private String filter;

    /**
     * Sets the string that indicates the columns to be selected
     *
     * @param filter string array separated by comma. Each element can be an integer which is the column index starting from 0 or
     *               a string which is the column name
     */
    public ColumnsOption setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * @param columns original columns
     * @return selected columns
     */
    public List<ColumnInfo> getColumns(List<ColumnInfo> columns) {
        List<ColumnInfo> result = new ArrayList<>();
        String[] cols = filter == null ? new String[]{} : filter.split(",");
        switch (this) {
            case ALL:
                return columns;
            case NONE:
                return result;
            case PK:
                return columns.stream().filter(col -> "PRI".equals(col.getKey())).collect(Collectors.toList());
            case UNIQUE:
                return columns.stream().filter(col -> "UNI".equals(col.getKey())).collect(Collectors.toList());
            case NON_UNIQUE:
                return columns.stream().filter(col -> !("PRI".equals(col.getKey()) || "UNI".equals(col.getKey()))).collect(Collectors.toList());
            case INCLUDE:
                for (String s : cols) {
                    if (NumberUtils.isDigits(s))
                        result.add(columns.get(Integer.valueOf(s)));
                    else
                        for (ColumnInfo col : columns) {
                            if (s.equals(col.getName())) {
                                result.add(col);
                                break;
                            }
                        }
                }
                return result;
            case EXCLUDE:
                out:
                for (int i = 0; i < columns.size(); i++) {
                    for (String s : cols) {
                        if (NumberUtils.isDigits(s) && Integer.valueOf(s).equals(i) ||
                                !NumberUtils.isDigits(s) && s.equals(columns.get(i).getName())) {
                            continue out;
                        }
                    }
                    result.add(columns.get(i));
                }
                return result;
            case EXCLUDE_TYPE:
                return columns.stream().filter(col -> {
                    for (String s : cols) {
                        if (s.equalsIgnoreCase(col.getVarType()))
                            return false;
                    }
                    return true;
                }).collect(Collectors.toList());
        }
        return columns;
    }

    public String getIfTest(List<ColumnInfo> columns, ColumnConfigs configs) {
        StringBuilder builder = new StringBuilder();
        this.getColumns(columns).forEach(col -> builder.append(col.getTestSnippetForFind(configs)));
        return builder.toString();
    }
}
