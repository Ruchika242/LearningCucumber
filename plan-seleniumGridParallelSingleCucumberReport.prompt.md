## Plan: Selenium Grid Parallel One Cucumber Report

This draft plan standardizes your framework for enterprise-style cross-browser execution: run the same Cucumber suite in parallel on Chrome, Firefox, and Edge via Selenium Grid, while producing one consolidated stakeholder report. The approach removes browser-specific test logic, centralizes runtime configuration, and keeps reporting pipeline deterministic in CI and local runs.

### Steps
1. Standardize execution engine in [pom.xml](pom.xml), [src/test/resources/testNG.xml](src/test/resources/testNG.xml), and `runner.*` around TestNG+Cucumber only.
2. Replace browser-specific runners with one parameterized runner (`runner.GridParallelRunner`) that reads `browser` from TestNG `@Parameters` and stores it in `BrowserContext` for thread-safe hooks and plugin access.
3. Refactor `drivermanager.DriverManager` to use `utilities.ConfigReader` (`grid.enabled`, `grid.url`, `headless`) and remove hardcoded Grid URL values.
4. Configure [src/test/resources/testNG.xml](src/test/resources/testNG.xml) with three parallel `<test>` blocks (Chrome, Firefox, Edge), all pointing to the same runner class and passing only different `browser` values.
5. Keep only one reporting owner: `reporting.ProfessionalCucumberHtmlPlugin`. Remove duplicate HTML/JSON report plugins from runner options to prevent browser-wise report fragmentation.
6. Ensure the plugin output path is single and stable (for example `target/reports/cucumber-report.html`) and that report generation runs once per suite in CI.
7. Align naming and portability standards across [src/test/resources/Features](src/test/resources/Features), `glue`, and `stepDefinations` package conventions; document run model in [README.md](README.md).

### Confirmed Decisions
1. Runner model: one parameterized runner only.
2. Report owner: `ProfessionalCucumberHtmlPlugin` only.
3. Scope expansion: include CI pipeline matrix and Grid capacity policy.

### CI Pipeline Matrix (Project-Level Standard)
1. Define CI matrix axis: `browser = [chrome, firefox, edge]`, `env = ci`, `headless = true`, with identical test scope and tags.
2. Keep one artifact naming strategy per job for raw logs/screenshots, but publish a single merged stakeholder HTML report from the final aggregation stage.
3. Add pre-check stage to validate Grid readiness (`/status`) before execution to fail fast on infrastructure issues.
4. Add post-execution stage for report publication and trend retention (build number, commit SHA, timestamp).

### Selenium Grid Capacity and Stability Policy
1. Max sessions: cap `thread-count` to Grid `maxSessions` and browser node slot count; do not exceed available slots.
2. Retries: allow controlled retry policy for infrastructure failures only (session not created, node disconnect, timeout), not for assertion failures.
3. Timeouts: standardize session creation timeout, command timeout, page-load timeout, and script timeout via config keys and CI defaults.
4. Queue and back-pressure: prefer queued execution over oversubscription; monitor node utilization and session wait time as release gates.
5. Observability: capture browser, node, session ID, and thread ID in logs and in the single HTML report for triage.
