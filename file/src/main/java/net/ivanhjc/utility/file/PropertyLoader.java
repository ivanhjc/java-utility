package net.ivanhjc.utility.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author Ivan Huang on 2019/2/14 16:28.
 */
public class PropertyLoader {

    private final static Logger logger = LoggerFactory.getLogger(PropertyLoader.class);

    /**
     * Loads properties from a file
     *
     * @param file located under classpath
     */
    public static Properties loadProperties(String file) {
        Properties properties = new Properties();
        try {
            properties.load(PropertyLoader.class.getResourceAsStream("/" + file));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return properties;
    }
}
