package com.automation.ecommerce.pages;

import org.openqa.selenium.By;

public class HomePage extends BasePage {

    private final By featuredItems = By.cssSelector(".features_items");

    public HomePage open() {
        openUrl("/");
        waitForVisible(featuredItems);
        return this;
    }
}
