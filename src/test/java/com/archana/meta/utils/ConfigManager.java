package com.archana.meta.utils;

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

    private ConfigManager(){}
}