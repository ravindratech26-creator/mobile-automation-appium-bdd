package com.saucelabs.mobile.context;

import com.saucelabs.mobile.pages.CartPage;
import com.saucelabs.mobile.pages.LoginPage;
import com.saucelabs.mobile.pages.ProductsPage;

/**
 * Per-scenario state, injected by cucumber-picocontainer into every step class.
 *
 * <p>Pages are created lazily: step classes are instantiated before the @Before hook starts the
 * driver, so building pages in constructors would fail. A new context (and new pages) is created
 * for every scenario, so no state leaks between scenarios.
 */
public class TestContext {

    private LoginPage loginPage;
    private ProductsPage productsPage;
    private CartPage cartPage;

    public LoginPage loginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage();
        }
        return loginPage;
    }

    public ProductsPage productsPage() {
        if (productsPage == null) {
            productsPage = new ProductsPage();
        }
        return productsPage;
    }

    public CartPage cartPage() {
        if (cartPage == null) {
            cartPage = new CartPage();
        }
        return cartPage;
    }
}
