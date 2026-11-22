package com.automation.ecommerce.pages;

import com.automation.ecommerce.config.ConfigReader;
import com.automation.ecommerce.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/** Reusable actions and the site header shared by every page. */
public abstract class BasePage {

    private static final int OVERLOAD_RETRIES = 3;
    private static final String OVERLOADED = "under heavy load";     // the demo site's own overload page
    private static final String ACCESS_DENIED = "HTTP ERROR 403";    // Chrome's page when the site refuses access

    protected final Logger log = LogManager.getLogger(getClass());
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    private final By loginLink = By.cssSelector("#header a[href='/login']");
    private final By productsLink = By.cssSelector("#header a[href='/products']");
    private final By cartLink = By.cssSelector("#header a[href='/view_cart']");
    private final By logoutLink = By.cssSelector("#header a[href='/logout']");
    private final By deleteAccountLink = By.cssSelector("#header a[href='/delete_account']");
    private final By loggedInAs = By.xpath("//header//a[contains(.,'Logged in as')]");
    private final By accountDeleted = By.cssSelector("h2[data-qa='account-deleted']");

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("wait.seconds")));
    }

    // ---------- Reusable actions ----------

    protected void openUrl(String path) {
        String url = ConfigReader.get("base.url") + path;
        log.info("Opening {}", url);
        driver.get(url);
        closeAdIfShown();
        retryIfSiteOverloaded();
    }

    protected WebElement waitForVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new TimeoutException("Element not visible: " + locator + " on page " + describePage(), e);
        }
    }

    /** Clicks an element; if an ad or overlay blocks it, retries with a JavaScript click. */
    protected void click(By locator) {
        WebElement element;
        try {
            element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException e) {
            throw new TimeoutException("Element not clickable: " + locator + " on page " + describePage(), e);
        }
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Click blocked on {}, retrying with JavaScript", locator);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", element);
        }
        closeAdIfShown();
        retryIfSiteOverloaded();
    }

    protected void type(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText().trim();
    }

    /** Checks visibility right now (no wait). Safe if the element is removed while checking. */
    protected boolean isDisplayed(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            return !elements.isEmpty() && elements.get(0).isDisplayed();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    /** Waits for the element and returns true, or returns false if it does not appear in time. */
    protected boolean isShownAfterWait(By locator) {
        try {
            waitForVisible(locator);
            return true;
        } catch (TimeoutException e) {
            log.warn("Not shown: {}", e.getMessage());
            return false;
        }
    }

    /** The browser's built-in validation message for a form field (empty when the browser accepts the value). */
    protected String getValidationMessage(By field) {
        return waitForPresent(field).getDomProperty("validationMessage");
    }

    /** Waits until the element exists in the page (it may be styled/hidden, e.g. dropdowns wrapped by a UI plugin). */
    protected WebElement waitForPresent(By locator) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new TimeoutException("Element not found: " + locator + " on page " + describePage(), e);
        }
    }

    /**
     * Pages load with the EAGER strategy (we don't wait for ads), so a page's own scripts may still be
     * setting up buttons. Call this before clicking a button that relies on those scripts.
     */
    protected void waitForPageScriptsLoaded() {
        new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("page.load.seconds")))
                .until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    protected void selectByValue(By locator, String value) {
        new Select(waitForPresent(locator)).selectByValue(value);
    }

    protected void selectByText(By locator, String text) {
        new Select(waitForPresent(locator)).selectByVisibleText(text);
    }

    /** Converts "Rs. 1,000" to 1000. */
    protected int toAmount(String priceText) {
        return Integer.parseInt(priceText.replaceAll("[^0-9]", ""));
    }

    /** The demo site sometimes shows a full-page Google ad (URL ends with #google_vignette); reload without it. */
    private void closeAdIfShown() {
        String url = driver.getCurrentUrl();
        if (url != null && url.contains("#google_vignette")) {
            log.warn("Full-page ad shown, reloading page without it");
            driver.get(url.substring(0, url.indexOf('#')));
        }
    }

    /**
     * The public demo site sometimes answers with "This website is under heavy load (queue full)" instead of
     * the page. Wait and load the same URL again, up to 3 times. driver.get() is a plain GET, so a submitted
     * form is never sent twice; if a form's result is lost, the test's own checks fail on the next step.
     */
    private void retryIfSiteOverloaded() {
        for (int attempt = 1; attempt <= OVERLOAD_RETRIES && isErrorPage(OVERLOADED); attempt++) {
            log.warn("Site overloaded ('queue full'), retry {} of {} in {} s", attempt, OVERLOAD_RETRIES, attempt * 5);
            try {
                Thread.sleep(attempt * 5000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            driver.get(driver.getCurrentUrl());
        }
    }

    /** True when the page is a short error page (not a real site page) containing the given text. */
    private boolean isErrorPage(String text) {
        try {
            Object found = ((JavascriptExecutor) driver).executeScript(
                    "var t = document.body ? document.body.textContent : '';"
                            + "return t.length < 2000 && t.indexOf(arguments[0]) >= 0;", text);
            return Boolean.TRUE.equals(found);
        } catch (WebDriverException e) {
            // e.g. the page is mid-navigation; treat as not an error page and let the normal waits report problems
            return false;
        }
    }

    /** Current URL for error messages, noting when the site showed an error page instead of the real page. */
    private String describePage() {
        String url = driver.getCurrentUrl();
        if (isErrorPage(ACCESS_DENIED)) {
            return url + " (site returned HTTP 403 'Access denied': the site refused this machine, not a product defect)";
        }
        if (isErrorPage(OVERLOADED)) {
            return url + " (site showed 'under heavy load (queue full)', not the expected page)";
        }
        return url;
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void navigateBack() {
        log.info("Browser Back");
        driver.navigate().back();
    }

    public void navigateForward() {
        log.info("Browser Forward");
        driver.navigate().forward();
    }

    public void refreshPage() {
        log.info("Refreshing page");
        driver.navigate().refresh();
    }

    // ---------- Header (available on every page) ----------

    public LoginPage goToLogin() {
        click(loginLink);
        return new LoginPage();
    }

    public ProductsPage goToProducts() {
        click(productsLink);
        return new ProductsPage();
    }

    public CartPage goToCart() {
        click(cartLink);
        return new CartPage();
    }

    public String getLoggedInUserName() {
        return getText(loggedInAs).replace("Logged in as", "").trim();
    }

    public boolean isUserLoggedIn() {
        return isDisplayed(loggedInAs);
    }

    public LoginPage logout() {
        click(logoutLink);
        return new LoginPage();
    }

    public boolean deleteAccount() {
        click(deleteAccountLink);
        return isShownAfterWait(accountDeleted);
    }
}
