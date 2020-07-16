package net.ivanhjc.utility.auto.enums;

/**
 * @author Administrator on 2018/7/10 17:23.
 */
public enum CustomColumnType {
    /**
     * Comma-separated string array
     */
    STRING_ARRAY("StringArray")
    ;

    public final String NAME;

    CustomColumnType(String name) {
        this.NAME = name;
    }
}
