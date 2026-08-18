# LearningCucumber Framework

This framework uses Selenium + Cucumber + JUnit 5 and follows a layered configuration model for local and CI execution.

## Configuration Strategy

Priority order for all runtime values:

1. JVM system properties (`-Dkey=value`)
2. Environment variables
3. Environment file (`config-<env>.properties`)
4. Base defaults (`config.properties`)

Key files:

- `src/test/resources/config.properties`
- `src/test/resources/config-qa.properties`
- `src/test/resources/config-ci.properties`

## Execution

Run full suite with default profile:

```powershell
mvn clean test
```

Run in headless Chrome:

```powershell
mvn clean test -Dbrowser=chrome -Dheadless=true
```

Run with Cucumber tags:

```powershell
mvn clean test -Dcucumber.filter.tags="@smoke"
```

Run with CI environment overrides:

```powershell
mvn clean test -Denv=ci
```

Run with the CI Maven profile (headless + ci env + parallel enabled defaults):

```powershell
mvn clean test -Pci
```

## Reports, Logs, and Screenshots

Generated artifacts:

- Cucumber HTML: `target/reports/cucumber/cucumber.html`
- Cucumber JSON: `target/reports/cucumber/cucumber.json`
- Cucumber JUnit XML: `target/reports/cucumber/cucumber.xml`
- Cucumber rerun file: `target/reports/cucumber/rerun.txt`
- Extent Spark report: `target/reports/extent/ExtentReport.html`
- Framework logs: `target/logs/automation.log`
- Screenshots: `target/screenshots`

Screenshot behavior is controlled from `config.properties`:

- `screenshot.on.failure`
- `screenshot.on.pass`
- `screenshot.attach.to.report`
- `screenshot.output.dir`

## Parallel Execution

Default configuration is serial for stability. To enable Cucumber parallel execution:

```powershell
mvn clean test -Dcucumber.execution.parallel.enabled=true
```

You can adjust thread count in `src/test/resources/junit-platform.properties`.

