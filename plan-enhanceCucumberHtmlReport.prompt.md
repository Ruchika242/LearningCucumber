# Plan: Enhance Cucumber HTML Report to Industry Standard

The goal is to drop ExtentReport entirely and replace/enrich the native **cucumber.html** output with a professional, feature-rich report using **Cluecumber** — the most widely adopted Maven plugin for Cucumber HTML reporting.

## Steps

1. **Remove ExtentReport from `pom.xml`** — delete the `extentreports-cucumber7-adapter` and `extentreports` dependencies so the project no longer carries unused weight.

2. **Clean up ExtentReport artefacts** — delete `src/test/resources/extent.properties` and `src/test/resources/extent-config.xml`; remove the `com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:` plugin entry from `junit-platform.properties`.

3. **Add Cluecumber Maven plugin to `pom.xml`** — configure the `cluecumber-engine` plugin (group `com.trivago.rta`) under `<build><plugins>` with the `reporting` goal bound to the `verify` phase; point `sourceJsonReportDirectory` at `target/reports/cucumber` (where `cucumber.json` already lands) and `generatedHtmlReportDirectory` at `target/reports/cluecumber`. This produces a rich static HTML report with charts, step timings, tag/feature filters, and scenario drill-down — all from the JSON already being generated.

4. **Tag feature files** `Login.feature` and `Dashboard.feature` — add `@Smoke`, `@Regression`, `@Login`, `@Dashboard` tags at Feature and Scenario level. Cluecumber renders these as clickable filter chips on the Tags page, giving full cross-feature visibility.

5. **Add `Description:` blocks to feature files** — add a prose description below each `Feature:` declaration; Cluecumber renders them as readable context on each feature card, making reports self-documenting for stakeholders.

6. **Enable screenshot-on-pass in `config.properties`** — set `screenshot.on.pass=true`; `scenario.attach()` already embeds the PNG bytes directly into the Cucumber JSON → Cluecumber renders them inline in each scenario step, meaning both pass and fail runs have visual evidence.

7. **Simplify `cucumber.plugin` in `junit-platform.properties`** — keep `pretty`, `summary`, `html:target/reports/cucumber/cucumber.html`, `json:target/reports/cucumber/cucumber.json`, `junit:target/reports/cucumber/cucumber.xml`, and `rerun:target/reports/cucumber/rerun.txt`; the JSON drives Cluecumber while the native HTML serves as a quick in-IDE fallback.

8. **Clean up `Hooks.java`** — remove any ExtentReport-specific imports or service calls now that the adapter is gone; the `scenario.attach()` screenshot call remains unchanged and continues to feed Cluecumber.

## Further Considerations

1. **Cluecumber customisation** — the plugin supports custom CSS, a logo URL, and a report title via `<customParameters>`; a company logo and branded colour scheme can be added with two extra config lines.
2. **Timestamped run history** — Cluecumber overwrites `target/reports/cluecumber/` each run; pair it with a CI `upload-artifact` step (e.g. GitHub Actions) to archive per-run reports without filling disk.
3. **Allure as a future upgrade** — if the project grows to 100+ scenarios, Allure (with its history trends and flaky-test tracking) is the de-facto enterprise choice and is worth considering as a next step.
