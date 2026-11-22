package com.automation.ecommerce.pages;

import com.automation.ecommerce.utils.User;
import org.openqa.selenium.By;

/** "Enter Account Information" page shown after the first sign-up step. */
public class SignupPage extends BasePage {

    // The radio button is styled by a jQuery "uniform" plugin, so the real <input> can be hidden behind
    // a styled wrapper. Clicking its <label> selects the radio reliably.
    private final By titleMr = By.cssSelector("label[for='id_gender1']");
    private final By email = By.cssSelector("input[data-qa='email']");
    private final By password = By.cssSelector("input[data-qa='password']");
    private final By day = By.cssSelector("select[data-qa='days']");
    private final By month = By.cssSelector("select[data-qa='months']");
    private final By year = By.cssSelector("select[data-qa='years']");
    private final By firstName = By.cssSelector("input[data-qa='first_name']");
    private final By lastName = By.cssSelector("input[data-qa='last_name']");
    private final By company = By.cssSelector("input[data-qa='company']");
    private final By address = By.cssSelector("input[data-qa='address']");
    private final By country = By.cssSelector("select[data-qa='country']");
    private final By state = By.cssSelector("input[data-qa='state']");
    private final By city = By.cssSelector("input[data-qa='city']");
    private final By zipcode = By.cssSelector("input[data-qa='zipcode']");
    private final By mobile = By.cssSelector("input[data-qa='mobile_number']");
    private final By createAccountButton = By.cssSelector("button[data-qa='create-account']");
    private final By accountCreated = By.cssSelector("h2[data-qa='account-created']");
    private final By continueButton = By.cssSelector("a[data-qa='continue-button']");

    public SignupPage() {
        waitForVisible(password);
    }

    public String getPrefilledEmail() {
        return waitForVisible(email).getDomProperty("value");
    }

    public SignupPage fillAccountDetails(User user) {
        log.info("Filling account details for {}", user.email());
        click(titleMr);
        type(password, user.password());
        selectByValue(day, user.birthDay());
        selectByValue(month, user.birthMonth());
        selectByValue(year, user.birthYear());
        type(firstName, user.firstName());
        type(lastName, user.lastName());
        type(company, user.company());
        type(address, user.address());
        selectByText(country, user.country());
        type(state, user.state());
        type(city, user.city());
        type(zipcode, user.zipcode());
        type(mobile, user.mobile());
        return this;
    }

    public SignupPage createAccount() {
        click(createAccountButton);
        return this;
    }

    public boolean isAccountCreated() {
        return waitForVisible(accountCreated).isDisplayed();
    }

    public HomePage continueToHome() {
        click(continueButton);
        return new HomePage();
    }
}
