package reporting;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfessionalCucumberHtmlPlugin implements ConcurrentEventListener {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final Object lock = new Object();
    private final Map<String, ScenarioResult> scenariosByKey = new LinkedHashMap<>();
    private final Map<String, String> featureNameByUri = new LinkedHashMap<>();

    private Instant testRunStartedAt;
    private Instant testRunFinishedAt;

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::onTestRunStarted);
        publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
        publisher.registerHandlerFor(EmbedEvent.class, this::onEmbedEvent);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::onTestRunFinished);
    }

    private void onTestRunStarted(TestRunStarted event) {
        synchronized (lock) {
            testRunStartedAt = event.getInstant();
        }
    }

    private void onTestCaseStarted(TestCaseStarted event) {
        synchronized (lock) {
            TestCase testCase = event.getTestCase();
            String key = scenarioKey(testCase);
            ScenarioResult scenario = new ScenarioResult();
            scenario.feature = resolveFeatureName(testCase.getUri());
            scenario.scenario = testCase.getName();
            scenario.browser = resolveBrowser();
            scenario.executionStart = event.getInstant();
            scenariosByKey.put(key, scenario);
        }
    }

    private void onTestStepFinished(TestStepFinished event) {
        synchronized (lock) {
            String key = scenarioKey(event.getTestCase());
            ScenarioResult scenario = scenariosByKey.get(key);
            if (scenario == null) {
                return;
            }

            TestStep testStep = event.getTestStep();
            if (!(testStep instanceof PickleStepTestStep pickleStep)) {
                return;
            }

            Result result = event.getResult();
            StepResult step = new StepResult();
            step.keyword = nonBlank(pickleStep.getStep().getKeyword(), "Step").trim();
            step.text = nonBlank(pickleStep.getStepText(), "").trim();
            step.status = normalizeStatus(result.getStatus());
            step.durationMs = durationToMillis(result.getDuration());
            step.errorMessage = extractErrorMessage(result.getError());
            step.stackTrace = extractStackTrace(result.getError());

            scenario.steps.add(step);
            scenario.durationMs += step.durationMs;

            if ("FAILED".equals(step.status) && scenario.failedStep == null) {
                scenario.failedStep = (step.keyword + " " + step.text).trim();
                scenario.failedErrorMessage = step.errorMessage;
                scenario.failedStackTrace = step.stackTrace;
            }

            if ("UNDEFINED".equals(step.status) && scenario.failedStep == null) {
                scenario.failedStep = (step.keyword + " " + step.text).trim();
                scenario.failedErrorMessage = step.errorMessage;
                scenario.failedStackTrace = step.stackTrace;
            }
        }
    }

    private void onEmbedEvent(EmbedEvent event) {
        synchronized (lock) {
            if (event.getData() == null || event.getData().length == 0) {
                return;
            }

            String mediaType = nonBlank(event.getMediaType(), "").toLowerCase(Locale.ROOT);
            if (!mediaType.startsWith("image/")) {
                return;
            }

            ScenarioResult scenario = scenariosByKey.get(scenarioKey(event.getTestCase()));
            if (scenario == null) {
                return;
            }

            scenario.failureScreenshotBase64 = Base64.getEncoder().encodeToString(event.getData());
            scenario.failureScreenshotMimeType = mediaType;
        }
    }

    private void onTestCaseFinished(TestCaseFinished event) {
        synchronized (lock) {
            ScenarioResult scenario = scenariosByKey.get(scenarioKey(event.getTestCase()));
            if (scenario == null) {
                return;
            }

            Result result = event.getResult();
            scenario.status = normalizeStatus(result.getStatus());
            scenario.executionEnd = event.getInstant();

            if (scenario.durationMs == 0L) {
                scenario.durationMs = durationToMillis(result.getDuration());
            }

            if ("FAILED".equals(scenario.status) && scenario.failedErrorMessage == null) {
                scenario.failedErrorMessage = extractErrorMessage(result.getError());
                scenario.failedStackTrace = extractStackTrace(result.getError());
            }
        }
    }

    private void onTestRunFinished(TestRunFinished event) {
        synchronized (lock) {
            testRunFinishedAt = event.getInstant();
            generateReport();
        }
    }

    private void generateReport() {
        List<ScenarioResult> scenarios = new ArrayList<>(scenariosByKey.values());

        int totalScenarios = scenarios.size();
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        int undefined = 0;
        int totalSteps = 0;

        Map<String, FeatureSummary> featureSummary = new LinkedHashMap<>();
        Map<String, BrowserSummary> browserSummary = initializeBrowserSummary();
        List<ScenarioResult> failedScenarios = new ArrayList<>();

        for (ScenarioResult scenario : scenarios) {
            totalSteps += scenario.steps.size();

            featureSummary.putIfAbsent(scenario.feature, new FeatureSummary(scenario.feature));
            FeatureSummary feature = featureSummary.get(scenario.feature);
            feature.total++;

            browserSummary.putIfAbsent(normalizeBrowserName(scenario.browser), new BrowserSummary(normalizeBrowserName(scenario.browser)));
            BrowserSummary browser = browserSummary.get(normalizeBrowserName(scenario.browser));
            browser.total++;

            switch (scenario.status) {
                case "PASSED" -> {
                    passed++;
                    feature.passed++;
                    browser.passed++;
                }
                case "FAILED" -> {
                    failed++;
                    feature.failed++;
                    browser.failed++;
                    failedScenarios.add(scenario);
                }
                case "UNDEFINED" -> {
                    undefined++;
                    feature.undefined++;
                    browser.undefined++;
                }
                default -> {
                    skipped++;
                    feature.skipped++;
                    browser.skipped++;
                }
            }
        }

        long executionTimeMs = calculateExecutionTimeMillis(scenarios);
        double passPercent = percentage(passed, totalScenarios);
        double failPercent = percentage(failed, totalScenarios);

        String html = buildHtml(
                scenarios,
                failedScenarios,
                featureSummary,
                browserSummary,
                totalScenarios,
                passed,
                failed,
                skipped,
                undefined,
                passPercent,
                failPercent,
                featureSummary.size(),
                totalSteps,
                executionTimeMs
        );

        try {
            Path output = Path.of("target", "reports", "cucumber-report.html");
            Files.createDirectories(output.getParent());
            Files.writeString(output, html, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate target/cucumber-report.html", e);
        }
    }

    private String buildHtml(
            List<ScenarioResult> scenarios,
            List<ScenarioResult> failedScenarios,
            Map<String, FeatureSummary> featureSummary,
            Map<String, BrowserSummary> browserSummary,
            int totalScenarios,
            int passed,
            int failed,
            int skipped,
            int undefined,
            double passPercent,
            double failPercent,
            int totalFeatures,
            int totalSteps,
            long executionTimeMs
    ) {
        String executionMode = resolveExecutionMode();
        String browser = normalizeBrowserName(resolveBrowser());
        String platform = nonBlank(System.getProperty("os.name"), "Unknown") + " " + nonBlank(System.getProperty("os.version"), "");
        String gridInfo = resolveFirstNonBlank(
                System.getProperty("selenium.grid.url"),
                System.getProperty("grid.url"),
                System.getProperty("remote.url"),
                System.getProperty("webdriver.remote.url"),
                System.getenv("SELENIUM_GRID_URL"),
                "Not available"
        );
        String nodeInfo = resolveFirstNonBlank(
                System.getProperty("selenium.grid.node"),
                System.getProperty("grid.node"),
                System.getenv("SELENIUM_GRID_NODE"),
                "Not available"
        );

        String runStarted = testRunStartedAt == null ? "Not available" : TIMESTAMP_FORMATTER.format(testRunStartedAt);
        String runFinished = testRunFinishedAt == null ? "Not available" : TIMESTAMP_FORMATTER.format(testRunFinishedAt);

        StringBuilder html = new StringBuilder(64_000);
        html.append("<!DOCTYPE html>\n")
                .append("<html lang=\"en\">\n")
                .append("<head>\n")
                .append("  <meta charset=\"UTF-8\">\n")
                .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                .append("  <title>Cucumber BDD Execution Report</title>\n")
                .append("  <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n")
                .append("  <style>\n")
                .append("    :root {\n")
                .append("      --bg: #f4f6fb;\n")
                .append("      --panel: #ffffff;\n")
                .append("      --ink: #1f2937;\n")
                .append("      --muted: #6b7280;\n")
                .append("      --ok: #22c55e;\n")
                .append("      --fail: #ef4444;\n")
                .append("      --skip: #f59e0b;\n")
                .append("      --undef: #7c3aed;\n")
                .append("      --accent: #2563eb;\n")
                .append("      --border: #e5e7eb;\n")
                .append("    }\n")
                .append("    * { box-sizing: border-box; }\n")
                .append("    body { margin: 0; background: var(--bg); color: var(--ink); font-family: 'Segoe UI', Arial, sans-serif; }\n")
                .append("    .container { max-width: 1400px; margin: 0 auto; padding: 24px; }\n")
                .append("    h1 { margin: 0 0 8px; font-size: 30px; }\n")
                .append("    .subtitle { color: var(--muted); margin-bottom: 24px; }\n")
                .append("    .section { background: var(--panel); border: 1px solid var(--border); border-radius: 12px; padding: 18px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(17, 24, 39, 0.05); }\n")
                .append("    .section h2 { margin: 0 0 16px; font-size: 22px; }\n")
                .append("    .grid { display: grid; gap: 12px; }\n")
                .append("    .grid.cards { grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); }\n")
                .append("    .card { border: 1px solid var(--border); border-radius: 10px; padding: 14px; background: #fff; }\n")
                .append("    .card .k { color: var(--muted); font-size: 13px; margin-bottom: 6px; }\n")
                .append("    .card .v { font-size: 24px; font-weight: 700; }\n")
                .append("    .ok { color: var(--ok); } .fail { color: var(--fail); } .skip { color: var(--skip); } .undef { color: var(--undef); }\n")
                .append("    .charts { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 20px; }\n")
                .append("    canvas { width: 100%; max-height: 350px; }\n")
                .append("    table { width: 100%; border-collapse: collapse; font-size: 14px; }\n")
                .append("    th, td { padding: 10px 12px; border-bottom: 1px solid var(--border); text-align: left; vertical-align: top; }\n")
                .append("    th { background: #f9fafb; font-weight: 700; }\n")
                .append("    .badge { display: inline-block; padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 700; color: white; }\n")
                .append("    .badge.PASSED { background: var(--ok); }\n")
                .append("    .badge.FAILED { background: var(--fail); }\n")
                .append("    .badge.SKIPPED { background: var(--skip); }\n")
                .append("    .badge.UNDEFINED { background: var(--undef); }\n")
                .append("    .muted { color: var(--muted); }\n")
                .append("    .detail-row { display: none; background: #fbfdff; }\n")
                .append("    .toggle-btn { border: 1px solid var(--border); background: #fff; border-radius: 6px; padding: 4px 8px; cursor: pointer; color: var(--accent); }\n")
                .append("    .step-table th, .step-table td { font-size: 13px; }\n")
                .append("    pre { white-space: pre-wrap; word-break: break-word; background: #111827; color: #f9fafb; padding: 10px; border-radius: 8px; max-height: 220px; overflow: auto; }\n")
                .append("    .screenshot { max-width: 380px; border: 1px solid var(--border); border-radius: 8px; }\n")
                .append("    .footer { margin-top: 14px; color: var(--muted); font-size: 13px; }\n")
                .append("  </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("  <div class=\"container\">\n")
                .append("    <h1>Cucumber BDD Execution Report</h1>\n")
                .append("    <div class=\"subtitle\">Generated from actual Cucumber execution results. Run started: ")
                .append(escapeHtml(runStarted))
                .append(" | Run finished: ")
                .append(escapeHtml(runFinished))
                .append("</div>\n");

        html.append("    <div class=\"section\">\n")
                .append("      <h2>1. Executive Dashboard</h2>\n")
                .append("      <div class=\"grid cards\">\n")
                .append(kpiCard("Total Scenarios", String.valueOf(totalScenarios), ""))
                .append(kpiCard("Passed", String.valueOf(passed), "ok"))
                .append(kpiCard("Failed", String.valueOf(failed), "fail"))
                .append(kpiCard("Skipped", String.valueOf(skipped), "skip"))
                .append(kpiCard("Undefined", String.valueOf(undefined), "undef"))
                .append(kpiCard("Pass %", formatPercent(passPercent), "ok"))
                .append(kpiCard("Fail %", formatPercent(failPercent), "fail"))
                .append(kpiCard("Total Features", String.valueOf(totalFeatures), ""))
                .append(kpiCard("Total Steps", String.valueOf(totalSteps), ""))
                .append(kpiCard("Execution Time", formatDuration(executionTimeMs), ""))
                .append("      </div>\n")
                .append("    </div>\n");

        html.append("    <div class=\"section\">\n")
                .append("      <h2>2. Doughnut/Pie Chart</h2>\n")
                .append("      <div class=\"charts\">\n")
                .append("        <canvas id=\"statusDoughnut\"></canvas>\n")
                .append("      </div>\n")
                .append("    </div>\n");

        html.append("    <div class=\"section\">\n")
                .append("      <h2>3. Execution Summary Bar Chart</h2>\n")
                .append("      <div class=\"charts\">\n")
                .append("        <canvas id=\"summaryBar\"></canvas>\n")
                .append("      </div>\n")
                .append("    </div>\n");

        html.append("    <div class=\"section\">\n")
                .append("      <h2>4. Feature-wise Summary</h2>\n")
                .append("      <table>\n")
                .append("        <thead><tr><th>Feature Name</th><th>Total Scenarios</th><th>Passed</th><th>Failed</th><th>Skipped</th><th>Pass %</th></tr></thead>\n")
                .append("        <tbody>\n");

        for (FeatureSummary feature : featureSummary.values()) {
            int featureSkipped = feature.skipped + feature.undefined;
            html.append("          <tr>")
                    .append("<td>").append(escapeHtml(feature.name)).append("</td>")
                    .append("<td>").append(feature.total).append("</td>")
                    .append("<td class=\"ok\">\n").append(feature.passed).append("</td>")
                    .append("<td class=\"fail\">\n").append(feature.failed).append("</td>")
                    .append("<td class=\"skip\">\n").append(featureSkipped).append("</td>")
                    .append("<td>").append(formatPercent(percentage(feature.passed, feature.total))).append("</td>")
                    .append("</tr>\n");
        }

        html.append("        </tbody>\n")
                .append("      </table>\n")
                .append("    </div>\n");

        html.append("    <div class=\"section\">\n")
                .append("      <h2>5. Scenario-wise Details</h2>\n")
                .append("      <table>\n")
                .append("        <thead><tr><th>Details</th><th>Feature</th><th>Scenario</th><th>Status</th><th>Duration</th><th>Browser</th></tr></thead>\n")
                .append("        <tbody>\n");

        int index = 0;
        for (ScenarioResult scenario : scenarios) {
            index++;
            String detailId = "detail-" + index;
            html.append("          <tr>")
                    .append("<td><button class=\"toggle-btn\" onclick=\"toggleDetails('").append(detailId).append("')\">Expand</button></td>")
                    .append("<td>").append(escapeHtml(scenario.feature)).append("</td>")
                    .append("<td>").append(escapeHtml(scenario.scenario)).append("</td>")
                    .append("<td><span class=\"badge ").append(scenario.status).append("\">")
                    .append(escapeHtml(scenario.status)).append("</span></td>")
                    .append("<td>").append(formatDuration(scenario.durationMs)).append("</td>")
                    .append("<td>").append(escapeHtml(capitalize(normalizeBrowserName(scenario.browser)))).append("</td>")
                    .append("</tr>\n");

            html.append("          <tr id=\"").append(detailId).append("\" class=\"detail-row\"><td colspan=\"6\">\n")
                    .append("            <strong>7. Expandable Step Details</strong>\n")
                    .append("            <table class=\"step-table\">\n")
                    .append("              <thead><tr><th>Step Type</th><th>Step</th><th>Step status</th><th>Step duration</th><th>Error message if failed</th></tr></thead>\n")
                    .append("              <tbody>\n");

            for (StepResult step : scenario.steps) {
                html.append("                <tr>")
                        .append("<td>").append(escapeHtml(step.keyword)).append("</td>")
                        .append("<td>").append(escapeHtml(step.text)).append("</td>")
                        .append("<td><span class=\"badge ").append(step.status).append("\">")
                        .append(escapeHtml(step.status)).append("</span></td>")
                        .append("<td>").append(formatDuration(step.durationMs)).append("</td>")
                        .append("<td>").append(escapeHtml(nonBlank(step.errorMessage, "-"))).append("</td>")
                        .append("</tr>\n");
            }

            if (scenario.steps.isEmpty()) {
                html.append("                <tr><td colspan=\"5\" class=\"muted\">No executable steps captured for this scenario.</td></tr>\n");
            }

            html.append("              </tbody>\n")
                    .append("            </table>\n")
                    .append("          </td></tr>\n");
        }

        html.append("        </tbody>\n")
                .append("      </table>\n")
                .append("    </div>\n");

        html.append("    <div class=\"section\">\n")
                .append("      <h2>6. Failed Test Details</h2>\n");

        if (failedScenarios.isEmpty()) {
            html.append("      <div class=\"muted\">No failed scenarios in this run.</div>\n");
        } else {
            html.append("      <table>\n")
                    .append("        <thead><tr><th>Feature</th><th>Scenario</th><th>Failed Step</th><th>Error Message</th><th>Stack Trace</th><th>Failure Screenshot</th></tr></thead>\n")
                    .append("        <tbody>\n");

            for (ScenarioResult failedScenario : failedScenarios) {
                html.append("          <tr>")
                        .append("<td>").append(escapeHtml(failedScenario.feature)).append("</td>")
                        .append("<td>").append(escapeHtml(failedScenario.scenario)).append("</td>")
                        .append("<td>").append(escapeHtml(nonBlank(failedScenario.failedStep, "Not available"))).append("</td>")
                        .append("<td>").append(escapeHtml(nonBlank(failedScenario.failedErrorMessage, "Not available"))).append("</td>")
                        .append("<td>");

                if (failedScenario.failedStackTrace == null || failedScenario.failedStackTrace.isBlank()) {
                    html.append("Not available");
                } else {
                    html.append("<pre>").append(escapeHtml(failedScenario.failedStackTrace)).append("</pre>");
                }

                html.append("</td><td>");

                if (failedScenario.failureScreenshotBase64 != null && !failedScenario.failureScreenshotBase64.isBlank()) {
                    String mimeType = nonBlank(failedScenario.failureScreenshotMimeType, "image/png");
                    html.append("<img class=\"screenshot\" alt=\"Failure screenshot\" src=\"data:")
                            .append(escapeHtml(mimeType))
                            .append(";base64,")
                            .append(failedScenario.failureScreenshotBase64)
                            .append("\" />");
                } else {
                    html.append("Not available");
                }

                html.append("</td></tr>\n");
            }

            html.append("        </tbody>\n")
                    .append("      </table>\n");
        }

        html.append("    </div>\n");

        html.append("    <div class=\"section\">\n")
                .append("      <h2>8. Browser-wise Summary</h2>\n")
                .append("      <table>\n")
                .append("        <thead><tr><th>Browser</th><th>Total</th><th>Passed</th><th>Failed</th><th>Skipped</th><th>Pass %</th></tr></thead>\n")
                .append("        <tbody>\n");

        for (String browserName : List.of("chrome", "firefox", "edge")) {
            BrowserSummary summary = browserSummary.getOrDefault(browserName, new BrowserSummary(browserName));
            int skippedByBrowser = summary.skipped + summary.undefined;
            html.append("          <tr>")
                    .append("<td>").append(escapeHtml(capitalize(browserName))).append("</td>")
                    .append("<td>").append(summary.total).append("</td>")
                    .append("<td class=\"ok\">\n").append(summary.passed).append("</td>")
                    .append("<td class=\"fail\">\n").append(summary.failed).append("</td>")
                    .append("<td class=\"skip\">\n").append(skippedByBrowser).append("</td>")
                    .append("<td>").append(formatPercent(percentage(summary.passed, summary.total))).append("</td>")
                    .append("</tr>\n");
        }

        html.append("        </tbody>\n")
                .append("      </table>\n")
                .append("    </div>\n");

        html.append("    <div class=\"section\">\n")
                .append("      <h2>9. Selenium Grid Information</h2>\n")
                .append("      <table>\n")
                .append("        <thead><tr><th>Execution mode</th><th>Browser</th><th>Platform/OS</th><th>Grid/node information</th></tr></thead>\n")
                .append("        <tbody><tr>")
                .append("<td>").append(escapeHtml(executionMode)).append("</td>")
                .append("<td>").append(escapeHtml(capitalize(browser))).append("</td>")
                .append("<td>").append(escapeHtml(platform.trim())).append("</td>")
                .append("<td>")
                .append("Grid: ").append(escapeHtml(gridInfo))
                .append("<br>Node: ").append(escapeHtml(nodeInfo))
                .append("</td>")
                .append("</tr></tbody>\n")
                .append("      </table>\n")
                .append("      <div class=\"footer\">Environment: ")
                .append(escapeHtml(nonBlank(System.getProperty("env"), "qa")))
                .append("</div>\n")
                .append("    </div>\n");

        int summarySkipped = skipped + undefined;
        html.append("  </div>\n")
                .append("  <script>\n")
                .append("    const statusData = {")
                .append("passed: ").append(passed).append(",")
                .append("failed: ").append(failed).append(",")
                .append("skipped: ").append(skipped).append(",")
                .append("undefined: ").append(undefined)
                .append("};\n")
                .append("\n")
                .append("    new Chart(document.getElementById('statusDoughnut'), {\n")
                .append("      type: 'doughnut',\n")
                .append("      data: {\n")
                .append("        labels: ['Passed', 'Failed', 'Skipped', 'Undefined'],\n")
                .append("        datasets: [{\n")
                .append("          data: [statusData.passed, statusData.failed, statusData.skipped, statusData.undefined],\n")
                .append("          backgroundColor: ['#22c55e', '#ef4444', '#f59e0b', '#7c3aed'],\n")
                .append("          borderWidth: 1\n")
                .append("        }]\n")
                .append("      },\n")
                .append("      options: { responsive: true, maintainAspectRatio: false }\n")
                .append("    });\n")
                .append("\n")
                .append("    new Chart(document.getElementById('summaryBar'), {\n")
                .append("      type: 'bar',\n")
                .append("      data: {\n")
                .append("        labels: ['Total', 'Passed', 'Failed', 'Skipped'],\n")
                .append("        datasets: [{\n")
                .append("          label: 'Scenario Counts',\n")
                .append("          data: [")
                .append(totalScenarios).append(",")
                .append(passed).append(",")
                .append(failed).append(",")
                .append(summarySkipped)
                .append("],\n")
                .append("          backgroundColor: ['#2563eb', '#22c55e', '#ef4444', '#f59e0b']\n")
                .append("        }]\n")
                .append("      },\n")
                .append("      options: {\n")
                .append("        responsive: true,\n")
                .append("        scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }\n")
                .append("      }\n")
                .append("    });\n")
                .append("\n")
                .append("    function toggleDetails(id) {\n")
                .append("      const row = document.getElementById(id);\n")
                .append("      if (!row) return;\n")
                .append("      row.style.display = row.style.display === 'table-row' ? 'none' : 'table-row';\n")
                .append("    }\n")
                .append("  </script>\n")
                .append("</body>\n")
                .append("</html>\n");

        return html.toString();
    }

    private static long calculateExecutionTimeMillis(List<ScenarioResult> scenarios) {
        if (scenarios.isEmpty()) {
            return 0L;
        }

        Instant minStart = null;
        Instant maxEnd = null;

        for (ScenarioResult scenario : scenarios) {
            if (scenario.executionStart != null && (minStart == null || scenario.executionStart.isBefore(minStart))) {
                minStart = scenario.executionStart;
            }
            if (scenario.executionEnd != null && (maxEnd == null || scenario.executionEnd.isAfter(maxEnd))) {
                maxEnd = scenario.executionEnd;
            }
        }

        if (minStart != null && maxEnd != null && !maxEnd.isBefore(minStart)) {
            return Duration.between(minStart, maxEnd).toMillis();
        }

        long sum = 0L;
        for (ScenarioResult scenario : scenarios) {
            sum += Math.max(scenario.durationMs, 0L);
        }
        return sum;
    }

    private static Map<String, BrowserSummary> initializeBrowserSummary() {
        Map<String, BrowserSummary> map = new LinkedHashMap<>();
        map.put("chrome", new BrowserSummary("chrome"));
        map.put("firefox", new BrowserSummary("firefox"));
        map.put("edge", new BrowserSummary("edge"));
        return map;
    }

    private static String scenarioKey(TestCase testCase) {
        return testCase.getUri() + ":" + testCase.getLine() + ":" + testCase.getName();
    }

    private String resolveFeatureName(URI uri) {
        String uriText = uri.toString();
        if (featureNameByUri.containsKey(uriText)) {
            return featureNameByUri.get(uriText);
        }

        String featureName = readFeatureNameFromResource(uri);
        if (featureName == null || featureName.isBlank()) {
            featureName = fileNameWithoutExtension(uriText);
        }

        featureNameByUri.put(uriText, featureName);
        return featureName;
    }

    private String readFeatureNameFromResource(URI uri) {
        List<String> candidates = new ArrayList<>();

        String path = uri.getPath();
        if (path != null && !path.isBlank()) {
            candidates.add(trimLeadingSlash(path));
            int featureFolderIndex = path.indexOf("Features/");
            if (featureFolderIndex >= 0) {
                candidates.add(path.substring(featureFolderIndex));
            }
        }

        String uriText = uri.toString();
        int schemeIndex = uriText.indexOf(':');
        if (schemeIndex >= 0 && schemeIndex < uriText.length() - 1) {
            candidates.add(trimLeadingSlash(uriText.substring(schemeIndex + 1)));
        }

        for (String candidate : candidates) {
            String normalized = trimLeadingSlash(candidate.replace('\\', '/'));
            try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(normalized)) {
                if (input == null) {
                    continue;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String trimmed = line.trim();
                        if (trimmed.startsWith("Feature:")) {
                            return trimmed.substring("Feature:".length()).trim();
                        }
                    }
                }
            } catch (IOException ignored) {
                // Continue with fallback parsing.
            }
        }

        return null;
    }

    private static String fileNameWithoutExtension(String path) {
        if (path == null || path.isBlank()) {
            return "Unknown Feature";
        }

        String normalized = path.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String fileName = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        return base.replace('-', ' ').replace('_', ' ').trim();
    }

    private static String normalizeStatus(Status status) {
        if (status == null) {
            return "SKIPPED";
        }

        return switch (status) {
            case PASSED -> "PASSED";
            case FAILED, AMBIGUOUS -> "FAILED";
            case UNDEFINED -> "UNDEFINED";
            case SKIPPED, PENDING, UNUSED -> "SKIPPED";
        };
    }

    private static long durationToMillis(Duration duration) {
        return duration == null ? 0L : duration.toMillis();
    }

    private static String extractErrorMessage(Throwable error) {
        if (error == null) {
            return null;
        }

        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return error.getClass().getSimpleName();
        }

        return message;
    }

    private static String extractStackTrace(Throwable error) {
        if (error == null) {
            return null;
        }

        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            error.printStackTrace(printWriter);
        }
        return stringWriter.toString();
    }

    private static String kpiCard(String key, String value, String cssClass) {
        String klass = cssClass == null || cssClass.isBlank() ? "" : " " + cssClass;
        return "        <div class=\"card\"><div class=\"k\">" + escapeHtml(key) + "</div><div class=\"v" + klass + "\">" + escapeHtml(value) + "</div></div>\n";
    }

    private static String formatDuration(long millis) {
        long safeMillis = Math.max(millis, 0L);
        long hours = safeMillis / 3_600_000;
        long minutes = (safeMillis % 3_600_000) / 60_000;
        long seconds = (safeMillis % 60_000) / 1_000;
        long ms = safeMillis % 1_000;
        return String.format(Locale.ROOT, "%02d:%02d:%02d.%03d", hours, minutes, seconds, ms);
    }

    private static double percentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return (numerator * 100.0) / denominator;
    }

    private static String formatPercent(double value) {
        return String.format(Locale.ROOT, "%.2f%%", value);
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String resolveBrowser() {
        return resolveFirstNonBlank(
                System.getProperty("browser"),
                System.getenv("BROWSER"),
                "chrome"
        ).toLowerCase(Locale.ROOT);
    }

    private static String normalizeBrowserName(String browser) {
        String normalized = nonBlank(browser, "chrome").trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("firefox")) {
            return "firefox";
        }
        if (normalized.contains("edge")) {
            return "edge";
        }
        return "chrome";
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        String lowered = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lowered.charAt(0)) + lowered.substring(1);
    }

    private static String resolveExecutionMode() {
        String gridUrl = resolveFirstNonBlank(
                System.getProperty("selenium.grid.url"),
                System.getProperty("grid.url"),
                System.getProperty("remote.url"),
                System.getProperty("webdriver.remote.url"),
                System.getenv("SELENIUM_GRID_URL"),
                null
        );

        return gridUrl == null ? "Local" : "Remote (Grid)";
    }

    private static String resolveFirstNonBlank(String... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return null;
    }

    private static String nonBlank(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    private static String trimLeadingSlash(String value) {
        if (value == null) {
            return null;
        }
        if (value.startsWith("/")) {
            return value.substring(1);
        }
        return value;
    }

    private static final class ScenarioResult {
        private String feature;
        private String scenario;
        private String status = "SKIPPED";
        private String browser = "chrome";
        private long durationMs;
        private Instant executionStart;
        private Instant executionEnd;
        private String failedStep;
        private String failedErrorMessage;
        private String failedStackTrace;
        private String failureScreenshotBase64;
        private String failureScreenshotMimeType;
        private final List<StepResult> steps = new ArrayList<>();
    }

    private static final class StepResult {
        private String keyword;
        private String text;
        private String status;
        private long durationMs;
        private String errorMessage;
        private String stackTrace;
    }

    private static final class FeatureSummary {
        private final String name;
        private int total;
        private int passed;
        private int failed;
        private int skipped;
        private int undefined;

        private FeatureSummary(String name) {
            this.name = name;
        }
    }

    private static final class BrowserSummary {
        private final String browser;
        private int total;
        private int passed;
        private int failed;
        private int skipped;
        private int undefined;

        private BrowserSummary(String browser) {
            this.browser = browser;
        }
    }
}

