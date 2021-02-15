package net.ivanhjc.utility.data;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum EscapeTypes implements EscapeType {
    SQL{
        @Override
        public String escape(String str) {
            return CharBuffer.wrap(str.toCharArray()).chars().mapToObj(c -> {
                if (Arrays.binarySearch(ESCAPE_CHARS_SQL, c) >= 0) {
                    return "\\" + (char) c;
                }
                return String.valueOf((char) c);
            }).collect(Collectors.joining());
        }
    },

    REGEX{
        @Override
        public String escape(String str) {
            return null;
        }
    };

    private final static int[] ESCAPE_CHARS_SQL = {'\'', '\\'};

    static {
        Arrays.sort(ESCAPE_CHARS_SQL);
    }
}
