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
        return System.getProperty(key, props.getProperty(key, defaultVal));
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
            throw new RuntimeException("Required config key '" + key + "' is missing in '" + env + ".properties' or -D" + key + "");
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
}
