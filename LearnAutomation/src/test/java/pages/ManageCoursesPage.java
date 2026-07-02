package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;

/**
 * Page Object for the Manage Courses page (/course/manage).
 *
 * All locators are derived from the SelectorHub screenshot provided.
 */
public class ManageCoursesPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Manage Courses page locators ─────────────────────────────
    private static final By ADD_COURSE_BTN     = By.xpath(
            "//button[normalize-space()='Add New Course' or normalize-space()='Add Course' or normalize-space()='Add New']");

    private static final By COURSE_NAME_INPUT  = By.xpath("//h3[normalize-space()='Course Name']/following::input[1]");
    private static final By COURSE_DESC_INPUT  = By.xpath(
            "//h3[normalize-space()='Description']/following::textarea[1] | " +
            "//h3[normalize-space()='Description']/following::input[1]");
    private static final By CATEGORY_DROPDOWN  = By.xpath("//button[normalize-space()='Select Category' or contains(normalize-space(),'Select Category')]");
    private static final By CATEGORY_OPTION     = By.xpath("//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'selenium') or //li//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'selenium')] or //div[contains(@role,'option')]//button | //a[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'selenium')]");
    private static final By INSTRUCTOR_INPUT   = By.xpath("//h3[normalize-space()='Instructor']/following::input[1]");
    private static final By PRICE_INPUT        = By.xpath("//h3[normalize-space()='Price']/following::input[1]");
    private static final By START_DATE_INPUT   = By.xpath("//h3[normalize-space()='Starts From']/following::input[1]");
    private static final By END_DATE_INPUT     = By.xpath("//h3[normalize-space()='Ends On']/following::input[1]");
    private static final By THUMBNAIL_INPUT    = By.cssSelector("input[type='file']");
    private static final By PERMANENT_CHECKBOX = By.xpath("//h3[normalize-space()='Permanent']/following::input[1]");

    private static final By SUBMIT_BTN         = By.xpath(
            "//button[normalize-space()='Save' or normalize-space()='Add' or @type='submit']");

    // Validation error messages (empty-form submission)
    private static final By VALIDATION_ERRORS  = By.xpath(
            "//*[contains(@class,'error') or contains(@class,'invalid') or " +
            "contains(@class,'validation') or contains(@class,'required')]");

    // Course cards / rows on the listing
    private static final By COURSE_CARDS       = By.xpath(
            "//div[contains(@class,'course-card')] | //div[contains(@class,'courseCard')]");
    private static final By DELETE_COURSES_BTN = By.xpath(
            "//button[normalize-space()='Delete Courses' or normalize-space()='Delete Course' or contains(normalize-space(),'Delete Courses')]");
    private static final By COURSE_TABLE_ROWS  = By.xpath("//table//tbody//tr | //table//tr");
    private static final By COURSE_ROW_CHECKBOX = By.xpath(".//input[@type='checkbox' or @role='checkbox']");
    private static final By COURSE_ROW_DELETE_ACTION = By.xpath(
            ".//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'delete') " +
            "or contains(@aria-label,'delete') or contains(@title,'delete')]");

    // Delete button – relative to a course name
    private static final By DELETE_BTN_XPATH   = By.xpath(
            ".//button[normalize-space()='Delete' or contains(@class,'delete')]");

    // Confirm delete (modal or inline)
    private static final By CONFIRM_DELETE_BTN = By.xpath(
            "//button[normalize-space()='Confirm'] | //button[normalize-space()='Yes'] | " +
            "//button[normalize-space()='OK'] | //button[contains(@class,'confirm')]");

    // ── Constructor ──────────────────────────────────────────────
    public ManageCoursesPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait   = wait;
    }

    // ── Actions ──────────────────────────────────────────────────

    /** Verify the page heading / URL. Returns true if on Manage Courses page. */
    public boolean isOnManageCoursesPage() {
        return driver.getCurrentUrl().contains("/course/manage");
    }

    /** Click the "Add Course" button to open the course form. */
    public void clickAddCourse() {
        wait.until(ExpectedConditions.elementToBeClickable(ADD_COURSE_BTN)).click();
    }

    /**
     * Submit the Add Course form WITHOUT filling any fields.
     * Used by TC_002 to verify validation messages appear.
     */
    public void submitEmptyForm() {
        clickAddCourse();
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BTN)).click();
    }

    /** Returns true if at least one validation error message is visible. */
    public boolean areValidationErrorsVisible() {
        List<WebElement> errors = driver.findElements(VALIDATION_ERRORS);
        return errors.stream().anyMatch(WebElement::isDisplayed);
    }

    /**
     * Fill and submit the Add Course form with the supplied data.
     */
    public void addCourse(String name, String description, String instructor,
                          String startDate, String endDate) {
        addCourse(name, description, instructor, startDate, endDate, "0", null, false, null);
    }

    public void addCourse(String name, String description, String instructor,
                          String startDate, String endDate, String price,
                          String category, boolean permanent, String thumbnailPath) {
        clickAddCourse();

        fillField(COURSE_NAME_INPUT, name);
        fillField(COURSE_DESC_INPUT, description);
        fillField(INSTRUCTOR_INPUT, instructor);
        fillField(PRICE_INPUT, price);
        fillField(START_DATE_INPUT, startDate);
        fillField(END_DATE_INPUT, endDate);

        if (thumbnailPath != null && !thumbnailPath.isBlank()) {
            uploadThumbnail(thumbnailPath);
        }

        if (permanent) {
            checkPermanent();
        }

        selectCategory(category);

        try {
            WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BTN));
            jsClick(saveButton);
        } catch (Exception e) {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Save']"))).click();
        }
    }

    /**
     * Find the first course card whose title contains {@code courseName}
     * and click its Delete button.
     */
    public void deleteCourseByName(String courseName) {
        List<WebElement> rows = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(COURSE_TABLE_ROWS));

        String targetNormalized = normalizeForMatch(courseName);
        WebElement matchingRow = null;

        for (WebElement row : rows) {
            String text = row.getText();
            if (text == null || text.isBlank()) continue;
            if (matchesTokens(normalizeForMatch(text), targetNormalized)) {
                matchingRow = row;
                break;
            }
        }

        if (matchingRow == null) {
            if (isCourseAbsent(courseName)) {
                return;
            }
            throw new RuntimeException("Course not found on page: " + courseName);
        }

        if (!selectRowForDeletion(matchingRow)) {
            throw new RuntimeException("Found course row but could not activate deletion controls for: " + courseName);
        }

        clickDeleteCoursesButton();
    }

    private boolean selectRowForDeletion(WebElement row) {
        try {
            List<WebElement> checkboxes = row.findElements(COURSE_ROW_CHECKBOX);
            for (WebElement checkbox : checkboxes) {
                if (checkbox.isDisplayed() && checkbox.isEnabled()) {
                    jsClick(checkbox);
                    return true;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            List<WebElement> deleteActions = row.findElements(COURSE_ROW_DELETE_ACTION);
            if (!deleteActions.isEmpty()) {
                jsClick(deleteActions.get(0));
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            List<WebElement> allButtons = row.findElements(By.xpath(".//button | .//a[contains(@role,'button')]") );
            if (!allButtons.isEmpty()) {
                jsClick(allButtons.get(allButtons.size() - 1));
                return true;
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    private void clickDeleteCoursesButton() {
        try {
            WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(DELETE_COURSES_BTN));
            jsClick(deleteButton);
        } catch (Exception e) {
            throw new RuntimeException("Could not click top-level Delete Courses button", e);
        }

        List<WebElement> confirm = driver.findElements(CONFIRM_DELETE_BTN);
        if (!confirm.isEmpty()) {
            for (WebElement element : confirm) {
                try {
                    if (element.isDisplayed() && element.isEnabled()) {
                        element.click();
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    /** Returns true if no card with the given name is found on the page. */
    public boolean isCourseAbsent(String courseName) {
        String target = normalizeForMatch(courseName);
        List<WebElement> all = driver.findElements(By.xpath("//*"));
        for (WebElement el : all) {
            try {
                String t = el.getText();
                if (t == null || t.isBlank()) continue;
                if (matchesTokens(normalizeForMatch(t), target)) return false;
            } catch (Exception ignored) {}
        }
        return true;
    }

    // Normalize strings for matching: lowercase, replace common punctuation and dashes,
    // remove non-printable chars and collapse whitespace.
    private String normalizeForMatch(String s) {
        if (s == null) return "";
        String out = s.toLowerCase();
        out = out.replace('\u2013', '-') // en-dash
                 .replace('\u2014', '-') // em-dash
                 .replace('’','\'')
                 .replace('“','"')
                 .replace('”','"');
        out = out.replaceAll("[^a-z0-9\\-\\s]", " ");
        out = out.replaceAll("\\s+", " ").trim();
        return out;
    }

    private boolean matchesTokens(String haystack, String needle) {
        if (haystack == null || needle == null) return false;
        if (haystack.contains(needle) || needle.contains(haystack)) return true;
        String[] tokens = needle.split(" ");
        for (String t : tokens) {
            if (t.isBlank()) continue;
            if (!haystack.contains(t)) return false;
        }
        return true;
    }

    // ── Internal helpers ─────────────────────────────────────────
    private void fillField(By locator, String value) {
        List<WebElement> fields = driver.findElements(locator);
        if (!fields.isEmpty()) {
            WebElement field = fields.get(0);
            field.clear();
            field.sendKeys(value);
        }
    }

    private void uploadThumbnail(String filePath) {
        try {
            WebElement uploadInput = wait.until(ExpectedConditions.presenceOfElementLocated(THUMBNAIL_INPUT));
            // Ensure hidden file inputs are interactable and send the path directly.
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].style.display='block'; arguments[0].style.visibility='visible'; arguments[0].style.opacity='1'; arguments[0].removeAttribute('required');",
                        uploadInput);
            } catch (Exception ignored) {
            }
            uploadInput.sendKeys(filePath);
        } catch (Exception e) {
            throw new RuntimeException("Could not upload thumbnail file: " + filePath, e);
        }
    }

    private void checkPermanent() {
        try {
            WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(PERMANENT_CHECKBOX));
            if (!checkbox.isSelected()) {
                jsClick(checkbox);
            }
        } catch (Exception ignored) {
        }
    }

    private void selectCategory(String category) {
        if (category == null || category.isBlank()) {
            return;
        }

        try {
            WebElement categoryButton = wait.until(ExpectedConditions.elementToBeClickable(CATEGORY_DROPDOWN));
            if (categoryButton.isDisplayed()) {
                jsClick(categoryButton);
                // Try several common representations of category options
                List<WebElement> options = driver.findElements(By.xpath("//li | //ul//li | //div[contains(@role,'option')] | //a | //button"));
                for (WebElement option : options) {
                    try {
                        String optionText = option.getText().trim();
                        if (optionText.equalsIgnoreCase(category) || optionText.toLowerCase().contains(category.toLowerCase())) {
                            jsClick(option);
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                }
                // Fallback: try previously-defined CATEGORY_OPTION locators
                List<WebElement> fallback = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(CATEGORY_OPTION));
                if (!fallback.isEmpty()) {
                    jsClick(fallback.get(0));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }
}
