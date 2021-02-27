package net.ivanhjc.utility.auto.enums;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.CharacterPredicate;
import org.apache.commons.text.CharacterPredicates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

public enum RandomStringCharacterPredicates implements CharacterPredicate {
    ONLY_PUNCTUATIONS {
        @Override
        public boolean test(int codePoint) {
            return codePoint >= 0x2000 && codePoint <= 0x206F;
        }
    },

    ONLY_CHINESE {
        @Override
        public boolean test(int codePoint) {
            return codePoint >= 0x4e00 && codePoint <= 0x56fc;
        }
    },

    /**
     * Only use characters specified in file unicode_predicates.
     */
    ONLY_SPECIFIED_IN_FILE {
        @Override
        public boolean test(int codePoint) {
            return Arrays.binarySearch(UNICODE_PREDICATES, (char) codePoint) >= 0;
        }
    },

    ALPHA_NUMERAL_AND_OTHER_SPECIFIED_IN_FILE {
        @Override
        public boolean test(int codePoint) {
            return CharacterPredicates.ASCII_ALPHA_NUMERALS.test(codePoint) || ONLY_SPECIFIED_IN_FILE.test(codePoint);
        }
    };

    private static final Logger LOG = LogManager.getLogger();
    public static final char[] UNICODE_PREDICATES;

    static {
        char[] temp = new char[0];
        try {
            temp = IOUtils.toCharArray(RandomStringCharacterPredicates.class.getResourceAsStream("/unicode_predicates"));
            Arrays.sort(temp);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        UNICODE_PREDICATES = temp;
    }
}
