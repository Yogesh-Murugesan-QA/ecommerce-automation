package com.automation.ecommerce.pages;

import org.openqa.selenium.By;

import java.util.LinkedHashMap;
import java.util.Map;

/** Payment page. Tests never enter card details here and never place an order. */
public class PaymentPage extends BasePage {

    private final By nameOnCard = By.cssSelector("input[data-qa='name-on-card']");
    private final By cardNumber = By.cssSelector("input[data-qa='card-number']");
    private final By cvc = By.cssSelector("input[data-qa='cvc']");
    private final By expiryMonth = By.cssSelector("input[data-qa='expiry-month']");
    private final By expiryYear = By.cssSelector("input[data-qa='expiry-year']");
    private final By payButton = By.cssSelector("button[data-qa='pay-button']");
    private final By orderPlaced = By.cssSelector("h2[data-qa='order-placed']");

    public PaymentPage() {
        wait.until(d -> d.getCurrentUrl().contains("/payment"));
        waitForVisible(payButton);
    }

    /** Browser validation message of each card field, by field name. An empty message means the browser would accept the field as it is. */
    public Map<String, String> getCardFieldValidationMessages() {
        Map<String, String> messages = new LinkedHashMap<>();
        messages.put("Name on Card", getValidationMessage(nameOnCard));
        messages.put("Card Number", getValidationMessage(cardNumber));
        messages.put("CVC", getValidationMessage(cvc));
        messages.put("Expiry Month", getValidationMessage(expiryMonth));
        messages.put("Expiry Year", getValidationMessage(expiryYear));
        return messages;
    }

    /** Clicks Pay with the form left empty. Callers must first confirm every field is required, so the browser blocks it. */
    public PaymentPage submitEmptyForm() {
        log.info("Submitting the payment form with no card details");
        click(payButton);
        return this;
    }

    public boolean isOrderPlaced() {
        return isDisplayed(orderPlaced);
    }
}
