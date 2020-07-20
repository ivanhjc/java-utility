package net.ivanhjc.utility.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Properties;

/**
 * @author Ivan Huang on 2019/2/14 16:28.
 */
public class PropertyLoader {
    private static final Logger logger = LogManager.getLogger(POIUtils.class);

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
