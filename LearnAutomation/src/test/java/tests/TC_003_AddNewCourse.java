package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ManageCoursesPage;
import utils.BaseTest;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TC_003 – Add a new course via the Manage Courses form.
 *
 * FIX APPLIED:
 *  – navigateToManageCourses() now uses the correct two-step hover+click
 *    pattern confirmed from the app's DOM:
 *      hover  → //span[normalize-space()='Manage']
 *      click  → //a[normalize-space()='Manage Courses']
 */
public class TC_003_AddNewCourse extends BaseTest {

	 private static final String VALID_EMAIL    = "admin@email.com";
	    private static final String VALID_PASSWORD = "admin@123";

    // Course details for this test run
    // NOTE: course name must be unique on every run — generate using current timestamp
    public static String TEST_COURSE_NAME;
    private static final String DESCRIPTION     = "Automated test course – can be deleted";
    private static final String INSTRUCTOR      = "QA Automation";
    private static final String START_DATE      = "06/01/2026";
    private static final String END_DATE        = "12/31/2026";
    private static final String PRICE           = "500";
    private static final String CATEGORY       = "Selenium";
    private static final boolean IS_PERMANENT   = true;
    private static final String THUMBNAIL_PATH = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dummy-thumbnail.png")
            .toAbsolutePath().toString();

    private String makeUniqueCourseName() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
        return "Selenium Automation QA - Auto Test - " + LocalDateTime.now().format(fmt);
    }

    @Test(priority = 3, description = "TC_003 – Add a new course")
    public void addNewCourse() {

        // Step 1 – Login
        login(VALID_EMAIL, VALID_PASSWORD);

        // Step 2 – Navigate to Manage Courses (hover + click, KEY FIX)
        navigateToManageCourses();

        Assert.assertTrue(driver.getCurrentUrl().contains("/course/manage"),
                "Did not land on Manage Courses page. URL: " + driver.getCurrentUrl());

        // Step 3 – Fill and submit the form
        ManageCoursesPage managePage = new ManageCoursesPage(driver, wait);
        String uniqueCourseName = makeUniqueCourseName();
        TEST_COURSE_NAME = uniqueCourseName;
        managePage.addCourse(TEST_COURSE_NAME, DESCRIPTION, INSTRUCTOR, START_DATE, END_DATE,
                PRICE, CATEGORY, IS_PERMANENT, THUMBNAIL_PATH);

        // Step 4 – Verify the new course appears in the listing
        // Wait up to 20 s for the card to appear (page may reload/re-render)
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(),'" + TEST_COURSE_NAME + "')]")));
        } catch (Exception e) {
            Assert.fail("TC_003 FAILED – New course '" + TEST_COURSE_NAME +
                    "' not found on the page after saving. " + e.getMessage());
        }

        System.out.println("TC_003 PASSED – Course added: " + TEST_COURSE_NAME);
    }
}
