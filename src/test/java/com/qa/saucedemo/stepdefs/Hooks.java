package com.qa.saucedemo.stepdefs;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cucumber hooks: run automatically before and after every scenario.
 * Equivalent to the old BaseTest's @BeforeMethod / @AfterMethod from TestNG.
 */
public class Hooks {

    private static final String BASE_URL = "https://www.saucedemo.com/";
    private static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");

    @Before
    public void setUp() {
        // WebDriverManager automatically downloads/matches the correct
        // ChromeDriver version for whatever Chrome is installed on this machine.
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // Running headless: this suite runs on a Jenkins server, which has
        // no visible display for Chrome to open a window in. --headless=new
        // runs Chrome without a UI; --window-size ensures a consistent
        // viewport so screenshots and element positions stay predictable
        // (headless Chrome defaults to a small window otherwise).
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        // Disable Chrome's password manager / breach-detection popups.
        // saucedemo.com's demo password ("secret_sauce") is publicly known,
        // so Chrome's "Change your password" leak-warning dialog pops up on
        // every login. This is a native browser dialog, NOT part of the page
        // DOM, so Selenium cannot see or dismiss it - but it silently sits on
        // top of the page and can intercept clicks on whatever is underneath
        // (e.g. the "Finish" button), causing confusing, hard-to-diagnose
        // timeouts. Disabling the feature outright is the standard fix for
        // test automation, where this prompt should never appear.
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--disable-features=PasswordLeakDetection,PasswordCheck");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.get(BASE_URL);

        DriverManager.setDriver(driver);
    }

    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = DriverManager.getDriver();

        if (scenario.isFailed() && driver != null) {
            captureFailureScreenshot(scenario, driver);
        }

        DriverManager.quitDriver();
    }

    /**
     * On failure, captures a screenshot two ways:
     *  1. Attached directly to the scenario, so it's embedded in the
     *     Cucumber JSON report (target/cucumber-reports/cucumber-report.json)
     *     - this is what the future AI analysis tool will read.
     *  2. Also saved as a standalone PNG under target/screenshots/, for
     *     quick manual viewing without digging through the JSON report.
     */
    private void captureFailureScreenshot(Scenario scenario, WebDriver driver) {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", scenario.getName());

            Files.createDirectories(SCREENSHOT_DIR);
            String safeName = scenario.getName().replaceAll("[^a-zA-Z0-9-_]", "_");
            Path screenshotFile = SCREENSHOT_DIR.resolve(safeName + ".png");
            Files.write(screenshotFile, screenshot);
        } catch (IOException e) {
            System.err.println("Failed to save failure screenshot for scenario '"
                    + scenario.getName() + "': " + e.getMessage());
        }
    }
}
