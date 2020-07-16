package net.ivanhjc.utility.auto.enums;

/**
 * @author Administrator on 2018/7/10 17:23.
 */
public enum MyBatisToken {
    SORT_FIELD("stfd"),
    /**
     * This parameter has only two values: DESC, ASC
     */
    SORT_ORDER("stod"),
    START_POINT("stpt"),
    /**
     * Name of parameter used to make a query of the last n days from a specific date (this date is included).
     * For example, to find data of the last 7 days from today, specify "lastDays = 7" and "stpt = #today#".
     */
    LAST_DAYS("lds"),
    LIMIT("lmt"),
    /**
     * Foreach collection name
     */
    COLLECTION("beans"),
    /**
     * Foreach item name
     */
    ITEM("item")
    ;

    public final String NAME;

    MyBatisToken(String name) {
        this.NAME = name;
    }
}
