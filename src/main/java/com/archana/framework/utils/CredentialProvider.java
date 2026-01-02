package com.archana.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CredentialProvider {

    private static final Logger log = LogManager.getLogger(CredentialProvider.class);

    private CredentialProvider() {}

    public static String getLoginEmail() {
        // Diagnostic: indicate source without exposing unrelated secrets
        if (System.getProperty("login.email") != null) {
            log.info("Credential source: login.email from JVM system property (-Dlogin.email)");
        } else if (System.getenv("login.email") != null || System.getenv("LOGIN_EMAIL") != null) {
            log.info("Credential source: login.email from environment variable (LOGIN_EMAIL)");
        } else {
            log.info("Credential source: login.email from properties file (config/{env}.properties)");
        }
        // Fail-fast: require login.email to be provided via dev.properties or override
        return ConfigManager.getRequired("login.email");
    }

    public static String getLoginPassword() {
        // Diagnostic: indicate source, mask value
        if (System.getProperty("login.password") != null) {
            log.info("Credential source: login.password from JVM system property (-Dlogin.password)");
        } else if (System.getenv("login.password") != null || System.getenv("LOGIN_PASSWORD") != null) {
            log.info("Credential source: login.password from environment variable (LOGIN_PASSWORD)");
        } else {
            log.info("Credential source: login.password from properties file (config/{env}.properties)");
        }
        // Fail-fast: require login.password to be provided via dev.properties or override
        return ConfigManager.getRequired("login.password");
    }
}
