package com.automation.ecommerce.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads config/<env>.properties (env = qa by default, switch with -Denv=staging).
 * Any key can be overridden from the command line, e.g. -Dbrowser=edge.
 */
public final class ConfigReader {

    private static final String ENV = readEnv();
    private static final Properties PROPERTIES = load();

    private ConfigReader() {
    }

    private static String readEnv() {
        String env = System.getProperty("env");
        return (env == null || env.isBlank()) ? "qa" : env.trim().toLowerCase();
    }

    private static Properties load() {
        String path = "config/" + ENV + ".properties";
        Properties properties = new Properties();
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Config file not found: " + path);
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read config file: " + path, e);
        }
        return properties;
    }

    public static String get(String key) {
        String override = System.getProperty(key);
        if (override != null && !override.isBlank()) {
            return override.trim();
        }
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing config key '" + key + "' for env '" + ENV + "'");
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static String getEnv() {
        return ENV;
    }
}
