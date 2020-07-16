package net.ivanhjc.utility.data;

/**
 * A set of predefined regular expressions
 */
public enum SplitRegex {

    /**
     * The delimiter is included and positioned ahead of each substring
     */
    AHEAD("(?=%s)"),

    /**
     * The delimiter is included and positioned behind each substring
     */
    BEHIND("(?<=%s)"),

    /**
     * The delimiter is included and positioned independently between the substrings
     */
    INDEPENDENT("((?<=%1$s)|(?=%1$s))"),

    /**
     * The delimiter is dropped
     */
    DROPPED("%s");

    String regex;

    SplitRegex(String regex) {
        this.regex = regex;
    }
}