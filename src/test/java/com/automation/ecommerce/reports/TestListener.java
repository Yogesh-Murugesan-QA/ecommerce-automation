package com.automation.ecommerce.reports;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/** Writes each test's result to the Extent report and the log. */
public class TestListener implements ITestListener {

    private static final Logger LOG = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info("===== START: {} =====", result.getName());
        ExtentManager.startTest(result.getName(), result.getMethod().getDescription());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("===== PASSED: {} =====", result.getName());
        ExtentManager.getTest().pass("Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("===== FAILED: {} - {} =====", result.getName(), result.getThrowable().getMessage());
        ExtentManager.getTest().fail(result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("===== SKIPPED: {} =====", result.getName());
        if (ExtentManager.getTest() != null) {
            ExtentManager.getTest().skip("Test skipped");
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
        LOG.info("Report saved to target/reports/ExtentReport.html");
    }
}
