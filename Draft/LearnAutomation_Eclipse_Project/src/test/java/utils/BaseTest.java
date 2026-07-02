package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;

    // ── App constants ──────────────────────────────────────────────
    public static final String BASE_URL    = "https://freelance-learn-automation.vercel.app";
    public static final String LOGIN_URL   = BASE_URL + "/login";

    // Selectors updated to match the live UI labels used by the app.
    protected static final By MANAGE_SPAN           = By.xpath("//span[normalize-space()='Manage' or normalize-space()='Manage Courses']");
    protected static final By MANAGE_COURSES_LINK   = By.xpath("//a[normalize-space()='Manage Courses' or normalize-space()='Manage']");
    protected static final By MANAGE_CATEGORIES_LINK = By.xpath("//a[normalize-space()='Manage Categories']");
    protected static final By LOGOUT_LINK           = By.xpath(
            "//*[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'sign out') or " +
            "contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'logout')]");
    protected static final By EMAIL_FIELD    = By.cssSelector("input[type='email'], input[name='email'], input[placeholder*='Email']");
    protected static final By PASSWORD_FIELD = By.cssSelector("input[type='password'], input[name='password'], input[placeholder*='Password']");
    protected static final By LOGIN_BTN      = By.xpath(
            "//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in') or " +
            "normalize-space()='Login' or @value='Login']");

    // ── Driver lifecycle ───────────────────────────────────────────
    @BeforeClass
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Remove these two lines if you want a visible browser window during dev
        // options.addArguments("--headless=new");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");

        driver  = new ChromeDriver(options);
        wait    = new WebDriverWait(driver, Duration.ofSeconds(20));
        actions = new Actions(driver);
        driver.get(BASE_URL);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void captureScreenshot(String testName) {
        try {
            if (!(driver instanceof TakesScreenshot)) {
                return;
            }
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS"));
            Path target = Path.of("target", "screenshots", testName + "_" + timestamp + ".png");
            Files.createDirectories(target.getParent());
            Files.copy(src.toPath(), target);
            System.out.println("Screenshot saved: " + target.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            System.out.println("Failed to capture screenshot: " + e.getMessage());
        }
    }

    // ── Shared navigation helpers ──────────────────────────────────

    /**
     * Log in with the given credentials.
     * Navigates to the login page if not already there.
     */
    protected void login(String email, String password) {
        driver.get(LOGIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD)).clear();
        driver.findElement(EMAIL_FIELD).sendKeys(email);
        driver.findElement(PASSWORD_FIELD).clear();
        driver.findElement(PASSWORD_FIELD).sendKeys(password);

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BTN));
        try {
            loginButton.click();
        } catch (Exception e) {
            jsClick(loginButton);
        }

        wait.until(ExpectedConditions.or(
                ExpectedConditions.not(ExpectedConditions.urlContains("/login")),
                ExpectedConditions.visibilityOfElementLocated(LOGOUT_LINK)));
    }

    /**
     * KEY FIX: hover over the "Manage" span so the dropdown becomes visible,
     * then click "Manage Courses".
     *
     * Why this was broken before:
     *  – The old code jumped straight to //a[contains(text(),'Manage Courses')]
     *    without first revealing the dropdown via hover.
     *  – The href fallbacks (/managecourse, /manage-courses) don't match the
     *    actual href="/course/manage" shown in the DevTools panel.
     */
    protected void navigateToManageCourses() {
        if (driver.getCurrentUrl().contains("/course/manage")) {
            return;
        }

        try {
            WebElement manageSpan = wait.until(ExpectedConditions.presenceOfElementLocated(MANAGE_SPAN));
            actions.moveToElement(manageSpan).perform();
            WebElement coursesLink = wait.until(ExpectedConditions.visibilityOfElementLocated(MANAGE_COURSES_LINK));
            try {
                actions.moveToElement(coursesLink).click().perform();
            } catch (Exception e) {
                jsClick(coursesLink);
            }
        } catch (Exception e) {
            driver.get(BASE_URL + "/course/manage");
        }

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/course/manage"),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(.,'Manage Courses')]"))));
    }

    /**
     * Logout by clicking the Logout link/button in the navbar.
     * Falls back to JS click if the element is obscured.
     */
    protected void logout() {
        List<WebElement> logoutCandidates = driver.findElements(LOGOUT_LINK);
        if (logoutCandidates.isEmpty()) {
            logoutCandidates = driver.findElements(By.xpath("//button[contains(.,'Sign out')] | //a[contains(.,'Sign out')]"));
        }

        WebElement logoutEl = wait.until(ExpectedConditions.elementToBeClickable(logoutCandidates.isEmpty() ? By.xpath("//button[contains(.,'Sign out')] | //a[contains(.,'Sign out')]") : LOGOUT_LINK));
        try {
            logoutEl.click();
        } catch (Exception e) {
            jsClick(logoutEl);
        }
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.urlToBe(BASE_URL + "/"),
                ExpectedConditions.urlToBe(BASE_URL)));
    }

    /**
     * JavaScript click – bypasses overlay / intercepted-click issues.
     */
    protected void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Scroll element into viewport, then JS-click.
     * Useful for buttons near the fold.
     */
    protected void scrollAndClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", element);
        pause(300);
        jsClick(element);
    }

    /** Simple pause in ms – use sparingly; prefer explicit waits. */
    protected void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
