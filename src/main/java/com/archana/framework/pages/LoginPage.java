package com.archana.framework.pages;

import com.archana.framework.driver.DriverManager;
import io.qameta.allure.Step;
import com.archana.framework.utils.WaitUtils;
import com.archana.framework.utils.ElementUtils;
import com.archana.framework.utils.ScreenshotUtils;
import com.archana.framework.utils.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

public class LoginPage {

    private final WebDriver driver;
    private static final Logger log = LogManager.getLogger(LoginPage.class);

    // original locators (kept as you provided)
    private final By email  = By.id("email");
    private final By password = By.id("password");
    private final By loginBtn = By.id("loginBtnText");
    private final By errorMsg = By.cssSelector(".error-message");
    private final By alertMessage = By.id("alertMessage");
    private final By alertPanelByClass = By.cssSelector(".alert.alert-custom.alert-danger");
    private final By successMsg = By.xpath("//*[contains(normalize-space(.), 'Login successful! Redirecting')]");
    // Dashboard marker: robust class-based selector (green tick/grid icon)
    private final By dashboardSelector = By.cssSelector(".bi.bi-grid-fill.me-2");
    // Alternative markers (fallbacks) if class ordering differs
    private final By dashboardIconAlt = By.xpath("//*[contains(@class,'bi') and contains(@class,'bi-grid-fill')]");
    private final By userAvatar = By.cssSelector(".user-avatar, .avatar, img[alt*='profile']");
    private final By forgotPasswordLink = By.id("forgotPasswordLink");
    private final By signUpLink = By.cssSelector("a[href=\"register.html\"]");
    private final By backToHome = By.cssSelector("div.back-home a");
    private final By rememberMeCheckbox = By.id("rememberMe");
    private final By resetEmailInput = By.id("resetEmail");
    private final By forgotPasswordModal = By.id("forgotPasswordModal");
    private final By forgotPasswordSubmit = By.xpath("//form[@id=\"forgotPasswordForm\"]//button[@class=\"btn btn-login\"]");

    public LoginPage(){
        this.driver = DriverManager.getDriver();
        if (this.driver == null) {
            throw new IllegalStateException("WebDriver not initialized - call DriverFactory.createDriver() before using pages.");
        }
    }

    @Step("Open login page")
    public LoginPage open() {
        String base = ConfigManager.getRequired("base.url");
        // ensure trailing slash behavior consistent
        String url = base.endsWith("/") ? base + "login.html" : base + "/login.html";
        log.info("Opening login page: {}", url);
        driver.get(url);
        log.info("Login page opened");
        return this;
    }

    @Step("Enter email: {e}")
    public LoginPage enterEmail(String e){
        log.info("Entering email: {}", e);
        ElementUtils.type(email, e);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String p){
        String masked = (p == null) ? "" : "*".repeat(Math.max(0, Math.min(p.length(), 8)));
        log.info("Entering password (masked): {}", masked);
        ElementUtils.type(password, p);
        return this;
    }

    @Step("Click Login")
    public LoginPage clickLogin(){
        log.info("Clicking Login button");
        ElementUtils.click(loginBtn);
        log.info("Login click submitted");
        return this;
    }

    @Step("Wait for transient success message")
    public boolean waitForTransientSuccessMessage(){
        try{
            WaitUtils.waitForVisible(successMsg);
            // wait until invisible (short)
            WaitUtils.waitForInvisibility(successMsg);
            log.info("Transient success message appeared and disappeared");
            return true;
        } catch (Exception ex){
            log.info("Transient success message not observed");
            return false;
        }
    }

    @Step("Wait for any error visible within {seconds}s")
    public boolean waitForAnyErrorVisible(int seconds) {
        try {
            log.info("Waiting up to {}s for any error message", seconds);
            // simple loop with small poll, to match previous logic
            long end = System.currentTimeMillis() + seconds * 1000L;
            while (System.currentTimeMillis() < end) {
                List<WebElement> errs = driver.findElements(errorMsg);
                for (WebElement e : errs) {
                    if (e.isDisplayed() && e.getText() != null && !e.getText().trim().isEmpty()) return true;
                }
                if (!driver.findElements(alertMessage).isEmpty()) {
                    WebElement a = driver.findElement(alertMessage);
                    if (a.isDisplayed() && ((a.getText() != null && !a.getText().trim().isEmpty()) ||
                            (a.getAttribute("innerText") != null && !a.getAttribute("innerText").trim().isEmpty()))) return true;
                }
                if (!driver.findElements(alertPanelByClass).isEmpty()) {
                    WebElement p = driver.findElement(alertPanelByClass);
                    if (p.isDisplayed() && ((p.getText() != null && !p.getText().trim().isEmpty()) ||
                            (p.getAttribute("innerText") != null && !p.getAttribute("innerText").trim().isEmpty()))) return true;
                }
                Thread.sleep(250);
            }
            log.info("No error message found within {}s", seconds);
            return false;
        } catch (Exception e) {
            log.warn("Error while waiting for error messages: {}", e.getMessage());
            return false;
        }
    }

    @Step("Get error text")
    public String getErrorText(){
        List<WebElement> errorElements = driver.findElements(errorMsg);
        for (WebElement error : errorElements) {
            try {
                if (error.isDisplayed() && error.getText() != null && !error.getText().trim().isEmpty()) {
                    String t = error.getText().trim();
                    log.info("Primary error text: '{}'", t);
                    return t;
                }
            } catch (Exception ignored) {}
        }
        if (!driver.findElements(alertMessage).isEmpty()) {
            WebElement alert = driver.findElement(alertMessage);
            if (alert.isDisplayed()) {
                String text = alert.getText();
                if (text == null || text.trim().isEmpty()) {
                    try { text = alert.getAttribute("innerText"); } catch (Exception ignore) { }
                }
                if (text != null && !text.trim().isEmpty()) {
                    String t = text.trim();
                    log.info("Alert error text: '{}'", t);
                    return t;
                }
            }
        }

        if (!driver.findElements(alertPanelByClass).isEmpty()) {
            WebElement panel = driver.findElement(alertPanelByClass);
            if (panel.isDisplayed()) {
                String t = panel.getText();
                if (t == null || t.trim().isEmpty()) {
                    try { t = panel.getAttribute("innerText"); } catch (Exception ignore) { }
                }
                if (t != null && !t.trim().isEmpty()) return t.trim();
            }
        }

        log.info("No error text found");
        return "";
    }

    @Step("Get all visible error messages")
    public List<String> getAllVisibleErrorMessages() {
        List<String> messages = new ArrayList<>();
        List<WebElement> errorElements = driver.findElements(errorMsg);
        for (WebElement error : errorElements) {
            try {
                if (error.isDisplayed()) {
                    String t = error.getText();
                    if (t == null || t.trim().isEmpty()) {
                        try { t = error.getAttribute("innerText"); } catch (Exception ignore) { }
                    }
                    if (t != null && !t.trim().isEmpty()) messages.add(t.trim());
                }
            } catch (Exception ignore) { }
        }

        // Also collect alert message, if present
        try {
            if (!driver.findElements(alertMessage).isEmpty()) {
                WebElement alert = driver.findElement(alertMessage);
                if (alert.isDisplayed()) {
                    String text = alert.getText();
                    if (text == null || text.trim().isEmpty()) {
                        try { text = alert.getAttribute("innerText"); } catch (Exception ignore) { }
                    }
                    if (text != null && !text.trim().isEmpty()) messages.add(text.trim());
                }
            }
        } catch (Exception ignore) { }

        // Also collect alert panel by class, if present
        try {
            if (!driver.findElements(alertPanelByClass).isEmpty()) {
                WebElement panel = driver.findElement(alertPanelByClass);
                if (panel.isDisplayed()) {
                    String t = panel.getText();
                    if (t == null || t.trim().isEmpty()) {
                        try { t = panel.getAttribute("innerText"); } catch (Exception ignore) { }
                    }
                    if (t != null && !t.trim().isEmpty()) messages.add(t.trim());
                }
            }
        } catch (Exception ignore) { }

        log.info("Collected {} visible error messages", messages.size());
        return messages;
    }

    @Step("Verify user is logged in")
    public boolean isLoggedIn(){
        try{
            // Primary marker with longer timeout to accommodate slow transitions
            WaitUtils.waitForVisible(dashboardSelector, 15);
            log.info("Dashboard primary marker visible");
            return true;
        } catch(Exception primary){
            try {
                // Fallback: any dashboard icon variant
                WaitUtils.waitForVisible(dashboardIconAlt, 15);
                log.info("Dashboard icon alternative marker visible");
                return true;
            } catch (Exception alt) {
                try {
                    // Fallback: user avatar present
                    WaitUtils.waitForVisible(userAvatar, 15);
                    log.info("User avatar marker visible");
                    return true;
                } catch (Exception avatar) {
                    // Fallback: URL indicates dashboard/home
                    String url = driver.getCurrentUrl().toLowerCase();
                    boolean inferred = url.contains("dashboard") || url.endsWith("/") || url.contains("index");
                    log.info("Login inferred by URL '{}': {}", url, inferred);
                    return inferred;
                }
            }
        }
    }

    @Step("Click Forgot Password")
    public LoginPage clickForgotPassword() {
        log.info("Clicking Forgot Password link");
        ElementUtils.click(forgotPasswordLink);
        return this;
    }

    @Step("Click Sign Up Now")
    public LoginPage clickSignUpNow() {
        log.info("Clicking Sign Up link");
        ElementUtils.click(signUpLink);
        return this;
    }

    @Step("Click Back To Home")
    public LoginPage clickBackToHome() {
        log.info("Clicking Back To Home link");
        try {
            // Prefer a visibility wait + scroll to avoid strict clickable timeouts
            WebElement el = WaitUtils.waitForVisible(backToHome, 15);
            try {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});", el);
            } catch (Exception ignored) {}

            ScreenshotUtils.attachScreenshot("Before BackToHome click");
            try {
                el.click();
                log.info("Back To Home clicked (native)");
            } catch (Exception e) {
                log.warn("Native click failed on Back To Home. Attempting JS click.");
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                log.info("Back To Home clicked (JS)");
            } finally {
                ScreenshotUtils.attachScreenshot("After BackToHome click");
            }
        } catch (Exception ex) {
            log.error("Failed to click Back To Home: {}", ex.getMessage());
            throw ex;
        }
        return this;
    }

    @Step("Toggle Remember Me to {checked}")
    public LoginPage toggleRememberMe(boolean checked) {
        WebElement cb = driver.findElement(rememberMeCheckbox);
        if (cb.isSelected() != checked) {
            cb.click();
        }
        log.info("Remember Me desired={}, actual={}", checked, cb.isSelected());
        return this;
    }

    @Step("Open Forgot Password modal")
    public LoginPage openForgotPasswordModal() {
        log.info("Opening Forgot Password modal");
        ElementUtils.click(forgotPasswordLink); // triggers modal
        WaitUtils.waitForVisible(forgotPasswordModal);
        log.info("Forgot Password modal visible");
        return this;
    }

    @Step("Enter reset email: {email}")
    public LoginPage enterResetEmail(String email){
        log.info("Entering reset email: {}", email);
        ElementUtils.type(resetEmailInput, email);
        return this;
    }

    @Step("Submit reset form")
    public LoginPage submitResetForm(){
        log.info("Submitting reset form");
        ElementUtils.click(forgotPasswordSubmit);
        WaitUtils.waitForInvisibility(forgotPasswordModal);
        log.info("Reset form submitted, modal closed");
        return this;
    }

    @Step("Get reset password confirmation text")
    public String getResetPasswordConfirmationText(){
        String t = ElementUtils.getText(alertMessage);
        log.info("Reset confirmation text length: {}", t == null ? 0 : t.length());
        return t;
    }

    public boolean isForgotModalClosed() {
        try {
            return driver.findElements(forgotPasswordModal).isEmpty() || !driver.findElement(forgotPasswordModal).isDisplayed();
        } catch (Exception e) {
            return true;
        }
    }
}
