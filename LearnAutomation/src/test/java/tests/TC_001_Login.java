package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import utils.BaseTest;

/**
 * TC_001 – Verify successful login.
 *
 * Update EMAIL / PASSWORD with valid test credentials for the app.
 */
public class TC_001_Login extends BaseTest {

    // ── Test credentials – change as needed ───────────────────────
    private static final String VALID_EMAIL    = "admin@email.com";
    private static final String VALID_PASSWORD = "admin@123";

    @Test(priority = 1, description = "TC_001 – Login with valid credentials")
    public void verifyLogin() {
        login(VALID_EMAIL, VALID_PASSWORD);

        // After login the user should NOT be on the /login page
        String currentUrl = driver.getCurrentUrl();
        Assert.assertFalse(currentUrl.contains("/login"),
                "Login failed – still on login page. URL: " + currentUrl);

        System.out.println("TC_001 PASSED – Logged in. URL: " + currentUrl);
    }
}
