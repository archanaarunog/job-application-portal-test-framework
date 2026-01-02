package com.archana.framework.utils;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigManager{
    private static final Properties props = new Properties();

    static {
        String env = System.getProperty("env", "dev");
        String resource = "config/" + env + ".properties";
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in != null) {
                props.load(in);
            } else {
                throw new RuntimeException("Config resource not found: " + resource);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + resource, e);
        }
    }

    public static String get(String key, String defaultVal) {
        // Priority: JVM system property -> environment variable (exact or ENV_STYLE) -> properties file -> default
        String sysVal = System.getProperty(key);
        if (sysVal != null) {
            return sysVal;
        }

        String envVal = getFromEnv(key);
        if (envVal != null) {
            return envVal;
        }

        return props.getProperty(key, defaultVal);
    }

    public static String get(String key){
        return get(key, null);
    }

    public static int getInt(String key, int defaultVal){
        return Integer.parseInt(get(key, Integer.toString(defaultVal)));
    }

    // Returns value or throws a clear error if missing
    public static String getRequired(String key){
        String val = get(key, null);
        if (val == null || val.trim().isEmpty()){
            String env = System.getProperty("env", "dev");
            String envKey = toEnvKey(key);
            throw new RuntimeException("Required config key '" + key + "' is missing. Provide via -D" + key + ", environment variable '" + key + "' or '" + envKey + "', or in '" + env + ".properties'.");
        }
        return val;
    }

    // Returns true if key is present and non-empty (system property overrides included)
    public static boolean exists(String key){
        String val = get(key, null);
        return val != null && !val.trim().isEmpty();
    }

    public static boolean getBoolean(String key, boolean defaultVal){
        String v = get(key, String.valueOf(defaultVal));
        return Boolean.parseBoolean(v);
    }

    private ConfigManager(){}

    private static String getFromEnv(String key){
        String v = System.getenv(key);
        if (v != null && !v.trim().isEmpty()){
            return v;
        }
        String alt = System.getenv(toEnvKey(key));
        return (alt != null && !alt.trim().isEmpty()) ? alt : null;
    }

    private static String toEnvKey(String key){
        // Convert dots/dashes and non-alphanumerics to underscores, uppercase
        return key.replaceAll("[^A-Za-z0-9]", "_").toUpperCase();
    }
}
