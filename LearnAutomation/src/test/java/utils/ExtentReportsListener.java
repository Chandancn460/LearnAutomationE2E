package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExtentReportsListener implements ITestListener {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
    private static Path reportDir;

    @Override
    public void onStart(ITestContext context) {
        reportDir = Paths.get("target", "extent-report");
        reportDir.toFile().mkdirs();
        String fileName = "ExtentReport_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".html";
        ExtentSparkReporter spark = new ExtentSparkReporter(reportDir.resolve(fileName).toString());
        spark.config().setReportName("LearnAutomation Suite Report");
        spark.config().setDocumentTitle("Automation Execution Report");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Application", "Learn Automation Courses");
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("Browser", "Chrome");
        extent.setSystemInfo("User", System.getProperty("user.name"));

        System.out.println("Extent report will be generated at: " + reportDir.resolve(fileName).toAbsolutePath());
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName())
                .assignCategory(result.getTestContext().getName())
                .assignAuthor("Automation");
        testThread.set(test);
        test.log(Status.INFO, "Test started: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = testThread.get();
        test.log(Status.PASS, "Test passed");
        attachScreenshot(result, test);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = testThread.get();
        test.log(Status.FAIL, result.getThrowable());
        attachScreenshot(result, test);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = testThread.get();
        test.log(Status.SKIP, result.getThrowable());
        attachScreenshot(result, test);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // no-op
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }

    private void attachScreenshot(ITestResult result, ExtentTest test) {
        Object instance = result.getInstance();
        if (instance instanceof BaseTest) {
            BaseTest base = (BaseTest) instance;
            String screenshotPath = base.captureScreenshot(result.getMethod().getMethodName());
            if (screenshotPath != null && !screenshotPath.isBlank()) {
                Path screenshot = Paths.get(screenshotPath);
                Path relativePath = reportDir == null ? screenshot : reportDir.relativize(screenshot);
                test.addScreenCaptureFromPath(relativePath.toString());
            }
        }
    }
}
