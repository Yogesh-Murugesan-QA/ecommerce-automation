package com.automation.ecommerce.pages;

import org.openqa.selenium.By;

import java.util.List;

public class ProductsPage extends BasePage {

    private final By searchBox = By.id("search_product");
    private final By searchButton = By.id("submit_search");
    private final By searchedProductsTitle = By.xpath(
            "//h2[contains(translate(.,'SEARCHEDPRODUCTS','searchedproducts'),'searched products')]");
    // Product names are read from the hover overlay: Google ad scripts can inject extra links/text
    // into the visible ".productinfo p", but the overlay copy keeps the clean product name.
    private final By productNames = By.cssSelector(".features_items .product-overlay .overlay-content p");
    private static final String VIEW_PRODUCT_LINK =
            "//div[contains(@class,'features_items')]//div[contains(@class,'product-image-wrapper')]"
                    + "[.//div[contains(@class,'overlay-content')]/p[normalize-space()='%s']]"
                    + "//a[contains(@href,'/product_details/')]";

    public ProductsPage() {
        waitForVisible(searchBox);
    }

    public ProductsPage search(String term) {
        log.info("Searching for '{}'", term);
        type(searchBox, term);
        click(searchButton);
        waitForVisible(searchedProductsTitle);
        return this;
    }

    public List<String> getProductNames() {
        return driver.findElements(productNames).stream()
                .map(element -> element.getDomProperty("textContent")) // overlay is hidden until hover
                .map(String::trim)
                .toList();
    }

    public ProductDetailsPage openProduct(String productName) {
        log.info("Opening product '{}'", productName);
        click(By.xpath(String.format(VIEW_PRODUCT_LINK, productName)));
        return new ProductDetailsPage();
    }
}
