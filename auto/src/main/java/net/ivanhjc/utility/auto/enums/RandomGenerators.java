package net.ivanhjc.utility.auto.enums;

import com.google.common.primitives.Primitives;
import net.ivanhjc.utility.auto.RandomGenerator;
import net.ivanhjc.utility.data.SplitRegex;
import net.ivanhjc.utility.data.StringUtils;
import net.ivanhjc.utility.reflection.ReflectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.*;

public enum RandomGenerators implements RandomGenerator {
    RANDOM_STRING {
        @Override
        public String generate() {
            return RandomStringUtils.randomAlphanumeric(1, 10);
        }
    },

    RANDOM_STRING_SELECT_FROM_FILE {
        @Override
        public String generate() {
            return RANDOM_STRING_SELECT_FROM_FILE_GENERATOR.generate(1, 10);
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
        public DateTime generate() {
            return new DateTime(RandomUtils.nextLong(0, System.currentTimeMillis()));
        }
    };

    private static final Logger LOG = LogManager.getLogger();

    public final static RandomStringGenerator RANDOM_STRING_SELECT_FROM_FILE_GENERATOR =
            new RandomStringGenerator.Builder()
                    .selectFrom(RandomStringCharacterPredicates.UNICODE_PREDICATES)
                    .build();

    public final static Map<Class, RandomGenerator> TYPE_GENERATOR_MAP;

    static {
        TYPE_GENERATOR_MAP = new HashMap<>();
        RandomGenerators[] filter = new RandomGenerators[]{RANDOM_STRING_SELECT_FROM_FILE};
        for (RandomGenerators g : RandomGenerators.values()) {
            if (ArrayUtils.contains(filter, g)) {
                continue;
            }
            TYPE_GENERATOR_MAP.put(g.getClass().getDeclaredMethods()[0].getReturnType(), g);
        }
    }

    /**
     * Generate a random object of the supplied type, which may be a common Java type such as String, Integer, int,
     * array, or a POJO class. The POJO class may or may not contain getter and setter methods because it's its fields
     * that are used for assigning and retrieving values. The values for each field is random according to its type.
     * The POJO class may contain fields of the same type as the POJO class, and in such case only the first-level
     * fields will be assigned with generated objects.
     *
     * @param type the type out of which the object is created
     */
    public static <T> Object generate(Class<T> type) {
        Class typeKey = type.isPrimitive() ? Primitives.wrap(type) : type;
        RandomGenerator generator = TYPE_GENERATOR_MAP.get(typeKey);
        if (generator != null) {
            return generator.generate();
        }

        if (type.isArray()) {
            Object array = Array.newInstance(type.getComponentType(), 2);
            Array.set(array, 0, generate(type.getComponentType()));
            Array.set(array, 1, generate(type.getComponentType()));
            return array;
        }

        try {
            T obj = type.newInstance();
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value;
                switch (field.getType().getSimpleName()) {
                    case "List":
                        String componentType = StringUtils.substringBetween(field.getGenericType().getTypeName(), "<", ">");
                        Object val;
                        if (componentType != null) {
                            Class<?> componentTypeClass = type.getClassLoader().loadClass(componentType);
                            val = generate(componentTypeClass);
                        } else {
                            val = generate(String.class);
                        }
                        value = new ArrayList<>(Arrays.asList(val, val));
                        break;
                    case "Map":
                        String componentTypes = StringUtils.substringBetween(field.getGenericType().getTypeName(), "<", ">");
                        Map map = new HashMap();
                        Class<?> keyType, valueType;
                        if (componentTypes != null) {
                            String[] types = StringUtils.splitAndTrim(componentTypes, ",", SplitRegex.DROPPED);
                            keyType = type.getClassLoader().loadClass(types[0]);
                            valueType = type.getClassLoader().loadClass(types[1]);
                        } else {
                            keyType = String.class;
                            valueType = String.class;
                        }
                        map.put(generate(keyType), generate(valueType));
                        value = map;
                        break;
                    default:
                        value = isGrandchild(type, field) ? null : generate(field.getType());
                }
                field.set(obj, value);
            }
            return obj;
        } catch (Exception e) {
            LOG.error(e);
            return null;
        }
    }

    private static boolean isGrandchild(Class type, Field field) {
        if (type == field.getType()) {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            return stackTraceElements[2].getMethodName().equals(stackTraceElements[3].getMethodName())
                    && stackTraceElements[2].getLineNumber() == stackTraceElements[3].getLineNumber();
        }
        return false;
    }

    /**
     * Generate a random object out of a designated .class file. This is a convenient method for {@link #generate(Class)}
     *
     * @param classPath the directory containing the .class file, e.g. "/home/user/downloads/classes/".
     * @param className the full name of the class, e.g. "com.company.Customer", and the "Customer.class" file should be
     *                  put under "/home/user/downloads/classes/com/company/".
     * @return an object populated with random values
     */
    public static Object generate(String classPath, String className) throws MalformedURLException, ClassNotFoundException {
        return generate(ReflectionUtils.getClass(classPath, className));
    }

    /**
     * Generate a list of objects of the given POJO class type with each field assigned with a random value according to
     * its type.
     *
     * @param type the POJO class type
     * @param size number of objects to produce
     * @param <T>  the POJO class type as the type parameter
     * @return the list of objects
     */
    public static <T> List<T> generate(Class<T> type, int size) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(type.cast(generate(type)));
        }
        return list;
    }

}
