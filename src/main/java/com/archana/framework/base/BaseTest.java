package com.archana.framework;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

import org.slf4j.MDC;

public abstract class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void setUp(java.lang.reflect.Method method){
        // Set a short per-test id before any setup/logging so sifting appender has the testId
        String uid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String testId = method.getDeclaringClass().getSimpleName() + "-" + method.getName() + "-" + uid;
        MDC.put("testId", testId);

        // initialize driver after MDC is set so driver-related logs are properly routed
        WebDriver driver = DriverFactory.getDriver();

    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result){
        if (!result.isSuccess()){
            try{
                WebDriver driver = DriverFactory.getDriver();

                // Attach screenshot to Allure (bytes) and also save a file copy under target/screenshots
                try {
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    Allure.addAttachment("Screenshot on Failure: " + result.getName(), "image/png",
                            new ByteArrayInputStream(screenshot), "png");
                    // also save a disk copy for legacy use
                    File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                    String target = Paths.get("target", "screenshots", result.getName() + ".png").toString();
                    FileUtils.copyFile(src, new File(target));
                } catch (Exception attachEx) {
                    System.err.println("Failed to attach or save screenshot: " + attachEx.getMessage());
                }

                // Attach page source to Allure
                try {
                    String pageSource = driver.getPageSource();
                    byte[] pageBytes = pageSource == null ? new byte[0] : pageSource.getBytes();
                    Allure.addAttachment("Page Source on Failure: " + result.getName(), "text/html",
                            new ByteArrayInputStream(pageBytes), "html");
                } catch (Exception psEx) {
                    System.err.println("Failed to attach page source: " + psEx.getMessage());
                }

                // Attach browser console logs (best-effort)
                try {
                    StringBuilder logsBuilder = new StringBuilder();
                    LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
                    for (LogEntry entry : logs) {
                        logsBuilder.append(new java.util.Date(entry.getTimestamp())).append(" ")
                                .append(entry.getLevel()).append(" ")
                                .append(entry.getMessage()).append("\n");
                    }
                    byte[] logBytes = logsBuilder.toString().getBytes();
                    Allure.addAttachment("Browser Console Logs: " + result.getName(), "text/plain",
                            new ByteArrayInputStream(logBytes), "txt");
                } catch (Exception logEx) {
                    System.err.println("Failed to capture browser logs: " + logEx.getMessage());
                }
            } catch (Exception e) {
                // prefer a logging framework (SLF4J / Logback) in real projects, for example:
                // private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BaseTest.class);
                // logger.error("Failed to take screenshot", e);
                System.err.println("Failed to capture screenshot: " + e.getMessage());
            }
        }

        // Small, configurable delay before quitting the browser. Useful to allow
        // transient browser UI (like password-save prompts) to settle and not
        // interfere with test assertions right at the end.
        int delayMs = 0;
        try {
            delayMs = ConfigManager.getInt("teardown.delay.ms", 0);
        } catch (Exception ignored) { }
        if (delayMs > 0){
            try { Thread.sleep(delayMs); } catch (InterruptedException ignored) { }
        }
        DriverFactory.quitDriver();

    }


    // Note: MDC is initialized in setUp(Method) to ensure it's present before any logs occur.
    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        // attach per-test log file to Allure if available
        String testId = MDC.get("testId");
        if (testId != null) {
            Path log = Paths.get("target/logs/test-" + testId + ".log");
            try {
                // wait briefly for the async sifting appender to create/flush the file
                int attempts = 0;
                while (!Files.exists(log) && attempts < 12) { // ~3 seconds max
                    try { Thread.sleep(250); } catch (InterruptedException ignored) {}
                    attempts++;
                }

                if (Files.exists(log)) {
                    // copy into class-specific subfolder for easier browsing (best-effort)
                    try {
                        String className = result.getMethod().getTestClass().getRealClass().getSimpleName();
                        Path classDir = Paths.get("target", "logs", className);
                        if (!Files.exists(classDir)) {
                            Files.createDirectories(classDir);
                        }
                        Path copyTo = classDir.resolve(log.getFileName());
                        Files.copy(log, copyTo, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception copyEx) {
                        System.err.println("Failed to copy per-test log to class folder: " + copyEx.getMessage());
                    }

                    byte[] bytes = Files.readAllBytes(log);
                    io.qameta.allure.Allure.addAttachment("test-log", new ByteArrayInputStream(bytes));
                } else {
                    System.err.println("Per-test log not found for " + testId + " after waiting; skipping attach.");
                }
            } catch (Exception e) {
                // best-effort: don't fail the test due to log attachment problems
                System.err.println("Failed to read/attach per-test log: " + e.getMessage());
            }
        }
        MDC.remove("testId");
    }

}

