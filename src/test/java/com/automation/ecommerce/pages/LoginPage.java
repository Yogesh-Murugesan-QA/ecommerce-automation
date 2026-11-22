package com.automation.ecommerce.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

/** The "Signup / Login" page: new-user sign-up form and existing-user login form. */
public class LoginPage extends BasePage {

    private final By signupName = By.cssSelector("input[data-qa='signup-name']");
    private final By signupEmail = By.cssSelector("input[data-qa='signup-email']");
    private final By signupButton = By.cssSelector("button[data-qa='signup-button']");
    private final By signupError = By.xpath("//form[@action='/signup']/p");

    private final By loginEmail = By.cssSelector("input[data-qa='login-email']");
    private final By loginPassword = By.cssSelector("input[data-qa='login-password']");
    private final By loginButton = By.cssSelector("button[data-qa='login-button']");
    private final By loginError = By.xpath("//form[@action='/login']/p");

    public LoginPage open() {
        openUrl("/login");
        waitForVisible(loginEmail);
        return this;
    }

    public SignupPage startSignup(String name, String email) {
        enterSignupDetails(name, email);
        return new SignupPage();
    }

    /** Submits the sign-up form when an error is expected (e.g. email already registered). */
    public LoginPage enterSignupDetails(String name, String email) {
        log.info("Signing up with name '{}' and email '{}'", name, email);
        type(signupName, name);
        type(signupEmail, email);
        click(signupButton);
        return this;
    }

    public String getSignupError() {
        return getText(signupError);
    }

    public HomePage login(String email, String password) {
        enterLoginDetails(email, password);
        return new HomePage();
    }

    /** Submits the login form when an error is expected (e.g. wrong credentials). */
    public LoginPage enterLoginDetails(String email, String password) {
        log.info("Logging in as '{}'", email);
        type(loginEmail, email);
        type(loginPassword, password);
        click(loginButton);
        return this;
    }

    public String getLoginError() {
        return getText(loginError);
    }

    /** Waits for the login form, so it is safe to call right after a click such as Logout. */
    public boolean isLoginFormDisplayed() {
        try {
            return waitForVisible(loginEmail).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
}
