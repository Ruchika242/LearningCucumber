# LearningCucumber Framework

This framework uses Selenium + Cucumber + TestNG with one parameterized runner and supports parallel cross-browser execution on Selenium Grid.

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

Run locally (non-grid) with browser override:

```powershell
mvn clean test -Dbrowser=chrome -Dgrid.enabled=false
```

Run all browsers in parallel on Selenium Grid (Chrome + Firefox + Edge):

```powershell
mvn clean test -Pci -Dgrid.enabled=true -Dgrid.url="http://10.0.0.153:4444"
```

Run with Cucumber tags:

```powershell
mvn clean test -Dcucumber.filter.tags="@Smoke"
```

Run with CI environment overrides:

```powershell
mvn clean test -Denv=ci -Dheadless=true
```

## Reports, Logs, and Screenshots

Generated artifacts:

- Single consolidated HTML report: `target/reports/cucumber-report.html`
- Framework logs: `target/logs/automation.log`
- Screenshots: `target/screenshots`

Screenshot behavior is controlled from `config.properties`:

- `screenshot.on.failure`
- `screenshot.on.pass`
- `screenshot.attach.to.report`
- `screenshot.output.dir`

## Parallel Execution Model

Parallelization is controlled by `src/test/resources/testNG.xml`:

- suite level `parallel="tests"`
- `thread-count="3"`
- three `<test>` entries with `browser` parameters (`chrome`, `firefox`, `edge`)
- all three point to the same parameterized runner: `runner.TestRunner`

This gives one test flow executed concurrently on three browsers while keeping one final HTML report.

## CI Matrix and Grid Capacity Policy

Recommended CI baseline:

- matrix axes: `browser`, `env`, `headless` (if your pipeline executes browser jobs independently)
- pre-flight Grid health check (`/status`) before test start
- strict session capacity alignment: TestNG `thread-count` must be <= Grid available slots
- retries only for infrastructure failures (session creation, node disconnect, transport timeout)
- no retry for assertion/functional failures

Timeout knobs are centrally configured in:

- `src/test/resources/config.properties`
- `src/test/resources/config-ci.properties`

Runtime override format:

```powershell
mvn clean test -Dpage.load.timeout=45 -Dscript.timeout=45 -Dimplicit.wait.timeout=0
```

