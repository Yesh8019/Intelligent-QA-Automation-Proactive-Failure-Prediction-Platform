package com.qa.saucedemo.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Entry point that JUnit 4 (and Maven Surefire) actually executes.
 * Surefire's default include pattern (**&#47;*Test.java) picks this class up
 * automatically - no extra pom.xml configuration needed.
 *
 * Cucumber then reads the .feature files under "features" and matches each
 * Gherkin step to a method in the "glue" package (our step definitions).
 *
 * TAGGING
 * --------
 * Every scenario is tagged (see the .feature files) with a mix of:
 *   - module tags:   @login  @cart  @checkout
 *   - run-type tags: @smoke  @regression
 *   - nature tags:   @positive  @negative  @criticalPath  @knownFailure
 *
 * The "tags" attribute below controls which scenarios actually run.
 * "not @wip" means: run everything EXCEPT anything tagged @wip (work in
 * progress). No scenario currently has @wip, so by default the full suite
 * runs - this just demonstrates that tag filtering is wired up and ready.
 *
 * You can override this from the command line WITHOUT recompiling, e.g.:
 *   mvn test -Dcucumber.filter.tags="@smoke"                 (smoke tests only)
 *   mvn test -Dcucumber.filter.tags="@login or @cart"        (specific modules)
 *   mvn test -Dcucumber.filter.tags="@regression and not @knownFailure"
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.qa.saucedemo.stepdefs"},
//        tags = "not @wip",
        plugin = {
                "pretty",
                "junit:target/cucumber-reports/cucumber-junit-report.xml",
                "json:target/cucumber-reports/cucumber-report.json"
        }
)
public class RunCucumberTest {
}
