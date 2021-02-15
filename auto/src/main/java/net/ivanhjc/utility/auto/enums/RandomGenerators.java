package net.ivanhjc.utility.auto.enums;

import net.ivanhjc.utility.auto.RandomGenerator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public enum RandomGenerators implements RandomGenerator {
    RANDOM_STRING {
        @Override
        public String generate() {
            return RandomStringUtils.randomAlphanumeric(1, 10);
        }
    },

    RANDOM_STRING_LIMIT_CHARS {
        @Override
        public String generate() {
            return RANDOM_STRING_ALPHA_NUMERAL_AND_OTHER_SPECIFIED_IN_FILE_GENERATOR.generate(1, 10);
        }
    },

    RANDOM_INTEGER {
        @Override
        public Integer generate() {
            return RandomUtils.nextInt();
        }
    },

    RANDOM_LONG {
        @Override
        public Long generate() {
            return RandomUtils.nextLong();
        }
    },

    RANDOM_FLOAT {
        @Override
        public Object generate() {
            return RandomUtils.nextFloat();
        }
    },

    RANDOM_DOUBLE {
        @Override
        public Double generate() {
            return RandomUtils.nextDouble();
        }
    },

    RANDOM_BOOLEAN {
        @Override
        public Boolean generate() {
            return RandomUtils.nextBoolean();
        }
    },

    RANDOM_BIG_DECIMAL {
        @Override
        public BigDecimal generate() {
            return BigDecimal.valueOf(RandomUtils.nextDouble());
        }
    },

    RANDOM_DATE {
        @Override
        public Date generate() {
            return new Date(RandomUtils.nextLong(0, new Date().getTime()));
        }
    };

    public final static RandomStringGenerator RANDOM_STRING_ALPHA_NUMERAL_AND_OTHER_SPECIFIED_IN_FILE_GENERATOR =
            new RandomStringGenerator.Builder()
                    .filteredBy(RandomStringCharacterPredicates.ALPHA_NUMERAL_AND_OTHER_SPECIFIED_IN_FILE)
                    .build();

    public final static Map<Class, RandomGenerator> TYPE_GENERATOR_MAP;

    static {
        TYPE_GENERATOR_MAP = new HashMap<>();
        RandomGenerators[] filter = new RandomGenerators[]{RANDOM_STRING_LIMIT_CHARS};
        for (RandomGenerators g : RandomGenerators.values()) {
            if (ArrayUtils.contains(filter, g)) {
                continue;
            }
            TYPE_GENERATOR_MAP.put(g.getClass().getDeclaredMethods()[0].getReturnType(), g);
        }
    }
}
