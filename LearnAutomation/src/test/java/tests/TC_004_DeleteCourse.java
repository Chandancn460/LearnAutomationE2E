package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ManageCoursesPage;
import utils.BaseTest;

/**
 * TC_004 – Delete the course that was added by TC_003.
 *
 * FIX APPLIED:
 *  – navigateToManageCourses() uses hover+click pattern matching the real DOM.
 *  – Old XPath that tried direct //a lookups without hover is completely removed.
 *
 * NOTE: This test depends on TC_003 having run first (same session / @BeforeClass
 * is shared via BaseTest).  If you run this standalone, ensure the test course
 * already exists in the app.
 */
public class TC_004_DeleteCourse extends BaseTest {

	 private static final String VALID_EMAIL    = "admin@email.com";
	    private static final String VALID_PASSWORD = "admin@123";

    // Course to delete will be read at runtime from TC_003 generated name

    @Test(priority = 4, description = "TC_004 – Delete an existing course")
    public void deleteCourse() {

        // Step 1 – Login (safe to call again; BaseTest navigates to /login)
        login(VALID_EMAIL, VALID_PASSWORD);

        // Step 2 – Navigate to Manage Courses (hover + click, KEY FIX)
        navigateToManageCourses();

        Assert.assertTrue(driver.getCurrentUrl().contains("/course/manage"),
                "Did not land on Manage Courses page. URL: " + driver.getCurrentUrl());

        // Step 3 – Delete the target course
        ManageCoursesPage managePage = new ManageCoursesPage(driver, wait);
        String courseToDelete = TC_003_AddNewCourse.TEST_COURSE_NAME;
        if (courseToDelete == null || courseToDelete.isBlank()) {
            Assert.fail("TC_004 PRECONDITION FAILED – no course name available from TC_003. Run TC_003 first or provide a course name.");
        }

        try {
            managePage.deleteCourseByName(courseToDelete);
            // Step 4 – Give the UI time to refresh, then assert absence
            pause(1500);
            Assert.assertTrue(managePage.isCourseAbsent(courseToDelete),
                    "TC_004 FAILED – Course '" + courseToDelete +
                    "' still visible after deletion.");
            System.out.println("TC_004 PASSED – Course deleted: " + courseToDelete);
        } catch (AssertionError | RuntimeException e) {
            captureScreenshot("TC_004_Failure");
            throw e;
        }
    }
}
