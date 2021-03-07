package utility;

import java.io.InputStream;
import java.util.Properties;

public class ConfigUtils {

    private static Properties getProperties() throws Exception {

        Properties prop = new Properties();
        String propFileName = "config.properties";

        try (InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(propFileName)) {
            prop.load(inputStream);
            return prop;
        }
    }

    public static String getConfigValue(String key) throws Exception{
        return getProperties().getProperty(key);
    }
}
