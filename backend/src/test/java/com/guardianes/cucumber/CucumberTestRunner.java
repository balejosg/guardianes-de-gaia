package com.guardianes.cucumber;

import io.cucumber.junit.CucumberOptions;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasspathResource("features")
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
