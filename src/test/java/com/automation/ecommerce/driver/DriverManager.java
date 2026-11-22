package com.automation.ecommerce.driver;

import com.automation.ecommerce.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Creates one browser per test thread (ThreadLocal) so tests can run in parallel safely.
 * Drivers are downloaded automatically by Selenium Manager.
 */
public final class DriverManager {

    private static final Logger LOG = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    // The demo site loads many ads that slow pages and cover buttons; blocking them keeps runs stable.
    private static final String BLOCK_ADS = "--host-resolver-rules="
            + "MAP pagead2.googlesyndication.com 127.0.0.1, "
            + "MAP googleads.g.doubleclick.net 127.0.0.1, "
            + "MAP tpc.googlesyndication.com 127.0.0.1, "
            + "MAP adservice.google.com 127.0.0.1, "
            + "MAP fundingchoicesmessages.google.com 127.0.0.1";

    private DriverManager() {
    }

    public static void initDriver() {
        String browser = ConfigReader.get("browser").toLowerCase();
        boolean headless = ConfigReader.getBoolean("headless");
        LOG.info("Starting {} browser (headless={}, env={})", browser, headless, ConfigReader.getEnv());

        WebDriver driver;
        switch (browser) {
            case "firefox": {
                FirefoxOptions options = new FirefoxOptions();
                options.setPageLoadStrategy(PageLoadStrategy.EAGER);
                if (headless) {
                    options.addArguments("-headless");
                }
                driver = new FirefoxDriver(options);
                break;
            }
            case "edge": {
                EdgeOptions options = new EdgeOptions();
                options.setPageLoadStrategy(PageLoadStrategy.EAGER);
                options.addArguments("--window-size=1920,1080", BLOCK_ADS);
                if (headless) {
                    options.addArguments("--headless=new");
                }
                driver = new EdgeDriver(options);
                break;
            }
            case "chrome": {
                ChromeOptions options = new ChromeOptions();
                options.setPageLoadStrategy(PageLoadStrategy.EAGER);
                options.addArguments("--window-size=1920,1080", "--disable-notifications", BLOCK_ADS);
                if (headless) {
                    // --no-sandbox and --disable-dev-shm-usage keep Chrome stable on Linux CI runners
                    options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                }
                driver = new ChromeDriver(options);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser + " (use chrome, edge or firefox)");
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("page.load.seconds")));
        if (!headless) {
            driver.manage().window().maximize();
        }
        DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("Driver not started for this thread");
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                LOG.warn("Error while closing browser: {}", e.getMessage());
            } finally {
                DRIVER.remove();
            }
        }
    }
}
