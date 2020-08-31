package net.ivanhjc.utility.data;

import net.ivanhjc.utility.model.TokenInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Ivan Huang on 2017/6/27
 */
public class StringUtilsTest {

    private String format = "%-20s%-20s%s%n";
    private String pointer = "->";
    private String[] testStrs1 = {null, "", "w", "W", "we", "We", "Word", "word", "wOrd", "WORD", "word_family", "wordFamily", "WordFamily", "t_word_family", "1011001000", "11010"};
    private String[] testStrs2 = {null, "", "20", "20.5", "20L", "0x20", "ABC"};
    private String[] testStrs3 = {null, "", "1234", "abcd", "ABCD", "1F6AD", "FGXY", "0x12FD", "0X", "0x24F", "0X3D6F"};
    private String[] testStrs4 = {null, "", "  1,  2,  3 , 4", ",1,2,3,", "bird,cat,dog", "\\u9000\\u62BC\\u91D1", "\\u9001\\uABXY\\u91D2", "你好", "≤"};

    @Test
    public void test() {
        Base obj = new A();
        obj.a();
        obj.b();
        obj.a();
    }

    interface Base {
        default void a() {
            System.out.println("a");
        }

        void b();

        void c(Base list);
    }

    class A implements Base {

        /*@Override
        public void a() {
            System.out.println("a1");
        }*/

        @Override
        public void b() {
            System.out.println("b1");
        }

        @Override
        public void c(Base list) {

        }
    }

    @Test
    public void capitalize() {
        for (String str : testStrs1)
            System.out.printf(format, str, pointer, StringUtils.capitalize(str));
    }

    @Test
    public void uncapitalize() {
        for (String str : testStrs1)
            System.out.printf(format, str, pointer, StringUtils.uncapitalize(str));
    }

    @Test
    public void toTitleCase() {
        for (String str : testStrs1)
            System.out.printf(format, str, pointer, StringUtils.toTitleCase(str));
    }

    @Test
    public void camelToSnake() {
        for (String str : testStrs1)
            System.out.printf(format, str, pointer, StringUtils.camelToSnake(str));
    }

    @Test
    public void snakeToCamel() {
        for (String str : testStrs1)
            System.out.printf(format, str, pointer, StringUtils.snakeToCamel(str));
    }

    @Test
    public void camelToSnakeArray() {
        ListUtils.println(StringUtils.camelToSnake(new String[]{"companyName", "cardNo"}));
    }

    @Test
    public void isLettersAndDigits() {
        for (String str : testStrs1)
            System.out.printf(format, str, pointer, StringUtils.isLettersAndDigits(str));
    }

    @Test
    public void isInteger() {
        for (String str : testStrs2) {
            System.out.printf(format, str, pointer, StringUtils.isInteger(str));
        }
    }

    @Test
    public void isHex() {
        for (String str : testStrs3) {
            System.out.printf(format, str, pointer, StringUtils.isHex(str));
        }
    }

    @Test
    public void inArray() {
        String array = "a,b,c,d,15,null";
        System.out.println(ListUtils.contains(array, 15L));
        System.out.println(ListUtils.contains(array, "b"));
        System.out.println(ListUtils.contains(array, 15));
        System.out.println(ListUtils.contains(array, "t"));
        System.out.println(ListUtils.contains(array, null));
    }

    @Test
    public void splitToEqualLengthStrings() {
        System.out.println("Backwards:");
        for (String str : testStrs1) {
            System.out.printf(format, str, pointer, Arrays.toString(StringUtils.splitEqually(str, 4, 0)));
        }
        System.out.println();
        System.out.println("Forwards:");
        for (String s : testStrs1) {
            System.out.printf(format, s, pointer, Arrays.toString(StringUtils.splitEqually(s, 4, 1)));
        }
    }

    @Test
    public void replace() {
        for (String str : testStrs1) {
            if (str == null)
                continue;
            System.out.printf(format, str, pointer, StringUtils.replace(new StringBuilder(str), "w", "ABC"));
        }
    }

    @Test
    public void split() {
        String delimiter = "\\\\u";
        String format = "%1$-30s %2$-10s %3$-30s %4$-30s %5$-30s %6$-30s%n";
        System.out.printf(format, "original", "", "dropped", "ahead", "behind", "independent");
        for (String str : testStrs4) {
            if (str == null)
                continue;
            System.out.printf(format, str, pointer,
                    Arrays.toString(StringUtils.split(str, delimiter, SplitRegex.DROPPED)),
                    Arrays.toString(StringUtils.split(str, delimiter, SplitRegex.AHEAD)),
                    Arrays.toString(StringUtils.split(str, delimiter, SplitRegex.BEHIND)),
                    Arrays.toString(StringUtils.split(str, delimiter, SplitRegex.INDEPENDENT)));
        }
    }

    @Test
    public void splitAndTrim() {
        String[] tests = {"  a, b   , c ,d ", " a \t \t b c  \t d", "参数名称 | 是否必填 | 参数类型         | 备注"};
        String format = "%1$-30s %2$-10s %3$-30s %n";
        System.out.printf(format, "original", "", "splitAndTrim");
        for (String str : tests) {
            System.out.printf(format, str, pointer, Arrays.toString(StringUtils.splitAndTrim(str, "\\|", SplitRegex.DROPPED)));
        }
    }

    @Test
    public void encloseSubstrings() {
        String delimiter = "\\\\";
        String encStr = "\"";
        String newDel = ", ";
        String format = "%1$-30s %2$-10s %3$-30s %4$-30s %5$-30s %6$-30s%n";
        System.out.printf(format, "original", "", "dropped", "ahead", "behind", "independent");
        for (String str : testStrs4) {
            if (str == null)
                continue;
            System.out.printf(format, str, pointer,
                    StringUtils.encloseSubstrings(str, delimiter, SplitRegex.DROPPED, encStr, newDel),
                    StringUtils.encloseSubstrings(str, delimiter, SplitRegex.AHEAD, encStr, newDel),
                    StringUtils.encloseSubstrings(str, delimiter, SplitRegex.BEHIND, encStr, newDel),
                    StringUtils.encloseSubstrings(str, delimiter, SplitRegex.INDEPENDENT, encStr, newDel));
        }
    }


    @Test
    public void translateCodePoints() {
        /*for (String str : testStrs4) {
            if (str == null || str.trim().isEmpty())
                continue;
            System.out.printf(format, str, pointer, StringUtils.translateCodePoints(str));
        }*/
        System.out.println(StringUtils.translateCodePoints(""));

    }

    @Test
    public void toCodePoints() {
        /*for (String str : testStrs4) {
            if (str == null || str.trim().isEmpty())
                continue;
            System.out.printf(format, str, pointer, StringUtils.toCodePoints(str));
        }*/
        System.out.println(StringUtils.toCodePoints("𐍃𐍉𐍆𐌹𐌰"));
    }

    @Test
    public void jsonSnakeToJavaBean() {
        String token = "{\n" +
                "  \"refresh_token\": \"25.b55fe1d287227ca97aab219bb249b8ab.315360000.1798284651.282335-8574074\",\n" +
                "  \"expires_in\": 2592000,\n" +
                "  \"scope\": \"public wise_adapt\",\n" +
                "  \"session_key\": \"9mzdDZXu3dENdFZQurfg0Vz8slgSgvvOAUebNFzyzcpQ5EnbxbF+hfG9DQkpUVQdh4p6HbQcAiz5RmuBAja1JJGgIdJI\",\n" +
                "  \"access_token\": \"24.6c5e1ff107f0e8bcef8c46d3424a0e78.2592000.1485516651.282335-8574074\",\n" +
                "  \"session_secret\": \"dfac94a3489fe9fca7c3221cbf7525ff\"\n" +
                "}";
        String token2 = "{\"refreshToken\":\"25.b55fe1d287227ca97aab219bb249b8ab.315360000.1798284651.282335-8574074\",\"expiresIn\":2592000,\"scope\":\"public wise_adapt\",\"sessionKey\":\"9mzdDZXu3dENdFZQurfg0Vz8slgSgvvOAUebNFzyzcpQ5EnbxbF+hfG9DQkpUVQdh4p6HbQcAiz5RmuBAja1JJGgIdJI\",\"accessToken\":\"24.6c5e1ff107f0e8bcef8c46d3424a0e78.2592000.1485516651.282335-8574074\",\"sessionSecret\":\"dfac94a3489fe9fca7c3221cbf7525ff\",\"expireDate\":\"Jul 1, 2018 2:01:25 PM\"}";
        TokenInfo info = StringUtils.GSON_SNAKE.fromJson(token2, TokenInfo.class);
        info.setExpireDate(new Date(System.currentTimeMillis() + info.getExpiresIn() * 1000));
        System.out.println(StringUtils.GSON_NULL.toJson(info));
    }

    @Test
    public void substringBetween() {
        String line = "2018-04-01 00:00:06,263 ERROR com.winsky.lease.order.impl.CarOrderServiceImpl.endOrder(CarOrderServiceImpl.java:720) - userID:48508,orderId:292446,returnChanncel:0 endOrder error!";
        String orderIDValue = org.apache.commons.lang3.StringUtils.substringBetween(line, "orderId:", ",");
        System.out.println(orderIDValue);
        String line2 = "292545              246";
        System.out.println(org.apache.commons.lang3.StringUtils.substringBefore(line2, " "));
    }

    @Test
    public void formatTable() {
        String[][] table = new String[10][];
        for (int i = 0; i < table.length; i++) {
            String[] row = new String[RandomUtils.nextInt(1, 10)];
            for (int j = 0; j < row.length; j++) {
                row[j] = RandomStringUtils.random(RandomUtils.nextInt(1, 10), true, true);
            }
            table[i] = row;
        }
        System.out.println(StringUtils.formatTable(table, null, null, 5));
    }

    @Test
    public void formatMarkdownTable() {
        System.out.println(StringUtils.formatTableMarkdown("id,name,age,sex\n1,Bob,12,M\n2,Alice,8,F\n", 4, ","));
    }

    @Test
    public void formatAsciiDocTable() {
        System.out.println(StringUtils.formatTableAsciiDoc("" +
                "|===\n" +
                        "| Key strokes                  | Meaning\n" +
                        "| TAB                     | Expand/Collapse entries\n" +
                        "| S-TAB                   | Cyclfe visibility through all top-level headings, all headings, and everything\n" +
                        "| C-c c-n | Go to next heading\n" +
                        "| C-c c-p | Go to previous heading\n" +
                        "| C-c c-f | Go to next heading of same level\n" +
                        "| C-c c-b | Go to previous heading of same level\n" +
                        "| C-c c-u | Go to parent heading\n" +
                        "| C-c c-p | Go to previous heading\n" +
                        "| M-RET | Insert a heading, item or row on next line\n" +
                        "| {C-u M-RET|C-RET} | Insert a heading at the end of current heading's subtree\n" +
                        "| C-u C-u M-RET | Insert a heading at the end of parent heading's subtree\n" +
                        "| C-c * | Toggle headings for current line or active region\n" +
                        "| M-UP | Move entry up to the same level\n" +
                        "| M-DOWN | Move entry down to the same level\n" +
                        "| M-LEFT | Move entry up a level\n" +
                        "| M-RIGHT | Move entry down a level\n" +
                        "| M-h | Expand selection\n" +
                        "| C-c C-t | Toggle todo\n" +
                        "| M-S-RET | Insert a TODO entry on next line\n" +
                        "| C-S-RET | Insert a TODO entry at the end of current heading's subtree\n" +
                        "| C-c C-s | Add scheduled date\n" +
                        "| C-c C-d | Add deadline\n" +
                        "| C-c ^ | Sort entries\n" +
                        "|===\n",
                2, "|"));
    }

    @Test
    public void urlToMap() throws MalformedURLException {
        System.out.println(StringUtils.GSON_NULL.toJson(StringUtils.urlToMap(new URL("http://test.com/find?id=1&name=ivan"))));
    }

    @Test
    public void indexOfFirstNonBlankChar() {
        System.out.println(StringUtils.indexOfFirstNonBlankChar(""));
        System.out.println(StringUtils.indexOfFirstNonBlankChar("   - hello"));
    }
}
