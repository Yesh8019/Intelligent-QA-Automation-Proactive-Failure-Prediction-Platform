# saucedemo-tests

Selenium + Cucumber (JUnit 4 runner) end-to-end test suite for [saucedemo.com](https://www.saucedemo.com/).

This is the **test project** piece of the *Intelligent QA Automation & Proactive Failure
Prediction Platform*. It produces test results (JUnit-style XML reports) that will later
be read by a separate AI-powered analysis tool to generate plain-English failure summaries
and a Go/No-Go release recommendation.

## What's inside

- **`src/test/resources/features/`** — Gherkin feature files describing test scenarios in plain English:
  - `Login.feature` — valid login, invalid login, locked-out user, and one **intentionally failing** scenario simulating a broken login button locator (Critical severity example)
  - `Cart.feature` — add single/multiple items, and one **intentionally failing** scenario with a wrong expected cart count (Low severity example)
  - `Checkout.feature` — full checkout flow, and one **intentionally failing** scenario simulating a checkout validation regression (High severity example)
- **`src/test/java/com/qa/saucedemo/stepdefs/`** — step definitions (the Java code behind each Gherkin line), plus `Hooks.java` (browser setup/teardown) and `DriverManager.java` (shares the WebDriver instance across step classes within a scenario)
- **`src/test/java/com/qa/saucedemo/runner/RunCucumberTest.java`** — the JUnit 4 runner (`@RunWith(Cucumber.class)`) that Maven actually executes
- **`src/test/java/com/qa/saucedemo/pages/`** — page objects (`LoginPage`, `InventoryPage`, `CheckoutPage`) — unchanged from before, since they only depend on `WebDriver`, not the test framework

## Tags

Every scenario is tagged so subsets can be run selectively (useful later in Jenkins —
e.g. run `@smoke` on every commit, full `@regression` before a release):

| Tag | Meaning |
|---|---|
| `@login` / `@cart` / `@checkout` | Feature/module tag |
| `@smoke` | Core happy-path scenario |
| `@regression` | Full regression scenario |
| `@positive` / `@negative` | Expected-success vs expected-error scenario |
| `@criticalPath` | Business-critical flow (login, checkout) |
| `@knownFailure` | One of the 3 intentionally-failing scenarios |

By default, `RunCucumberTest` runs everything (`tags = "not @wip"`, and nothing is
tagged `@wip` yet). Override from the command line without recompiling:

```bash
mvn test -Dcucumber.filter.tags="@smoke"                          # smoke tests only
mvn test -Dcucumber.filter.tags="@login or @cart"                 # specific modules
mvn test -Dcucumber.filter.tags="@regression and not @knownFailure"  # skip known failures
```

## Requirements

- JDK 24
- Maven 3.9+
- Google Chrome installed (WebDriverManager auto-downloads the matching ChromeDriver)

## How to run

**From IntelliJ:** Right-click `RunCucumberTest.java` → **Run**

**From terminal:**
```bash
mvn clean test
```

## Where the results go

Two report formats are generated after running:
- `target/cucumber-reports/cucumber-junit-report.xml` — **JUnit-style XML**, this is what our future AI analysis tool will read
- `target/cucumber-reports/cucumber-report.json` — full Cucumber JSON report (richer detail, useful later)

## Expected result

Running the full suite should show **3 intentional failures** out of 10 scenarios:
- `Login.feature` → "Login fails due to a broken button locator" — Critical
- `Checkout.feature` → "Checkout with a missing postal code" — High
- `Cart.feature` → "Cart badge count after removing an item" — Low

## ⚠️ Manual cleanup needed

I don't have a file-delete tool via the IntelliJ MCP connection, so please **manually delete**
these now-unused TestNG files in IntelliJ (right-click → Delete):
- `testng.xml` (project root)
- `src/test/java/com/qa/saucedemo/tests/BaseTest.java`
- `src/test/java/com/qa/saucedemo/tests/LoginTests.java`
- `src/test/java/com/qa/saucedemo/tests/CartTests.java`
- `src/test/java/com/qa/saucedemo/tests/CheckoutTests.java`
- the now-empty `tests` folder itself, once those 4 files are gone
