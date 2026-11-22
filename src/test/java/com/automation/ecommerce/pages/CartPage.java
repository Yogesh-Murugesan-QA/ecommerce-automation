package com.automation.ecommerce.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CartPage extends BasePage {

    private final By cartRows = By.cssSelector("tr[id^='product-']");
    private final By emptyCartMessage = By.id("empty_cart");
    private final By proceedToCheckoutButton = By.cssSelector("a.check_out");
    private final By checkoutLoginMessage = By.cssSelector("#checkoutModal .modal-body p");
    private final By checkoutModalLoginLink = By.cssSelector("#checkoutModal a[href='/login']");

    // Row of a given product in the cart table
    private static final String PRODUCT_ROW =
            "//tr[starts-with(@id,'product-')][.//td[contains(@class,'cart_description')]//a[normalize-space()='%s']]";

    /** Waits until the cart has loaded: either product rows or the "Cart is empty" message. */
    public CartPage() {
        wait.until(d -> d.getCurrentUrl().contains("/view_cart"));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(cartRows),
                ExpectedConditions.visibilityOfElementLocated(emptyCartMessage)));
    }

    public int getNumberOfItems() {
        return (int) driver.findElements(cartRows).stream().filter(WebElement::isDisplayed).count();
    }

    public String getEmptyCartMessage() {
        return getText(emptyCartMessage);
    }

    public boolean hasProduct(String name) {
        return isDisplayed(productRow(name));
    }

    public int getPrice(String name) {
        return toAmount(getText(inRow(name, "//td[contains(@class,'cart_price')]/p")));
    }

    public int getQuantity(String name) {
        return Integer.parseInt(getText(inRow(name, "//td[contains(@class,'cart_quantity')]/button")));
    }

    public int getTotal(String name) {
        return toAmount(getText(inRow(name, "//td[contains(@class,'cart_total')]/p")));
    }

    public CartPage removeProduct(String name) {
        log.info("Removing '{}' from cart", name);
        WebElement row = waitForVisible(productRow(name));
        click(inRow(name, "//a[contains(@class,'cart_quantity_delete')]"));
        wait.until(ExpectedConditions.invisibilityOf(row));
        return this;
    }

    public CheckoutPage proceedToCheckout() {
        click(proceedToCheckoutButton);
        return new CheckoutPage();
    }

    /** As a guest, checkout opens a popup asking the user to register or log in. */
    public CartPage proceedToCheckoutAsGuest() {
        click(proceedToCheckoutButton);
        waitForVisible(checkoutLoginMessage);
        return this;
    }

    public String getCheckoutLoginMessage() {
        return getText(checkoutLoginMessage);
    }

    public boolean isRegisterLoginLinkShown() {
        return isDisplayed(checkoutModalLoginLink);
    }

    private By productRow(String name) {
        return By.xpath(String.format(PRODUCT_ROW, name));
    }

    /** An element inside the given product's row, e.g. its price cell. */
    private By inRow(String name, String xpathInsideRow) {
        return By.xpath(String.format(PRODUCT_ROW, name) + xpathInsideRow);
    }
}
