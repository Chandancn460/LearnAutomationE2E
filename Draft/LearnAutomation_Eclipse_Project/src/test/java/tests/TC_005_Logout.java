package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.BaseTest;

import java.util.Arrays;
import java.util.List;

/**
 * TC_005 – Logout after completing all course management operations.
 *
 * FIX APPLIED:
 *  The old XPath guessed at avatar/menu classes that do NOT exist on this app:
 *    //button[contains(@class,'menu') or contains(@class,'avatar') ...]
 *
 *  New strategy:
 *   1. Look for a plain text-based Logout link/button  →  //a[normalize-space()='Logout']
 *   2. If the app hides Logout inside a user-menu dropdown, hover over the
 *      user trigger first (by class "nav-menu-item-manage" sibling or a
 *      link containing the logged-in email/name), then click Logout.
 *   3. Fall back to JS click if a CSS overlay intercepts.
 *
 *  The LOGOUT_LINK constant is defined in BaseTest so all subclasses share it.
 */
public class TC_005_Logout extends BaseTest {

	 private static final String VALID_EMAIL    = "admin@email.com";
	    private static final String VALID_PASSWORD = "admin@123";

    @Test(priority = 5, description = "TC_005 – Logout from the application")
    public void verifyLogout() {

        // Step 1 – Ensure logged in
        login(VALID_EMAIL, VALID_PASSWORD);
        String postLoginUrl = driver.getCurrentUrl();
        Assert.assertFalse(postLoginUrl.contains("/login"),
                "Pre-condition failed: could not log in before TC_005.");

        // Step 2 – Find and click Logout
        //
        // Strategy A: Direct visible Logout link / button
        //   XPath: //a[normalize-space()='Logout'] | //button[normalize-space()='Logout']
        //
        // Strategy B: If app places Logout inside a user dropdown, first hover
        //   over the user trigger (email link in navbar), then click Logout.
        //
        // We try Strategy A first. If it times out we attempt Strategy B.
        //
        boolean loggedOut = false;

        // ── Strategy A ────────────────────────────────────────────
        try {
            List<By> logoutLocators = Arrays.asList(
                    By.xpath("//*[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'sign out')]"),
                    By.xpath("//*[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'logout')]")
            );

            for (By locator : logoutLocators) {
                List<WebElement> candidates = driver.findElements(locator);
                if (!candidates.isEmpty()) {
                    WebElement logoutEl = candidates.stream()
                            .filter(WebElement::isDisplayed)
                            .findFirst()
                            .orElse(candidates.get(0));
                    wait.until(ExpectedConditions.elementToBeClickable(logoutEl));
                    jsClick(logoutEl);
                    loggedOut = true;
                    break;
                }
            }
        } catch (Exception strategyAFailed) {
            System.out.println("TC_005 – Strategy A (direct Sign out/Logout) failed, " +
                    "trying Strategy B (user-menu hover)...");
        }

        // ── Strategy B: hover over user email/name trigger, then click Logout ─
        if (!loggedOut) {
            try {
                // The logged-in user's name or email is typically shown as a link
                // in the navbar – hover over it to reveal the dropdown
                                // Try to open any common user-menu trigger (avatar, menu button, profile link)
                                List<By> menuTriggers = Arrays.asList(
                                                By.cssSelector("button[aria-label*='menu'], button[class*='menu'], button[class*='avatar'], a[href*='/profile'], nav img, nav button"),
                                                By.xpath("//nav//a[contains(@href,'/profile') or contains(@class,'user') or contains(@class,'account')] | //nav//span[contains(@class,'user')]")
                                );

                                for (By trigger : menuTriggers) {
                                        try {
                                                List<WebElement> elems = driver.findElements(trigger);
                                                if (!elems.isEmpty()) {
                                                        WebElement candidate = elems.stream().filter(WebElement::isDisplayed).findFirst().orElse(elems.get(0));
                                                        try { candidate.click(); } catch (Exception e) { jsClick(candidate); }
                                                        pause(400);
                                                }
                                        } catch (Exception ignored) {}
                                }

                                // After opening menu(s), attempt to find the Sign out / Logout element
                                WebElement logoutEl = wait.until(
                                                ExpectedConditions.visibilityOfElementLocated(LOGOUT_LINK));
                                jsClick(logoutEl);
                                loggedOut = true;
            } catch (Exception strategyBFailed) {
                System.out.println("TC_005 – Strategy B failed: " + strategyBFailed.getMessage());
            }
        }

        Assert.assertTrue(loggedOut,
                "TC_005 FAILED – Could not locate or click the Logout button/link. " +
                "Inspect the logged-in navbar DOM and update LOGOUT_LINK in BaseTest.");

        // Step 3 – Confirm redirect to login or home
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.urlToBe(BASE_URL + "/"),
                ExpectedConditions.urlToBe(BASE_URL)));

        String afterLogoutUrl = driver.getCurrentUrl();
        boolean isLoggedOut = afterLogoutUrl.contains("/login")
                || afterLogoutUrl.equals(BASE_URL + "/")
                || afterLogoutUrl.equals(BASE_URL);

        Assert.assertTrue(isLoggedOut,
                "TC_005 FAILED – After clicking Logout, URL was: " + afterLogoutUrl);

        System.out.println("TC_005 PASSED – Logged out. Redirected to: " + afterLogoutUrl);
    }
}
