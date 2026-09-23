package com.saucelabs.mobile.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Single entry point for all scenarios. What runs is decided at execution time, not here:
 * <pre>
 *   mvn test                                          # everything except @ignore, Android
 *   mvn test -Dcucumber.filter.tags="@smoke"          # smoke pack
 *   mvn test -Dplatform=ios -Dcucumber.filter.tags="@regression and not @logout"
 * </pre>
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.saucelabs.mobile.steps",
                "com.saucelabs.mobile.hooks"
        },
        plugin = {
                "pretty",
                "summary",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "rerun:target/cucumber-reports/rerun.txt",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {

    /**
     * Each scenario becomes a TestNG data row. parallel=true lets TestNG run them concurrently;
     * the actual thread count comes from testng.xml (data-provider-thread-count), so parallelism
     * is switched on per environment (1 local emulator vs. N cloud devices) without code changes.
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
