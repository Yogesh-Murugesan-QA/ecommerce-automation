package com.automation.ecommerce.tests;

import com.automation.ecommerce.base.BaseTest;
import com.automation.ecommerce.pages.CartPage;
import com.automation.ecommerce.pages.CheckoutPage;
import com.automation.ecommerce.pages.HomePage;
import com.automation.ecommerce.pages.ProductDetailsPage;
import com.automation.ecommerce.utils.TestDataReader;
import com.automation.ecommerce.utils.User;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CartAndCheckoutTests extends BaseTest {

    @Test(groups = "regression", description = "Adding more of a product already in the cart updates its cart quantity and line total")
    public void modifyCartQuantity() {
        String productName = TestDataReader.get("product/name");
        int newQuantity = TestDataReader.getInt("product/updatedQuantity");

        step("Add 1 x '" + productName + "' to cart");
        ProductDetailsPage productPage = new HomePage().open().goToProducts().openProduct(productName);
        int price = productPage.getPrice();
        CartPage cartPage = productPage.addToCart().viewCart();
        Assert.assertEquals(cartPage.getQuantity(productName), 1, "Cart should start with quantity 1");

        step("Add " + (newQuantity - 1) + " more to change the cart quantity from 1 to " + newQuantity);
        cartPage = cartPage.goToProducts().openProduct(productName).setQuantity(newQuantity - 1).addToCart().viewCart();

        step("Validate quantity and total in cart");
        Assert.assertEquals(cartPage.getNumberOfItems(), 1, "Product should stay a single cart row, not be added twice");
        Assert.assertEquals(cartPage.getQuantity(productName), newQuantity, "Cart should show the updated quantity");
        Assert.assertEquals(cartPage.getTotal(productName), price * newQuantity, "Cart total should be price x updated quantity");
        attachScreenshot("Quantity updated in cart");
    }

    @Test(groups = "regression", description = "Removing a product from the cart removes only that product, also after page refresh")
    public void removeProductFromCart() {
        String firstProduct = TestDataReader.get("product/name");
        String secondProduct = TestDataReader.get("secondProduct/name");

        step("Add '" + firstProduct + "' and '" + secondProduct + "' to cart");
        ProductDetailsPage productPage = new HomePage().open().goToProducts().openProduct(firstProduct);
        productPage.addToCart().continueShopping();
        CartPage cartPage = productPage.goToProducts().openProduct(secondProduct).addToCart().viewCart();
        Assert.assertEquals(cartPage.getNumberOfItems(), 2, "Cart should contain two products");

        step("Remove '" + firstProduct + "'");
        cartPage.removeProduct(firstProduct);
        Assert.assertFalse(cartPage.hasProduct(firstProduct), "Removed product should no longer be in the cart");
        Assert.assertTrue(cartPage.hasProduct(secondProduct), "Other product should still be in the cart");
        Assert.assertEquals(cartPage.getNumberOfItems(), 1, "Cart should contain one product");

        step("Refresh the cart and check the removal is saved");
        cartPage.refreshPage();
        CartPage refreshedCart = new CartPage();
        Assert.assertFalse(refreshedCart.hasProduct(firstProduct), "Removed product should not come back after refresh");
        Assert.assertEquals(refreshedCart.getNumberOfItems(), 1, "Cart should still contain one product after refresh");
        attachScreenshot("Product removed from cart");
    }

    @Test(groups = "regression", description = "Removing the only product leaves an empty cart, also after page refresh")
    public void removingLastProductEmptiesCart() {
        String productName = TestDataReader.get("product/name");

        step("Add '" + productName + "' to cart");
        CartPage cartPage = new HomePage().open().goToProducts().openProduct(productName).addToCart().viewCart();
        Assert.assertEquals(cartPage.getNumberOfItems(), 1, "Cart should contain one product");

        step("Remove it, refresh the cart and check it is empty");
        cartPage.removeProduct(productName);
        cartPage.refreshPage();
        CartPage refreshedCart = new CartPage();
        Assert.assertEquals(refreshedCart.getNumberOfItems(), 0, "Cart should contain no products");
        String message = refreshedCart.getEmptyCartMessage();
        Assert.assertTrue(message.contains(TestDataReader.get("messages/emptyCart")),
                "Empty cart message should be shown, but was: '" + message + "'");
        attachScreenshot("Empty cart");
    }

    @Test(groups = "regression", description = "Products added as a guest stay in the cart after the user logs in")
    public void cartPersistsAfterLogin() {
        String productName = TestDataReader.get("product/name");
        int quantity = TestDataReader.getInt("product/quantity");
        User user = signUpNewUser();

        step("Log out and add " + quantity + " x '" + productName + "' to cart as a guest");
        new HomePage().logout();
        ProductDetailsPage productPage = new HomePage().goToProducts().openProduct(productName);
        productPage.setQuantity(quantity).addToCart().continueShopping();

        step("Log in and open the cart");
        HomePage home = productPage.goToLogin().login(user.email(), user.password());
        Assert.assertEquals(home.getLoggedInUserName(), user.name(), "User should be logged in");
        CartPage cartPage = home.goToCart();
        Assert.assertTrue(cartPage.hasProduct(productName), "Product added as a guest should still be in the cart");
        Assert.assertEquals(cartPage.getQuantity(productName), quantity, "Quantity should be unchanged after login");
        attachScreenshot("Cart kept after login");

        step("Clean up: delete the test account");
        Assert.assertTrue(cartPage.deleteAccount(), "Test account should be deleted");
    }

    @Test(groups = "regression", description = "Browser Back from checkout returns to an unchanged cart, and Forward returns to the same order")
    public void backNavigationDuringCheckout() {
        String productName = TestDataReader.get("product/name");
        int quantity = TestDataReader.getInt("product/quantity");
        User user = signUpNewUser();

        step("Add product to cart and go to checkout");
        ProductDetailsPage productPage = new HomePage().goToProducts().openProduct(productName);
        int price = productPage.getPrice();
        CheckoutPage checkoutPage = productPage.setQuantity(quantity).addToCart().viewCart().proceedToCheckout();
        int orderTotal = checkoutPage.getTotalAmount();
        Assert.assertEquals(orderTotal, price * quantity, "Order total should be price x quantity");

        step("Press browser Back");
        checkoutPage.navigateBack();
        CartPage cartPage = new CartPage();
        Assert.assertTrue(cartPage.hasProduct(productName), "Product should still be in the cart after Back");
        Assert.assertEquals(cartPage.getQuantity(productName), quantity, "Quantity should be unchanged after Back");
        Assert.assertEquals(cartPage.getTotal(productName), orderTotal, "Cart total should be unchanged after Back");

        step("Press browser Forward");
        cartPage.navigateForward();
        CheckoutPage checkoutAgain = new CheckoutPage();
        Assert.assertTrue(checkoutAgain.isProductListed(productName), "Order review should still list the product");
        Assert.assertEquals(checkoutAgain.getTotalAmount(), orderTotal, "Order total should be unchanged after Forward");
        Assert.assertTrue(checkoutAgain.getDeliveryAddress().contains(user.firstName() + " " + user.lastName()),
                "Delivery address should still belong to the logged-in user");
        attachScreenshot("Checkout after Back and Forward");

        step("Clean up: delete the test account");
        Assert.assertTrue(checkoutAgain.deleteAccount(), "Test account should be deleted");
    }
}
