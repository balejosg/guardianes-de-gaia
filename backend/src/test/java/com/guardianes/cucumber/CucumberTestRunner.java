package com.guardianes.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.guardianes.cucumber",
        plugin = {
            "pretty",
            "html:target/cucumber-reports",
            "json:target/cucumber-reports/Cucumber.json",
            "junit:target/cucumber-reports/Cucumber.xml"
        },
        tags = "not @ignore")
public class CucumberTestRunner {}
