package com.automation.ecommerce.pages;

import org.openqa.selenium.By;

/** Checkout review page. Tests stop here: no payment details are entered and no order is placed. */
public class CheckoutPage extends BasePage {

    private final By deliveryAddress = By.id("address_delivery");
    private final By billingAddress = By.id("address_invoice");
    private final By totalAmount = By.xpath("//b[normalize-space()='Total Amount']/ancestor::tr//p[contains(@class,'cart_total_price')]");
    private final By placeOrderButton = By.cssSelector("a[href='/payment']");
    private static final String PRODUCT_ROW =
            "//tr[starts-with(@id,'product-')]//td[contains(@class,'cart_description')]//a[normalize-space()='%s']";

    public CheckoutPage() {
        waitForVisible(deliveryAddress);
    }

    public String getDeliveryAddress() {
        return getText(deliveryAddress);
    }

    public String getBillingAddress() {
        return getText(billingAddress);
    }

    public boolean isProductListed(String name) {
        return isDisplayed(By.xpath(String.format(PRODUCT_ROW, name)));
    }

    public int getTotalAmount() {
        return toAmount(getText(totalAmount));
    }

    public boolean isPlaceOrderShown() {
        return isDisplayed(placeOrderButton);
    }
}
