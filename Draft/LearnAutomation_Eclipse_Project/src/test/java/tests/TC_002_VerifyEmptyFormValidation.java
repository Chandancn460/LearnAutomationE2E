package tests;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ManageCoursesPage;
import utils.BaseTest;

/**
 * TC_002 – Verify that submitting the Add Course form with no data
 *           shows validation error messages.
 *
 * FIX APPLIED:
 *  – navigateToManageCourses() now correctly:
 *      1. Hovers over  //span[normalize-space()='Manage']
 *      2. Clicks       //a[normalize-space()='Manage Courses']
 *    matching the exact DOM structure confirmed in the screenshot.
 *  – Old XPath (//a[contains(text(),'Manage Courses')] with wrong href fallbacks)
 *    is completely replaced.
 */
public class TC_002_VerifyEmptyFormValidation extends BaseTest {

	 private static final String VALID_EMAIL    = "admin@email.com";
	    private static final String VALID_PASSWORD = "admin@123";

    @Test(priority = 2, description = "TC_002 – Validate empty form submission on Add Course")
    public void verifyEmptyFormValidation() {

        // Step 1 – Ensure logged in
        login(VALID_EMAIL, VALID_PASSWORD);

        // Step 2 – Navigate to Manage Courses via hover dropdown (KEY FIX)
        navigateToManageCourses();

        // Step 3 – Confirm we're on the right page
        Assert.assertTrue(driver.getCurrentUrl().contains("/course/manage"),
                "Did not land on Manage Courses page. URL: " + driver.getCurrentUrl());

        // Step 4 – Open the Add Course form and submit it empty
        ManageCoursesPage managePage = new ManageCoursesPage(driver, wait);
        managePage.submitEmptyForm();

        // Step 5 – Verify validation messages appear
        // Give the UI a moment to render inline errors
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    org.openqa.selenium.By.xpath(
                            "//*[contains(@class,'error') or contains(@class,'invalid') " +
                            "or contains(@class,'required') or contains(@class,'validation')]")));
        } catch (Exception e) {
            // Some apps rely on browser-native :invalid styles; check below
        }

        boolean errorsVisible = managePage.areValidationErrorsVisible();
        Assert.assertTrue(errorsVisible,
                "TC_002 FAILED – No validation errors shown after submitting empty form.");

        System.out.println("TC_002 PASSED – Validation errors displayed for empty form.");
    }
}
