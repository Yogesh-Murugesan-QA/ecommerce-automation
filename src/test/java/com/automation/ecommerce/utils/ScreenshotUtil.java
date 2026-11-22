package com.automation.ecommerce.utils;

import com.automation.ecommerce.driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public final class ScreenshotUtil {

    private ScreenshotUtil() {
    }

    /** Saves a PNG under target/screenshots and returns it as Base64 for the HTML report. */
    public static String capture(String name) throws IOException {
        byte[] image = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path folder = Paths.get("target", "screenshots");
        Files.createDirectories(folder);
        Files.write(folder.resolve(name + "_" + time + ".png"), image);
        return Base64.getEncoder().encodeToString(image);
    }
}
