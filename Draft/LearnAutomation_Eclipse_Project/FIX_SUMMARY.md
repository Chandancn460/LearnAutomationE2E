# LearnAutomation – Test Fix Summary

## Root Causes & Fixes

---

### Problem 1 – TC_002, TC_003, TC_004 all failing at "Navigate to Manage Courses"

**Error message:**
```
Expected condition failed: waiting for presence of element located by:
By.xpath: //a[contains(text(),'Manage Courses')] | //li[contains(.,'Manage Courses')]/a
          | //a[@href='/managecourse' or @href='/manage-courses']
```

**Why it failed:**

The "Manage Courses" link lives inside a **CSS hover dropdown**. 
Until the mouse hovers over the `<span>Manage</span>` trigger, the dropdown
`<div class="nav-menu-item-hover-div">` is hidden — Selenium cannot find or interact
with elements inside it.

Additionally, the href fallbacks in the old XPath were wrong:
| Old (broken) | Actual (from DevTools) |
|---|---|
| `@href='/managecourse'` | `@href='/course/manage'` |
| `@href='/manage-courses'` | (not used) |

**The fix — `BaseTest.navigateToManageCourses()`:**

```java
// Step 1 – hover over the Manage trigger to reveal the dropdown
WebElement manageSpan = wait.until(
    ExpectedConditions.presenceOfElementLocated(
        By.xpath("//span[normalize-space()='Manage']")));
actions.moveToElement(manageSpan).perform();

// Step 2 – wait for the Manage Courses link to become VISIBLE, then click
WebElement coursesLink = wait.until(
    ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//a[normalize-space()='Manage Courses']")));
actions.moveToElement(coursesLink).click().perform();

// Step 3 – confirm landing
wait.until(ExpectedConditions.urlContains("/course/manage"));
```

XPaths confirmed from **SelectorHub** in the screenshot:
- Manage trigger : `//span[normalize-space()='Manage']`
- Manage Courses : `//a[normalize-space()='Manage Courses']`  ← shown as "Rel XPath" in panel

---

### Problem 2 – TC_005 failing at "Logout"

**Error message:**
```
Expected condition failed: waiting for element to be clickable:
By.xpath: //button[contains(@class,'menu') or contains(@class,'avatar') or
          contains(@class,'user')] | //img[contains(@class,'avatar')]
          | //span[contains(@class,'user-icon')]
```

**Why it failed:**
The old XPath guessed at class names (`avatar`, `user-icon`, etc.) that simply do not
exist on this app's navbar.

**The fix:**
Use a text-based XPath that matches what the app actually renders when logged in:

```java
protected static final By LOGOUT_LINK = By.xpath(
    "//a[normalize-space()='Logout'] | //button[normalize-space()='Logout']");
```

TC_005 also adds a **Strategy B** fallback: if Logout is inside a user-menu dropdown,
hover over the user trigger first, then click Logout — the same hover pattern used
for Manage Courses.

---

## File structure

```
LearnAutomation/
├── pom.xml
└── src/test/
    ├── java/
    │   ├── utils/
    │   │   └── BaseTest.java          ← shared driver setup + navigateToManageCourses() FIX
    │   ├── pages/
    │   │   └── ManageCoursesPage.java ← Page Object for /course/manage
    │   └── tests/
    │       ├── TC_001_Login.java
    │       ├── TC_002_VerifyEmptyFormValidation.java
    │       ├── TC_003_AddNewCourse.java
    │       ├── TC_004_DeleteCourse.java
    │       └── TC_005_Logout.java
    └── resources/
        └── testng.xml
```

## How to run

```bash
# From the project root
mvn clean test
```

Or right-click `testng.xml` → Run in IntelliJ/Eclipse.

## ⚠️ Before running – update credentials

In each TC_00x file:
```java
private static final String VALID_EMAIL    = "test@gmail.com";   // ← your actual creds
private static final String VALID_PASSWORD = "Test@1234";
```

Or better: move them to a `config.properties` file and read with `Properties` class.
