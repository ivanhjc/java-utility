package net.ivanhjc.utility.data;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Ivan Huang on 2017/6/27
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * Convert a string to capitalized form with all other letters to lowercase, e.g. hello, hELlo -> Hello
     *
     * @param str the string to convert, may be null or empty, returns as is in these cases
     * @see Character#isTitleCase(char)
     */
    public static String toTitleCase(String str) {
        return str == null || str.isEmpty() ? str
                : str.length() == 1 ? str.toUpperCase()
                : str.substring(0, 1).toUpperCase().concat(str.substring(1).toLowerCase());
    }

    /**
     * Convert a string from camel casing (e.g. "wordFamily" or "WordFamily") to snake casing (e.g. "word_family"). Null
     * or empty strings are not accepted.
     *
     * @param str The string to convert
     * @return The converted string
     */
    public static String camelToSnake(String str) {
        String[] strings = str.split("(?=[A-Z])");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i < strings.length - 1)
                stringBuilder.append(strings[i].toLowerCase().concat("_"));
            else
                stringBuilder.append(strings[i].toLowerCase());
        }
        return stringBuilder.toString();
    }

    /**
     * Converts a string from snake casing to camel casing where every word is capitalized, e.g.
     * <pre>{@code
     * word_family, Word_FamILy -> WordFamily
     * }
     * </pre>
     *
     * @param str may be null or empty, returns as is in such cases
     * @return the converted string
     */
    public static String snakeToCamel(String str) {
        if (str == null || str.trim().isEmpty())
            return str;

        String[] parts = str.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            builder.append(toTitleCase(part));
        }
        return builder.toString();
    }

    public static String[] camelToSnake(String[] arr) {
        String[] arr1 = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            arr1[i] = camelToSnake(arr[i]);
        }
        return arr1;
    }

    /**
     * Check if a string contains only letters and digits
     *
     * @param str The string to check. It may be null or empty and false is returned in these cases.
     */
    public static boolean isLettersAndDigits(String str) {
        if (str == null || str.trim().isEmpty())
            return false;

        for (char c : str.toCharArray()) {
            if (!Character.isLetterOrDigit(c))
                return false;
        }
        return true;
    }

    /**
     * Checks if a string is an integer
     *
     * @param str the string to check, may be null or empty, returns false in such cases
     * @see org.apache.commons.lang3.math.NumberUtils#isDigits(String)
     * @see org.apache.commons.lang3.math.NumberUtils#isNumber(String)
     * @see org.apache.commons.lang3.math.NumberUtils#isParsable(String)
     * @see org.apache.commons.lang3.StringUtils#isNumeric(CharSequence)
     * @see org.apache.commons.lang3.StringUtils#isNumericSpace(CharSequence)
     */
    public static boolean isInteger(String str) {
        if (str == null || str.trim().isEmpty())
            return false;

        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

    /**
     * Checks if a string is a valid hexadecimal number
     *
     * @param str the string to check, may be null or empty and returns false in these cases. May be with or without the "0x" or "0X" prefix.
     */
    public static boolean isHex(String str) {
        if (str == null || str.trim().isEmpty())
            return false;
        if (str.length() > 2 && "0x".equalsIgnoreCase(str.substring(0, 2)))
            str = str.substring(2);

        for (char c : str.toCharArray()) {
            if (Character.digit(c, 16) == -1)
                return false;
        }
        return true;
    }

    /**
     * Splits a string to an array of equal-length strings, e.g. "10001001110" -> ["1000", "1001", "110]
     *
     * @param str  the string to split
     * @param len  the length by which the string will be split into sub-strings, must be greater than 0
     * @param type the direction in which the splitting starts, 0 - backwards, 1 - forwards, other - returning null
     * @return an array of equal-length substrings, or null when str is null or len is not greater than 0 or dir is neither 0 nor 1
     */
    public static String[] splitEqually(String str, int len, int type) {
        if (str == null || len <= 0)
            return null;

        int groups = (str.length() + len - 1) / len;
        String[] strs = new String[groups];
        switch (type) {
            case 0:
                for (int i = 0; i < groups; i++) {
                    int beginIndex = str.length() - len * (i + 1);
                    int endIndex = beginIndex + len;
                    if (beginIndex < 0)
                        beginIndex = 0;
                    strs[groups - i - 1] = str.substring(beginIndex, endIndex);
                }
                break;
            case 1:
                for (int i = 0; i < groups; i++) {
                    int beginIndex = len * i;
                    int endIndex = beginIndex + len;
                    if (endIndex > str.length())
                        endIndex = str.length();
                    strs[i] = str.substring(beginIndex, endIndex);
                }
                break;
            default:
                strs = null;
        }
        return strs;
    }

    /**
     * Replaces a substring of a string builder with a specified string
     *
     * @param builder     the string builder, can't be null
     * @param search      the substring to be replaced
     * @param replacement the specified string
     * @return the original string builder
     */
    public static StringBuilder replace(StringBuilder builder, String search, String replacement) {
        int start = builder.indexOf(search);
        if (start == -1)
            return builder;
        int end = start + search.length();
        return builder.replace(start, end, replacement);
    }

    /**
     * Replaces all occurrences of given strings (searches) in a string with corresponding replacements provided with a map
     *
     * @param builder the original string
     * @param map     the search-replacement mapping
     * @return the altered string of the original
     */
    public static StringBuilder replace(StringBuilder builder, Map<String, String> map) {
        // TODO: 2019/4/8
        return null;
    }

    /**
     * Splits a string by a specified delimiter and a regular expression
     *
     * @param str       the string to split
     * @param delimiter the delimiter to use, "\" is escaped by "\\\\"
     * @param regex     the regular expression for splitting
     * @return the split results
     * @see SplitRegex
     */
    public static String[] split(String str, String delimiter, SplitRegex regex) {
        return str.split(String.format(regex.regex, delimiter));
    }

    public static String[] splitAndTrim(String str, String delimiter, SplitRegex regex) {
        String[] strs = split(str, delimiter, regex);
        List<String> trimmed = new ArrayList<>();
        for (String s : strs) {
            if (s.trim().isEmpty())
                continue;
            trimmed.add(s.trim());
        }
        return trimmed.toArray(new String[0]);
    }

    /**
     * Returns the escaped form of a character when it should be used as is in regular expressions
     *
     * @param c the character to be escaped
     * @return the escaped form
     */
    public static String escape(char c) {
        switch (c) {
            case '|':
                return "\\|";
            case '\\':
                return "\\\\";
            case '.':
                return "\\.";
            default:
                return String.valueOf(c);
        }
    }

    /**
     * Returns the escaped form of a string when it should be used as is in regular expressions
     *
     * @param str the character to be escaped
     * @return the escaped form
     */
    public static String escape(String str) {
        char[] chars = str.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : chars) {
            builder.append(escape(c));
        }
        return builder.toString();
    }

    /**
     * Encloses each substring of a string with a specified string. The substrings are divided by a specified delimiter
     * and they may or may not contain the delimiter according to the specified regular expression to use.
     *
     * @param str          the original string to operate on, can't be null
     * @param delimiter    the delimiter by which the original string is split
     * @param regex        the regular expression by which the original string is split
     * @param encStr       the string which will enclose the substrings
     * @param newDelimiter the new delimiter for the enclosed substrings
     * @return e.g. 1,2,3 -> "1","2","3"
     */
    public static String encloseSubstrings(String str, String delimiter, SplitRegex regex, String encStr, String newDelimiter) {
        String[] subStrs = split(str, delimiter, regex);
        StringBuilder builder = new StringBuilder();
        for (String subStr : subStrs) {
            builder.append(encStr).append(subStr).append(encStr).append(newDelimiter);
        }
        builder.delete(builder.length() - delimiter.length(), builder.length());
        return builder.toString();
    }

    /**
     * Convert a string of Unicode code points to readable characters. Is error-tolerated, which means
     * inconvertible code points are reserved as is.
     *
     * @param codePoints e.g. "\u9000\u62BC\u91D1\u5931\u8D25\u901A\u77E5", can't be null or empty.
     * @return the translated string
     * @see <a href="https://docs.oracle.com/javase/specs/jls/se9/html/jls-3.html#jls-3.1">JLS Chapter 3. Lexical Structure</a>
     * @see <a href="https://www.unicode.org/charts/">Character Code Charts</a>
     */
    public static String translateCodePoints(String codePoints) {
        String[] codeStrs = split(codePoints, "\\\\u", SplitRegex.AHEAD);
        StringBuilder builder = new StringBuilder();
        for (String codeStr : codeStrs) {
            if (codeStr.trim().isEmpty())
                continue;

            String prefix, digits, rest = null;
            prefix = codeStr.substring(0, 2);
            if (codeStr.length() > 6) {
                digits = codeStr.substring(2, 6);
                rest = codeStr.substring(6);
            } else {
                digits = codeStr.substring(2);
            }

            int codePoint;
            if (isHex(digits) && Character.isValidCodePoint(codePoint = Integer.valueOf(digits, 16))) {
                builder.append((char) codePoint);
            } else {
                builder.append(prefix.concat(digits));
            }

            if (rest != null) {
                builder.append(rest);
            }
        }
        return builder.toString();
    }

    /**
     * Convert a string to a sequence of Unicode code points
     *
     * @param str the string to convert, can't be null or empty
     * @return the converted string
     */
    public static String toCodePoints(String str) {
        StringBuilder builder = new StringBuilder();
        char[] chars = str.toCharArray();
        for (char ch : chars) {
            builder.append("\\u").append(leftPad(Integer.toHexString(ch), 4, '0'));
        }
        return builder.toString();
    }

    /**
     * Formats a {@link java.sql.ResultSet} object like a table
     *
     * @param resultSet the data to format
     * @return the formatted string
     */
    public static String formatTable(ResultSet resultSet) throws SQLException {
        resultSet.last();
        int rowNum = resultSet.getRow();
        String[][] table = new String[rowNum + 1][];
        resultSet.beforeFirst();
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int colNum = rsmd.getColumnCount();
        String[] headers = new String[colNum];
        for (int i = 1; i <= colNum; i++) {
            headers[i - 1] = rsmd.getColumnName(i);
        }
        table[0] = headers;

        int rowIdx = 1;
        while (resultSet.next()) {
            String[] row = new String[colNum];
            for (int i = 1; i <= colNum; i++) {
                row[i - 1] = String.valueOf(resultSet.getObject(i));
            }
            table[rowIdx++] = row;
        }
        return formatTable(table, colNum, null, 2, true);
    }

    /**
     * Format a 2D array into a table with fixed-width columns
     *
     * @param table  The string to be formatted. Before formatting it should contain multiple lines each of which represents a row of the table
     *               and has multiple cells separated with a certain type of delimiter such as space " ", comma ",", tab "\t", or vertical line
     *               "|" etc. For example, the following strings are formattable: {@code "id,name,age,sex\n1,Bob,12,M\n2,Alice,8,F\n"}
     * @param colNum The number of columns of the target table, if null each row can have different number of columns
     * @param oldDel The delimiter used to separate columns of the original table
     * @param newDel The delimiter used to separate columns of the new table, if null an empty string will be used as the new delimiter
     * @param margin The number of spaces between columns of the new table
     * @return The formatted table string
     */
    public static String formatTable(List<String> table, Integer colNum, String oldDel, String newDel, int margin) {
        String[][] tab = new String[table.size()][];
        for (int i = 0; i < table.size(); i++) {
            String line = table.get(i);
            if (line.trim().isEmpty())
                continue;

            String[] row = split(line, escape(oldDel), SplitRegex.DROPPED);
            for (int j = 0; j < row.length; j++) {
                row[j] = row[j].trim();
            }
            tab[i] = row;
        }
        return formatTable(tab, colNum, newDel, margin, true);
    }

    /**
     * Formats a 2D array into a table with fixed-width columns.
     *
     * @param table     The 2-dimensional array to be formatted
     * @param colNum    The number of columns of the target table, if null each row can have different number of columns
     * @param delimiter The delimiter used to separate columns of the new table, if null an empty string will be used
     *                  as the new delimiter
     * @param margin    The number of spaces between columns of the new table
     * @param ideo      Whether or not consider ideographic characters when calculating the width of each field.
     *                  True so that ideographic characters won't break the fix-width alignment. This does not guarantee
     *                  each field of the same column has exactly the same display width since this also depends on the
     *                  fonts in use. If a field contains ideographic characters then the width of the field is its
     *                  length plus the number of spaces produced by subtracting the count of ideographic characters
     *                  from the maximum length of the column because each ideographic character takes up (approximately)
     *                  two spaces.
     * @return The formatted table as a StringBuilder object
     */
    public static String formatTable(String[][] table, Integer colNum, String delimiter, int margin, boolean ideo) {
        List<Integer> colMaxWidths = new ArrayList<>();
        for (String[] row : table) {
            for (int i = 0; i < row.length; i++) {
                int width = row[i].length();
                if (colMaxWidths.size() < i + 1) {
                    colMaxWidths.add(width);
                } else if (colMaxWidths.get(i) < width) {
                    colMaxWidths.set(i, width);
                }
            }
        }

        StringBuilder result = new StringBuilder();
        String colDelimiter = StringUtils.isBlank(delimiter) ? "" : delimiter + " ";
        String margin1 = margin == 0 ? "" : String.format("%" + margin + "s", "");
        if (colNum == null) {
            for (String[] row : table) {
                result.append(colDelimiter);
                for (int i = 0; i < row.length - 1; i++) {
                    int width = !ideo ? colMaxWidths.get(i) : (colMaxWidths.get(i) - getIdeogramCount(row[i]));
                    result.append(String.format("%-" + (width == 0 ? 1 : width) + "s", row[i])).append(margin1).append(colDelimiter);
                }
                result.append(row[row.length - 1]).append("\n");
            }
        } else {
            for (String[] row : table) {
                result.append(colDelimiter);
                for (int i = 0; i < colNum; i++) {
                    if (i < colNum - 1) {
                        String field = i >= row.length ? "" : row[i];
                        int width = !ideo ? colMaxWidths.get(i) : (colMaxWidths.get(i) - getIdeogramCount(field));
                        result.append(String.format("%-" + (width == 0 ? 1 : width) + "s", field)).append(margin1).append(colDelimiter);
                    } else {
                        StringBuilder rest = new StringBuilder();
                        for (int j = i; j < row.length; j++) {
                            rest.append(row[j]);
                        }
                        result.append(rest).append("\n");
                    }
                }
            }
        }
        return result.toString();
    }

    public static int getIdeogramCount(String str) {
        return (int) str.codePoints().filter(Character::isIdeographic).count();
    }

    /**
     * Formats a string to a Markdown table
     *
     * @param str       the string to be formatted as an AsciiDoc table
     * @param colNum    the number of columns of the target table
     * @param delimiter the separator between the columns of the source table
     */
    public static String formatTableMarkdown(String str, int colNum, String delimiter) {
        List<String> table = new ArrayList<>(Arrays.asList(str.split("\n")));
        StringBuilder delimiters = new StringBuilder();
        for (int i = 0; i < colNum; i++) {
            delimiters.append("---").append(delimiter);
        }
        table.add(1, delimiters.toString());
        return formatTable(table, colNum, delimiter, "|", 1).toString();
    }

    /**
     * Formats a string to an AsciiDoc table
     *
     * @param str       the string to be formatted as an AsciiDoc table
     * @param colNum    the number of columns of the target table
     * @param delimiter the separator between the columns of the source table
     */
    public static String formatTableAsciiDoc(String str, int colNum, String delimiter) {
        List<String> lines = new ArrayList<>(Arrays.asList(str.split("\n")));
        if ("|===".equals(lines.get(0).trim())) {
            lines.remove(0);
        }
        if ("|===".equals(lines.get(lines.size() - 1).trim())) {
            lines.remove(lines.size() - 1);
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (lines.get(i).charAt(0) == '|') {
                lines.set(i, line.substring(1));
            }
        }
        return "|===\n" + formatTable(lines, colNum, delimiter, "|", 1) + "|===\n";
    }

    /**
     * Converts a url string to a map object
     */
    public static Map<String, Object> urlToMap(URL url) {
        Map<String, Object> map = new HashMap<>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            map.put(kv[0], kv.length > 1 ? kv[1] : "");
        }
        return map;
    }

    /**
     * Returns the index of the first non-whitespace character in a string, or -1 if all characters of the string are blank
     *
     * @param str the string to check
     */
    public static int indexOfFirstNonBlankChar(String str) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isWhitespace(chars[i])) {
                return i;
            }
        }
        return -1;
    }
}
