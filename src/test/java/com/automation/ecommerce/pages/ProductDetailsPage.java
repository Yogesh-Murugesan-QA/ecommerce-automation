package com.automation.ecommerce.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ProductDetailsPage extends BasePage {

    // Name can include ad-injected anchors ("Men" + "Tshirt"), so tests assert with contains()
    private final By productName = By.cssSelector(".product-information h2");
    // Price is the span whose text starts with "Rs." and has no child elements
    private final By productPrice = By.xpath("//div[contains(@class,'product-information')]//span[starts-with(normalize-space(.),'Rs.')][not(*)]");
    private final By quantity = By.id("quantity");
    private final By addToCartButton = By.cssSelector("button.cart");
    private final By cartPopup = By.id("cartModal");
    private final By addedMessage = By.cssSelector("#cartModal .modal-body p");
    private final By viewCartLink = By.cssSelector("#cartModal a[href='/view_cart']");
    private final By continueShoppingButton = By.cssSelector("#cartModal button.close-modal");

    public ProductDetailsPage() {
        waitForVisible(productName);
    }

    public String getProductName() {
        return getText(productName);
    }

    public int getPrice() {
        return toAmount(getText(productPrice));
    }

    public ProductDetailsPage setQuantity(int qty) {
        type(quantity, String.valueOf(qty));
        return this;
    }

    public ProductDetailsPage addToCart() {
        log.info("Adding product to cart");
        // A click before the page's script has set up the button can do nothing, so wait for the page to finish loading
        waitForPageScriptsLoaded();
        click(addToCartButton);
        // Popup uses class "modal fade" and is hidden until the click; wait for it
        waitForVisible(cartPopup);
        return this;
    }

    public String getAddedToCartMessage() {
        waitForVisible(addedMessage);
        return getText(addedMessage);
    }

    public ProductDetailsPage continueShopping() {
        click(continueShoppingButton);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(cartPopup));
        return this;
    }

    public CartPage viewCart() {
        click(viewCartLink);
        return new CartPage();
    }
}
