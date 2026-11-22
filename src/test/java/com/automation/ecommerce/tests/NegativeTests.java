package com.automation.ecommerce.tests;

import com.automation.ecommerce.base.BaseTest;
import com.automation.ecommerce.pages.CartPage;
import com.automation.ecommerce.pages.HomePage;
import com.automation.ecommerce.pages.LoginPage;
import com.automation.ecommerce.pages.ProductsPage;
import com.automation.ecommerce.utils.TestDataReader;
import com.automation.ecommerce.utils.User;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NegativeTests extends BaseTest {

    @Test(description = "Sign-up with an email that is already registered is rejected")
    public void signupWithExistingEmailIsRejected() {
        User user = signUpNewUser();

        step("Log out and try to sign up again with the same email");
        LoginPage loginPage = new HomePage().logout();
        loginPage.enterSignupDetails(TestDataReader.get("duplicateSignupName"), user.email());
        Assert.assertEquals(loginPage.getSignupError(), TestDataReader.get("messages/duplicateEmail"),
                "Duplicate email error should be shown");
        Assert.assertFalse(loginPage.isUserLoggedIn(), "No user should be logged in");
        Assert.assertTrue(loginPage.isLoginFormDisplayed(),
                "User should stay on the Signup / Login page, not move on to account details");
        attachScreenshot("Duplicate email error");

        step("Clean up: log in and delete the test account");
        Assert.assertTrue(loginPage.login(user.email(), user.password()).deleteAccount(), "Test account should be deleted");
    }

    @Test(description = "Login with invalid credentials shows an error and does not log in")
    public void loginWithInvalidCredentialsFails() {
        String email = TestDataReader.get("invalidLogin/email");
        String password = TestDataReader.get("invalidLogin/password");

        step("Log in with an unregistered email: " + email);
        LoginPage loginPage = new LoginPage().open().enterLoginDetails(email, password);
        Assert.assertEquals(loginPage.getLoginError(), TestDataReader.get("messages/invalidLogin"),
                "Invalid login error should be shown");
        Assert.assertFalse(loginPage.isUserLoggedIn(), "User should not be logged in");
        Assert.assertTrue(loginPage.getCurrentUrl().contains("/login"), "User should stay on the login page");
        attachScreenshot("Invalid login error");
    }

    @Test(description = "Login with a registered email but a wrong password shows an error and does not log in")
    public void loginWithWrongPasswordFails() {
        User user = signUpNewUser();
        String wrongPassword = TestDataReader.get("invalidLogin/password");

        step("Log out and log in with the correct email but a wrong password");
        LoginPage loginPage = new HomePage().logout().enterLoginDetails(user.email(), wrongPassword);
        Assert.assertEquals(loginPage.getLoginError(), TestDataReader.get("messages/invalidLogin"),
                "Invalid login error should be shown");
        Assert.assertFalse(loginPage.isUserLoggedIn(), "User should not be logged in");
        Assert.assertTrue(loginPage.getCurrentUrl().contains("/login"), "User should stay on the login page");
        attachScreenshot("Wrong password error");

        step("Clean up: log in with the correct password and delete the test account");
        Assert.assertTrue(loginPage.login(user.email(), user.password()).deleteAccount(), "Test account should be deleted");
    }

    @Test(description = "Login with an empty email is blocked by the form's required-field validation")
    public void loginWithEmptyEmailIsBlocked() {
        step("Submit the login form with an empty email");
        LoginPage loginPage = new LoginPage().open().enterLoginDetails("", TestDataReader.get("invalidLogin/password"));
        String validation = loginPage.getLoginEmailValidationMessage();
        Assert.assertTrue(validation != null && !validation.isEmpty(),
                "Browser should flag the empty email field, but validation message was: '" + validation + "'");
        Assert.assertFalse(loginPage.isLoginErrorShown(), "Form should not be submitted to the server");
        Assert.assertFalse(loginPage.isUserLoggedIn(), "User should not be logged in");
        attachScreenshot("Empty email blocked");
    }

    @Test(description = "Sign-up with a badly formatted email is blocked by the form's email validation")
    public void signupWithInvalidEmailFormatIsBlocked() {
        String badEmail = TestDataReader.get("invalidEmailFormat");

        step("Start sign-up with a badly formatted email: " + badEmail);
        LoginPage loginPage = new LoginPage().open().enterSignupDetails(TestDataReader.get("user/name"), badEmail);
        String validation = loginPage.getSignupEmailValidationMessage();
        Assert.assertTrue(validation != null && !validation.isEmpty(),
                "Browser should flag the email format, but validation message was: '" + validation + "'");
        Assert.assertTrue(loginPage.getCurrentUrl().contains("/login"),
                "User should stay on the Signup / Login page, not move on to account details");
        attachScreenshot("Invalid email format blocked");
    }

    @Test(description = "Searching for a product that does not exist returns no results")
    public void searchForNonExistingProductReturnsNothing() {
        String term = TestDataReader.get("invalidSearchTerm");

        step("Search for '" + term + "'");
        ProductsPage productsPage = new HomePage().open().goToProducts().search(term);
        Assert.assertTrue(productsPage.getProductNames().isEmpty(),
                "No products should be shown, but found: " + productsPage.getProductNames());
        attachScreenshot("No search results");
    }

    @Test(description = "Checkout without logging in is blocked and asks the user to register or log in")
    public void checkoutWithoutLoginIsBlocked() {
        String productName = TestDataReader.get("product/name");

        step("Add '" + productName + "' to cart as a guest");
        ProductsPage productsPage = new HomePage().open().goToProducts().search(TestDataReader.get("product/searchTerm"));
        CartPage cartPage = productsPage.openProduct(productName).addToCart().viewCart();
        Assert.assertTrue(cartPage.hasProduct(productName), "Product should be in the cart");

        step("Try to proceed to checkout without logging in");
        cartPage.proceedToCheckoutAsGuest();
        Assert.assertTrue(cartPage.getCheckoutLoginMessage().contains(TestDataReader.get("messages/checkoutLoginRequired")),
                "User should be asked to register or log in");
        Assert.assertTrue(cartPage.isRegisterLoginLinkShown(), "Register / Login link should be shown");
        Assert.assertTrue(cartPage.getCurrentUrl().contains("/view_cart"), "Guest should not reach the checkout page");
        attachScreenshot("Checkout blocked for guest");
    }
}
