package com.automation.ecommerce.pages;

import org.openqa.selenium.By;

/** Checkout review page. The happy flow stops here; only the payment validation test goes on to the payment page, and no order is ever placed. */
public class CheckoutPage extends BasePage {

    private final By deliveryAddress = By.id("address_delivery");
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

    public boolean isProductListed(String name) {
        return isDisplayed(By.xpath(String.format(PRODUCT_ROW, name)));
    }

    public int getTotalAmount() {
        return toAmount(getText(totalAmount));
    }

    public boolean isPlaceOrderShown() {
        return isDisplayed(placeOrderButton);
    }

    /** "Place Order" only opens the payment form; nothing is ordered until that form is submitted. */
    public PaymentPage proceedToPayment() {
        click(placeOrderButton);
        return new PaymentPage();
    }
}
