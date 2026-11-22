package com.automation.ecommerce.base;

import com.automation.ecommerce.driver.DriverManager;
import com.automation.ecommerce.pages.HomePage;
import com.automation.ecommerce.pages.SignupPage;
import com.automation.ecommerce.reports.ExtentManager;
import com.automation.ecommerce.utils.ScreenshotUtil;
import com.automation.ecommerce.utils.TestDataReader;
import com.automation.ecommerce.utils.User;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverManager.initDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE && DriverManager.hasDriver()) {
                attachScreenshot("FAILED_" + result.getName());
            }
        } finally {
            DriverManager.quitDriver();
        }
    }

    /** Logs a test step to both the log file and the HTML report. */
    protected void step(String message) {
        log.info(message);
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.info(message);
        }
    }

    /** Takes a screenshot, saves it to target/screenshots and adds it to the HTML report. */
    protected void attachScreenshot(String title) {
        try {
            String base64 = ScreenshotUtil.capture(title.replaceAll("[^A-Za-z0-9_]", "_"));
            ExtentTest test = ExtentManager.getTest();
            if (test != null) {
                test.info(title, MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
            }
        } catch (Exception e) {
            log.warn("Could not take screenshot '{}': {}", title, e.getMessage());
        }
    }

    /** Reusable: registers a new user through the UI. The user is left logged in on the home page. */
    protected User signUpNewUser() {
        User user = TestDataReader.newUser();
        step("Sign up a new user: " + user.email());
        SignupPage signupPage = new HomePage().open().goToLogin().startSignup(user.name(), user.email());
        signupPage.fillAccountDetails(user).createAccount();
        Assert.assertTrue(signupPage.isAccountCreated(), "Account should be created");
        signupPage.continueToHome();
        return user;
    }
}
