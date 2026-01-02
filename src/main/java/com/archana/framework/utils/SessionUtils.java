package com.archana.framework.utils;

import com.archana.framework.driver.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SessionUtils {

    // Export cookies
    public static Set<Cookie> exportCookies(WebDriver driver) {
        return driver.manage().getCookies();
    }

    // Restore cookies into driver (must navigate to the same origin first)
    public static void restoreCookies(WebDriver driver, Set<Cookie> cookies) {
        if (cookies == null) return;
        for (Cookie c : cookies) {
            try {
                driver.manage().addCookie(c);
            } catch (Exception e) {
                System.err.println("[SessionUtils] Failed to add cookie: " + c + " -> " + e.getMessage());
            }
        }
    }

    // Export localStorage as JSON string with basic redaction for sensitive keys
    public static String exportLocalStorage(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = "var ls = window.localStorage; var obj = {}; var redact=/token|auth|password|secret/i; for (var i=0;i<ls.length;i++){ var k = ls.key(i); var v = ls.getItem(k); obj[k]= redact.test(k) ? '[REDACTED]' : v;} return JSON.stringify(obj);";
        Object res = js.executeScript(script);
        return res == null ? "{}" : res.toString();
    }

    // Restore localStorage from JSON string (must be on same origin)
    public static void restoreLocalStorage(WebDriver driver, String json) {
        if (json == null || json.trim().isEmpty()) return;
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = "var obj = JSON.parse(arguments[0]); for (var k in obj){ if (obj.hasOwnProperty(k)) window.localStorage.setItem(k, obj[k]); }";
        js.executeScript(script, json);
    }

    // Export sessionStorage as JSON string with basic redaction
    public static String exportSessionStorage(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = "var ss = window.sessionStorage; var obj = {}; var redact=/token|auth|password|secret/i; for (var i=0;i<ss.length;i++){ var k = ss.key(i); var v = ss.getItem(k); obj[k]= redact.test(k) ? '[REDACTED]' : v;} return JSON.stringify(obj);";
        Object res = js.executeScript(script);
        return res == null ? "{}" : res.toString();
    }

    // Export browser console logs (best-effort; may be empty depending on driver)
    public static String exportConsoleLogs(WebDriver driver) {
        try {
            LogEntries entries = driver.manage().logs().get(LogType.BROWSER);
            StringBuilder sb = new StringBuilder();
            for (LogEntry e : entries) {
                sb.append(Instant.ofEpochMilli(e.getTimestamp()))
                  .append(" ")
                  .append(e.getLevel())
                  .append(" ")
                  .append(e.getMessage())
                  .append("\n");
            }
            return sb.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    // Export simple metadata: url and userAgent
    public static String exportMetadata(WebDriver driver) {
        Map<String, String> meta = new HashMap<>();
        try { meta.put("url", driver.getCurrentUrl()); } catch (Exception ignored) {}
        try {
            Object ua = ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;");
            meta.put("userAgent", ua == null ? "" : ua.toString());
        } catch (Exception ignored) {}
        meta.put("timestamp", Instant.now().toString());
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : meta.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"" + e.getKey() + "\":\"" + e.getValue().replace("\"","\\\"") + "\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    // Attach common failure evidence to Allure
    public static void attachFailureEvidence(String testName) {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver == null) return;

            // Page source
            try {
                String html = driver.getPageSource();
                if (html != null && !html.isEmpty()) {
                    Allure.addAttachment("Page Source - " + testName, "text/html", new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)), ".html");
                }
            } catch (Exception ignored) {}

            // Cookies
            try {
                Set<Cookie> cookies = exportCookies(driver);
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (Cookie c : cookies) {
                    if (!first) sb.append(",");
                    sb.append("{\"name\":\"" + c.getName().replace("\"","\\\"") + "\",\"value\":\"[REDACTED]\",\"domain\":\"" + (c.getDomain()==null?"":c.getDomain()) + "\",\"path\":\"" + (c.getPath()==null?"":c.getPath()) + "\",\"secure\":" + c.isSecure() + ",\"httpOnly\":" + c.isHttpOnly() + "}");
                    first = false;
                }
                sb.append("]");
                Allure.addAttachment("Cookies - " + testName, "application/json", new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)), ".json");
            } catch (Exception ignored) {}

            // Storage
            try {
                String ls = exportLocalStorage(driver);
                Allure.addAttachment("LocalStorage - " + testName, "application/json", new ByteArrayInputStream(ls.getBytes(StandardCharsets.UTF_8)), ".json");
            } catch (Exception ignored) {}
            try {
                String ss = exportSessionStorage(driver);
                Allure.addAttachment("SessionStorage - " + testName, "application/json", new ByteArrayInputStream(ss.getBytes(StandardCharsets.UTF_8)), ".json");
            } catch (Exception ignored) {}

            // Console logs
            try {
                String console = exportConsoleLogs(driver);
                if (console != null && !console.isEmpty()) {
                    Allure.addAttachment("Console Logs - " + testName, "text/plain", new ByteArrayInputStream(console.getBytes(StandardCharsets.UTF_8)), ".log");
                }
            } catch (Exception ignored) {}

            // Metadata
            try {
                String meta = exportMetadata(driver);
                Allure.addAttachment("Metadata - " + testName, "application/json", new ByteArrayInputStream(meta.getBytes(StandardCharsets.UTF_8)), ".json");
            } catch (Exception ignored) {}
        } catch (Exception ignored) {
        }
    }
}
