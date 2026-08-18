## Plan: Raise Framework to Industry Standards

This draft plan standardizes your framework in three layers: configuration, execution, and implementation quality. It keeps your current stack (Cucumber + JUnit 5 + Selenium) but introduces predictable environment control, CI-ready execution profiles, and consistent observability (reports, logs, screenshots) with clear toggles. The result is reliable local/CI runs, easier debugging, and cleaner long-term maintenance.

### Steps
1. Define target conventions by auditing [pom.xml](pom.xml), [junit-platform.properties](src/test/resources/junit-platform.properties), and `testRunner.TestRunner`.
2. Design layered configuration using `utilities.ConfigReader` with environment files under [src/test/resources](src/test/resources) and runtime overrides.
3. Standardize execution profiles in [pom.xml](pom.xml) for tags, browsers, parallelism, and CI/local modes via `maven-surefire-plugin`.
4. Unify reporting outputs in [junit-platform.properties](src/test/resources/junit-platform.properties), [extent.properties](src/test/resources/extent.properties), and [extent-config.xml](src/test/resources/extent-config.xml).
5. Implement observability hooks in `hooks.Hooks` and `utilities.ScreenshotUtil` for fail-only or conditional screenshots and scenario attachments.
6. Refactor core framework design in `drivermanager.DriverManagerClass`, `utilities.WaitUtils`, and step packages for maintainable, parallel-safe structure.

### Further Considerations
1. Which reporting stack should be primary? Option A Extent only / Option B Cucumber HTML+JSON only / Option C both.
2. Screenshot policy preference? Option A failures only / Option B failures+warnings / Option C configurable per tag.
3. Execution target priority? Option A local developer speed / Option B CI stability / Option C Selenium Grid scalability.

