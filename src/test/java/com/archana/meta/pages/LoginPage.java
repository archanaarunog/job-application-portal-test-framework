package com.archana.meta.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginPage {
    private final WebDriver driver;
    private final By email = By.id("email");
    private final By password = By.id("password");
    private final By loginBtn = By.id("loginBtnText");
    private final By errorMsg = By.cssSelector(".error-message");
    private final By alertMessage = By.id("alertMessage");
    private final By alertPanelByClass = By.cssSelector(".alert.alert-custom.alert-danger");
    private final By successMsg = By.xpath("//*[contains(normalize-space(.), 'Login successful! Redirecting')]");
    private final By dashboardSelector = By.xpath("//*[@class=\"bi bi-grid-fill me-2\"]");

    public LoginPage(WebDriver driver){
        this.driver = driver;
    }

    public LoginPage open(String baseUrl){
        driver.get(baseUrl + "/login.html");
        return this;
    }

    public LoginPage enterEmail(String e){
        driver.findElement(email).clear();
        driver.findElement(email).sendKeys(e);
        return this;
    }

    public LoginPage enterPassword(String p){
        driver.findElement(password).clear();
        driver.findElement(password).sendKeys(p);
        return this;
    }

    public void clickLogin(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    public boolean waitForTransientSuccessMessage(){
        try{
            WebDriverWait waitVisible = new WebDriverWait(driver, Duration.ofSeconds(6));
            waitVisible.until(ExpectedConditions.visibilityOfElementLocated(successMsg));
            WebDriverWait waitInvisible = new WebDriverWait(driver, Duration.ofSeconds(5));
            waitInvisible.until(ExpectedConditions.invisibilityOfElementLocated(successMsg));
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    /**
     * Wait for any known error element to become visible within the given timeout.
     * Returns true if any of the error selectors is visible, false otherwise.
     */
    public boolean waitForAnyErrorVisible(int seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
            wait.until(driver1 -> {
                List<WebElement> errs = driver1.findElements(errorMsg);
                for (WebElement e : errs) {
                    if (e.isDisplayed() && e.getText() != null && !e.getText().trim().isEmpty()) return true;
                }
                if (!driver1.findElements(alertMessage).isEmpty()) {
                    WebElement a = driver1.findElement(alertMessage);
                    if (a.isDisplayed() && ((a.getText() != null && !a.getText().trim().isEmpty()) || (a.getAttribute("innerText") != null && !a.getAttribute("innerText").trim().isEmpty()))) return true;
                }
                if (!driver1.findElements(alertPanelByClass).isEmpty()) {
                    WebElement p = driver1.findElement(alertPanelByClass);
                    if (p.isDisplayed() && ((p.getText() != null && !p.getText().trim().isEmpty()) || (p.getAttribute("innerText") != null && !p.getAttribute("innerText").trim().isEmpty()))) return true;
                }
                return false;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorText(){
        // 1) Inline field error (client-side validation) - only visible ones
        List<WebElement> errorElements = driver.findElements(errorMsg);
        for (WebElement error : errorElements) {
            if (error.isDisplayed() && error.getText() != null && !error.getText().trim().isEmpty()) {
                return error.getText().trim();
            }
        }
        if (!driver.findElements(alertMessage).isEmpty()) {
            WebElement alert = driver.findElement(alertMessage);
            if (alert.isDisplayed()) {
                String text = alert.getText();
                if (text == null || text.trim().isEmpty()) {
                    try {
                        text = alert.getAttribute("innerText");
                    } catch (Exception ignore) { }
                }
                if (text != null && !text.trim().isEmpty()) {
                    return text.trim();
                }
            }
        }

        if (!driver.findElements(alertPanelByClass).isEmpty()) {
            WebElement panel = driver.findElement(alertPanelByClass);
            if (panel.isDisplayed()) {
                String t = panel.getText();
                if (t == null || t.trim().isEmpty()) {
                    try {
                        t = panel.getAttribute("innerText");
                    } catch (Exception ignore) { }
                }
                if (t != null && !t.trim().isEmpty()) return t.trim();
            }
        }

        // 4) Nothing visible right now
        return "";
    }

    public boolean isLoggedIn(){
        try{
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(6));
            wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardSelector));
            return true;
        }
        catch(Exception e){
            return false;
        }
    }


}


