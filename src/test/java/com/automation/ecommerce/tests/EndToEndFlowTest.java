package com.automation.ecommerce.tests;

import com.automation.ecommerce.base.BaseTest;
import com.automation.ecommerce.pages.CartPage;
import com.automation.ecommerce.pages.CheckoutPage;
import com.automation.ecommerce.pages.HomePage;
import com.automation.ecommerce.pages.LoginPage;
import com.automation.ecommerce.pages.ProductDetailsPage;
import com.automation.ecommerce.pages.ProductsPage;
import com.automation.ecommerce.pages.SignupPage;
import com.automation.ecommerce.utils.TestDataReader;
import com.automation.ecommerce.utils.User;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class EndToEndFlowTest extends BaseTest {

    @Test(groups = {"smoke", "regression"}, description = "Happy flow: Sign up > Login > Search > Select product > Add to cart > Cart validation > Checkout (stops before payment)")
    public void completePurchaseFlow() {
        String searchTerm = TestDataReader.get("product/searchTerm");
        String productName = TestDataReader.get("product/name");
        int quantity = TestDataReader.getInt("product/quantity");
        User user = TestDataReader.newUser();

        step("1. Sign up as a new user: " + user.email());
        SignupPage signupPage = new HomePage().open().goToLogin().startSignup(user.name(), user.email());
        Assert.assertEquals(signupPage.getPrefilledEmail(), user.email(), "Sign-up form should carry over the entered email");
        signupPage.fillAccountDetails(user).createAccount();
        Assert.assertTrue(signupPage.isAccountCreated(), "'Account Created!' message should be shown");
        HomePage home = signupPage.continueToHome();
        Assert.assertEquals(home.getLoggedInUserName(), user.name(), "New user should be logged in after sign-up");
        attachScreenshot("Account created");

        step("2. Log out and log in with the new account");
        LoginPage loginPage = home.logout();
        Assert.assertTrue(loginPage.isLoginFormDisplayed(), "Logout should show the login page");
        home = loginPage.login(user.email(), user.password());
        Assert.assertEquals(home.getLoggedInUserName(), user.name(), "User should be logged in with valid credentials");

        step("3. Search for '" + searchTerm + "'");
        ProductsPage productsPage = home.goToProducts().search(searchTerm);
        List<String> results = productsPage.getProductNames();
        Assert.assertTrue(results.contains(productName),
                "Search results should include '" + productName + "', but found: " + results);
        attachScreenshot("Search results");

        step("4. Select product '" + productName + "'");
        ProductDetailsPage productPage = productsPage.openProduct(productName);
        String shownName = productPage.getProductName();
        Assert.assertTrue(shownName.contains(productName),
                "The selected product's page should open; expected name to contain '" + productName + "' but was '" + shownName + "'");
        int price = productPage.getPrice();
        Assert.assertTrue(price > 0, "Product should have a valid price");

        step("5. Add " + quantity + " item(s) to cart at Rs. " + price + " each");
        productPage.setQuantity(quantity).addToCart();
        Assert.assertEquals(productPage.getAddedToCartMessage(), TestDataReader.get("messages/addedToCart"),
                "Add-to-cart confirmation should be shown");
        CartPage cartPage = productPage.viewCart();

        step("6. Validate the cart");
        Assert.assertEquals(cartPage.getNumberOfItems(), 1, "Cart should contain exactly one product");
        Assert.assertEquals(cartPage.getPrice(productName), price, "Cart price should match the product page price");
        Assert.assertEquals(cartPage.getQuantity(productName), quantity, "Cart quantity should match the quantity added");
        Assert.assertEquals(cartPage.getTotal(productName), price * quantity, "Cart total should be price x quantity");
        attachScreenshot("Cart validated");

        step("7. Proceed to checkout and validate order details");
        CheckoutPage checkoutPage = cartPage.proceedToCheckout();
        String address = checkoutPage.getDeliveryAddress();
        Assert.assertTrue(address.contains(user.firstName() + " " + user.lastName()), "Delivery address should show the user's name: " + address);
        Assert.assertTrue(address.contains(user.address()), "Delivery address should show the street address: " + address);
        Assert.assertTrue(address.contains(user.city()), "Delivery address should show the city: " + address);
        Assert.assertTrue(address.contains(user.mobile()), "Delivery address should show the mobile number: " + address);
        Assert.assertTrue(checkoutPage.isProductListed(productName), "Order review should list the product");
        Assert.assertEquals(checkoutPage.getTotalAmount(), price * quantity, "Order total should match the cart total");
        Assert.assertTrue(checkoutPage.isPlaceOrderShown(), "Place Order button should be available");
        attachScreenshot("Checkout review");
        step("Stopping before payment: no payment details entered and no order placed");

        step("8. Clean up: delete the test account");
        Assert.assertTrue(checkoutPage.deleteAccount(), "Test account should be deleted");
    }
}
