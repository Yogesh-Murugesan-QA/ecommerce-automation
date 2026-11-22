# E-commerce Web Automation – SDET Assessment (Task 3)

[![UI Tests](https://github.com/Yogesh-Murugesan-QA/ecommerce-automation/actions/workflows/ui-tests.yml/badge.svg?branch=main)](https://github.com/Yogesh-Murugesan-QA/ecommerce-automation/actions/workflows/ui-tests.yml)

Selenium + Java + TestNG automation for an e-commerce site, using the Page Object Model.

## Application under test

**https://automationexercise.com**: a public demo e-commerce site built for test automation.

Amazon and Flipkart were not used because sign-up needs real phone OTPs, the sites use captcha/bot detection, and the assessment asks for a suitable test environment with no real payments. This site supports the full flow (sign-up, login, search, product, cart, checkout) with test accounts and no real payment.

## Tech stack

Java 17 · Selenium 4 · TestNG · Maven · Extent Reports · Log4j2 · Jackson (JSON test data)

## Project structure

```
src/test/java/com/automation/ecommerce
├── base/BaseTest.java          setup/teardown, screenshots, step logging, reusable sign-up
├── config/ConfigReader.java    loads config/<env>.properties, command-line overrides
├── driver/DriverManager.java   browser creation, ThreadLocal driver for parallel runs
├── pages/                      Page Object Model
│   ├── BasePage.java           reusable actions (click, type, waits) + site header
│   ├── HomePage, LoginPage, SignupPage, ProductsPage,
│   └── ProductDetailsPage, CartPage, CheckoutPage
├── reports/                    ExtentManager + TestListener (HTML report)
├── utils/                      TestDataReader, ScreenshotUtil, User
└── tests/
    ├── EndToEndFlowTest.java       complete happy flow
    ├── NegativeTests.java          invalid-data scenarios
    └── CartAndCheckoutTests.java   cart and checkout edge scenarios
src/test/resources
├── config/qa.properties, staging.properties
├── testdata/testdata.json
└── log4j2.xml
testng.xml                      suite (parallel execution)
```

## How to run

Prerequisites: JDK 17+, Maven 3.9+, Chrome. Browser drivers are downloaded automatically by Selenium Manager.

```bash
mvn clean test                                  # default: qa env, Chrome
mvn clean test -Dheadless=true                  # headless
mvn clean test -Dbrowser=edge                   # chrome | edge | firefox
mvn clean test -Denv=staging                    # another environment's config
```

You can also run `testng.xml` directly from IntelliJ or Eclipse.

### Run in GitHub Actions (CI)

The workflow in `.github/workflows/ui-tests.yml` runs the full suite in headless Chrome on every push to `main`, or on demand from the **Actions** tab (**Run workflow**). The Extent report, log and screenshots are uploaded as the `test-results` artifact on the run's summary page.

## Output

| What | Where |
| --- | --- |
| HTML report (steps, screenshots, errors) | `target/reports/ExtentReport.html` |
| Log file | `target/logs/automation.log` |
| Screenshots (key steps and failures) | `target/screenshots/` |

## Scenarios automated

| Test | Scenario(s) covered |
| --- | --- |
| `EndToEndFlowTest.completePurchaseFlow` | Happy flow: Sign up → Login → Search → Select product → Add to cart → Cart validation → Checkout (1, 3, 5, 7, 8, 12, 13) |
| `NegativeTests.signupWithExistingEmailIsRejected` | Sign-up with duplicate data (2) |
| `NegativeTests.loginWithInvalidCredentialsFails` | Login with invalid credentials (4) |
| `NegativeTests.searchForNonExistingProductReturnsNothing` | Search for a non-existing product (6) |
| `NegativeTests.checkoutWithoutLoginIsBlocked` | Checkout without required information (account/login) (14) |
| `NegativeTests.loginWithWrongPasswordFails` | Login with a registered email and wrong password (4) |
| `NegativeTests.loginWithEmptyEmailIsBlocked` | Login with an empty email, blocked by required-field validation (4) |
| `NegativeTests.signupWithInvalidEmailFormatIsBlocked` | Sign-up with invalid data: badly formatted email (2) |
| `CartAndCheckoutTests.modifyCartQuantity` | Modify cart quantity (9) |
| `CartAndCheckoutTests.removeProductFromCart` | Remove product from cart, also after refresh (10) |
| `CartAndCheckoutTests.removingLastProductEmptiesCart` | Remove product from cart: removing the only product leaves an empty cart (10) |
| `CartAndCheckoutTests.cartPersistsAfterLogin` | Cart persistence: guest cart kept after login (11) |
| `CartAndCheckoutTests.backNavigationDuringCheckout` | Back/Forward navigation during checkout (15) |

All 15 scenarios from the assessment are automated.

**Business assertions** include: the logged-in user's name after sign-up and login; the product appearing in search results; the cart price matching the product page price; quantity and total (price × quantity); delivery address details at checkout; and the order total matching the cart.

The tests stop at the checkout review page. No payment details are entered and no order is placed.

## Framework features

| Requirement | How it is done |
| --- | --- |
| Reusable functions | `BasePage` (click, type, waits, select, header navigation), `BaseTest.signUpNewUser()` |
| Page Object Model | One class per page under `pages/` |
| Test data management | `testdata.json`; a unique email is generated per run so tests never clash |
| Configuration / environments | `config/<env>.properties`, selected with `-Denv`; any key can be overridden with `-Dkey=value` |
| Meaningful assertions | Business checks on user, prices, quantity, totals and address, not just clicks |
| Logging | Log4j2 to console and `target/logs/automation.log` |
| Screenshots | At key steps and on every failure; saved to disk and embedded in the report |
| Error handling | Explicit waits with clear error messages; JavaScript click fallback when an ad overlay blocks a click; full-page ad detection; browser always closed in `finally` |
| Reporting | Extent HTML report through a TestNG listener |
| Parallel execution | `parallel="methods"` in `testng.xml` with a ThreadLocal driver per thread |

## Test accounts and environment

No credentials, VPN or private environment are needed. The site is public, and each test that needs a user creates a fresh account through the sign-up page with a unique email (`testuser_<random>@example.com`), then deletes it at the end. Profile details (name, address, phone) come from `testdata.json`.

## Notes

- The demo site's cart has no quantity editor, so the cart quantity is modified the way the site supports it: adding the same product again from its product page. `modifyCartQuantity` adds 1, then 2 more, and checks the cart shows one row with quantity 3 and the matching total.

- The demo site shows Google ads. The framework blocks common ad hosts in Chrome/Edge and reloads the page if a full-page ad appears.
- Test accounts are deleted at the end of each test that creates one.

## AI / External Assistance Declaration

| Item | Details |
| --- | --- |
| AI tools used | Claude (Anthropic), to help draft and structure the framework code and README |
| My contribution | Chose the application and scope, reviewed the code, ran and debugged the tests locally, and validated the locators and assertions against the live site |
| Other resources | Selenium, TestNG and Extent Reports documentation |
