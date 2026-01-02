package com.archana.framework.listeners;

import com.archana.framework.driver.DriverManager;
import io.qameta.allure.Allure;
import com.archana.framework.utils.SessionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

/**
 * TestLoggingListener
 * - ensures logs directory exists at suite start
 * - attaches screenshots on failure and success
 * - attaches a snapshot of the main log file on failure and at suite finish
 * - uses DriverManager.getDriver() (exact accessor from your project)
 */
public class TestLoggingListener implements ITestListener, ISuiteListener {
    private static final Logger log = LogManager.getLogger(TestLoggingListener.class);
    private static final String LOG_DIR = "logs";

    // Ensure logs directory exists
    private void ensureLogDirectory() {
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) {
                    log.info("Created logs directory: {}", dir.getAbsolutePath());
                } else {
                    log.warn("Could not create logs directory (it may already exist): {}", dir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            // If log4j not configured yet, print stack to stdout as fallback
            System.out.println("Unable to create logs directory: " + e.getMessage());
        }
    }

    private void attachScreenshot(String name) {
        try {
            if (DriverManager.getDriver() instanceof TakesScreenshot) {
                TakesScreenshot ts = (TakesScreenshot) DriverManager.getDriver();
                byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
                log.info("Attached screenshot to Allure: {}", name);
            } else {
                log.warn("Driver is not TakesScreenshot-capable");
            }
        } catch (Exception e) {
            log.error("Failed to capture or attach screenshot: ", e);
        }
    }

    private void attachLogFileSnapshot(String attachmentName) {
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                log.warn("Log directory not found: {}", dir.getAbsolutePath());
                return;
            }

            File latest = null;
            File[] files = dir.listFiles((d, name) -> name.startsWith("test-run-") && name.endsWith(".log"));
            if (files != null) {
                for (File f : files) {
                    if (latest == null || f.lastModified() > latest.lastModified()) {
                        latest = f;
                    }
                }
            }

            if (latest != null && latest.exists()) {
                byte[] bytes = Files.readAllBytes(latest.toPath());
                Allure.addAttachment(attachmentName + " (" + latest.getName() + ")", "text/plain", new ByteArrayInputStream(bytes), ".log");
                log.info("Attached log file snapshot to Allure: {}", latest.getAbsolutePath());
            } else {
                log.warn("No log file found to attach in {}", dir.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to attach log file to Allure: ", e);
        }
    }

    // Attach only the tail (last N lines) of the latest log file to keep report lean
    private void attachLogTailSnapshot(String attachmentName, int maxLines) {
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                log.warn("Log directory not found: {}", dir.getAbsolutePath());
                return;
            }

            File latest = null;
            File[] files = dir.listFiles((d, name) -> name.startsWith("test-run-") && name.endsWith(".log"));
            if (files != null) {
                for (File f : files) {
                    if (latest == null || f.lastModified() > latest.lastModified()) {
                        latest = f;
                    }
                }
            }

            if (latest != null && latest.exists()) {
                java.util.List<String> allLines = Files.readAllLines(latest.toPath());
                int start = Math.max(0, allLines.size() - Math.max(1, maxLines));
                String tail = String.join(System.lineSeparator(), allLines.subList(start, allLines.size()));
                Allure.addAttachment(attachmentName + " (tail " + maxLines + " lines from " + latest.getName() + ")", "text/plain", new ByteArrayInputStream(tail.getBytes()), ".log");
                log.info("Attached log tail ({} lines) to Allure: {}", maxLines, latest.getAbsolutePath());
            } else {
                log.warn("No log file found to attach in {}", dir.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to attach log tail to Allure: ", e);
        }
    }

    /* -------------------- ITestListener methods -------------------- */

    @Override
    public void onTestStart(ITestResult result) {
        ThreadContext.put("class", result.getTestClass().getName());
        ThreadContext.put("test", result.getMethod().getMethodName());
        log.info("=== START TEST: {}#{} ===",
                result.getTestClass().getName(), result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("=== TEST PASSED: {}#{} ===",
                result.getTestClass().getName(), result.getMethod().getMethodName());
        // Attach a success screenshot (positive-case evidence)
        attachScreenshot("Success - " + result.getMethod().getMethodName() + " - " + new Date());
        // Attach a concise log tail for successful tests
        attachLogTailSnapshot("Success test logs - " + result.getMethod().getMethodName(), 200);
        ThreadContext.remove("test");
        ThreadContext.remove("class");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("=== TEST FAILED: {}#{} ===\nThrowable: {}",
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                result.getThrowable() == null ? "none" : result.getThrowable().toString());

        // Attach failure screenshot
        attachScreenshot("Failure - " + result.getMethod().getMethodName() + " - " + new Date());

        // Attach rich failure evidence
        try {
            SessionUtils.attachFailureEvidence(result.getMethod().getMethodName());
        } catch (Exception e) {
            log.warn("Failed to attach session evidence: {}", e.getMessage());
        }

        // Attach current log snapshot
        attachLogFileSnapshot("Test run logs - " + result.getMethod().getMethodName());
        ThreadContext.remove("test");
        ThreadContext.remove("class");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("=== TEST SKIPPED: {}#{} ===",
                result.getTestClass().getName(), result.getMethod().getMethodName());
        attachLogFileSnapshot("Skipped test logs - " + result.getMethod().getMethodName());
        ThreadContext.remove("test");
        ThreadContext.remove("class");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.warn("=== TEST FAILED WITHIN SUCCESS PERCENTAGE: {}#{} ===",
                result.getTestClass().getName(), result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("Test context starting: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("Test context finished: {}", context.getName());
        attachLogFileSnapshot("Final test-run logs for context: " + context.getName());
    }

    /* -------------------- ISuiteListener methods -------------------- */

    @Override
    public void onStart(ISuite suite) {
        // Ensure log directory is present before tests begin
        ensureLogDirectory();
        ThreadContext.put("suite", suite.getName());
        log.info("Suite started: {}", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("Suite finished: {}", suite.getName());
        attachLogFileSnapshot("Suite final logs: " + suite.getName());
        ThreadContext.remove("suite");
    }
}
