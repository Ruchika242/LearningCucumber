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
import utilities.BrowserContext;

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

    private static final SharedState SHARED = new SharedState();

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
        synchronized (SHARED.lock) {
            if (SHARED.activeRuns == 0) {
                SHARED.scenariosByKey.clear();
                SHARED.featureNameByUri.clear();
                SHARED.activeScenarioByThreadCase.clear();
                SHARED.sequence = 0L;
                SHARED.testRunStartedAt = event.getInstant();
                SHARED.testRunFinishedAt = null;
            }
            SHARED.activeRuns++;
        }
    }

    private void onTestCaseStarted(TestCaseStarted event) {
        synchronized (SHARED.lock) {
            TestCase testCase = event.getTestCase();
            String browser = resolveBrowser();
            String threadCaseKey = threadCaseKey(testCase);
            String key = threadCaseKey + "#" + (++SHARED.sequence);
            ScenarioResult scenario = new ScenarioResult();
            scenario.feature = resolveFeatureName(testCase.getUri());
            scenario.scenario = testCase.getName();
            scenario.browser = browser;
            scenario.executionStart = event.getInstant();
            SHARED.scenariosByKey.put(key, scenario);
            SHARED.activeScenarioByThreadCase.put(threadCaseKey, key);
        }
    }

    private void onTestStepFinished(TestStepFinished event) {
        synchronized (SHARED.lock) {
            ScenarioResult scenario = getActiveScenario(event.getTestCase());
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
            step.text = nonBlank(pickleStep.getStep().getText(), "").trim();
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
        synchronized (SHARED.lock) {
            if (event.getData() == null || event.getData().length == 0) {
                return;
            }

            String mediaType = nonBlank(event.getMediaType(), "").toLowerCase(Locale.ROOT);
            if (!mediaType.startsWith("image/")) {
                return;
            }

            ScenarioResult scenario = getActiveScenario(event.getTestCase());
            if (scenario == null) {
                return;
            }

            scenario.failureScreenshotBase64 = Base64.getEncoder().encodeToString(event.getData());
            scenario.failureScreenshotMimeType = mediaType;
        }
    }

    private void onTestCaseFinished(TestCaseFinished event) {
        synchronized (SHARED.lock) {
            TestCase testCase = event.getTestCase();
            String threadCase = threadCaseKey(testCase);
            String key = SHARED.activeScenarioByThreadCase.remove(threadCase);
            ScenarioResult scenario = key == null ? null : SHARED.scenariosByKey.get(key);
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
        synchronized (SHARED.lock) {
            SHARED.activeRuns = Math.max(0, SHARED.activeRuns - 1);
            if (SHARED.activeRuns == 0) {
                SHARED.testRunFinishedAt = event.getInstant();
                generateReport();
            }
        }
    }

    private void generateReport() {
        List<ScenarioResult> scenarios = new ArrayList<>(SHARED.scenariosByKey.values());

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
        String browser = resolveReportBrowser(browserSummary);
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

        String runStarted = SHARED.testRunStartedAt == null ? "Not available" : TIMESTAMP_FORMATTER.format(SHARED.testRunStartedAt);
        String runFinished = SHARED.testRunFinishedAt == null ? "Not available" : TIMESTAMP_FORMATTER.format(SHARED.testRunFinishedAt);

        String env = nonBlank(System.getProperty("env"), "qa");
        String passBarWidth = String.format(Locale.ROOT, "%.1f", passPercent);
        String failBarWidth = String.format(Locale.ROOT, "%.1f", failPercent);

        StringBuilder html = new StringBuilder(128_000);

        // ── HEAD ────────────────────────────────────────────────────────────
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
            .append("  <meta charset=\"UTF-8\">\n")
            .append("  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n")
            .append("  <title>Test Execution Report</title>\n")
            .append("  <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n")
            .append("  <style>\n")
            // CSS variables
            .append("    :root{--bg:#f1f5f9;--panel:#fff;--ink:#1e293b;--muted:#64748b;")
            .append("--ok:#10b981;--ok-bg:#d1fae5;--fail:#ef4444;--fail-bg:#fee2e2;")
            .append("--skip:#f59e0b;--skip-bg:#fef3c7;--undef:#8b5cf6;--undef-bg:#ede9fe;")
            .append("--accent:#6366f1;--border:#e2e8f0;--topbar:#1e293b;}\n")
            // reset
            .append("    *{box-sizing:border-box;margin:0;padding:0;}\n")
            .append("    body{background:var(--bg);color:var(--ink);font-family:'Segoe UI',system-ui,sans-serif;font-size:14px;}\n")
            // topbar
            .append("    .topbar{background:var(--topbar);color:#f8fafc;padding:14px 28px;display:flex;align-items:center;gap:16px;position:sticky;top:0;z-index:100;box-shadow:0 2px 8px rgba(0,0,0,.25);}\n")
            .append("    .topbar h1{font-size:18px;font-weight:700;letter-spacing:.3px;flex:1;}\n")
            .append("    .topbar .meta{font-size:12px;color:#94a3b8;display:flex;gap:18px;}\n")
            .append("    .env-badge{background:#6366f1;color:#fff;border-radius:4px;padding:2px 8px;font-size:11px;font-weight:700;text-transform:uppercase;}\n")
            // nav
            .append("    nav{background:#fff;border-bottom:1px solid var(--border);padding:0 28px;display:flex;gap:0;overflow-x:auto;}\n")
            .append("    nav a{display:block;padding:11px 16px;font-size:13px;font-weight:500;color:var(--muted);text-decoration:none;border-bottom:2px solid transparent;white-space:nowrap;}\n")
            .append("    nav a:hover{color:var(--accent);border-bottom-color:var(--accent);}\n")
            // main layout
            .append("    .page{max-width:1440px;margin:0 auto;padding:24px 28px;}\n")
            // section card
            .append("    .section{background:var(--panel);border:1px solid var(--border);border-radius:10px;padding:20px 24px;margin-bottom:20px;box-shadow:0 1px 4px rgba(0,0,0,.06);}\n")
            .append("    .section-title{font-size:15px;font-weight:700;color:var(--ink);margin-bottom:16px;display:flex;align-items:center;gap:8px;}\n")
            .append("    .section-title .icon{width:20px;height:20px;border-radius:5px;display:inline-flex;align-items:center;justify-content:center;font-size:11px;font-weight:800;color:#fff;flex-shrink:0;}\n")
            // kpi grid
            .append("    .kpi-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(160px,1fr));gap:12px;}\n")
            .append("    .kpi{border-radius:8px;padding:14px 16px;border-left:4px solid var(--border);background:#fff;border-top:1px solid var(--border);border-right:1px solid var(--border);border-bottom:1px solid var(--border);}\n")
            .append("    .kpi .kpi-label{font-size:11px;color:var(--muted);text-transform:uppercase;letter-spacing:.5px;margin-bottom:6px;font-weight:600;}\n")
            .append("    .kpi .kpi-value{font-size:26px;font-weight:800;}\n")
            .append("    .kpi.ok{border-left-color:var(--ok);}.kpi.ok .kpi-value{color:var(--ok);}\n")
            .append("    .kpi.fail{border-left-color:var(--fail);}.kpi.fail .kpi-value{color:var(--fail);}\n")
            .append("    .kpi.skip{border-left-color:var(--skip);}.kpi.skip .kpi-value{color:var(--skip);}\n")
            .append("    .kpi.undef{border-left-color:var(--undef);}.kpi.undef .kpi-value{color:var(--undef);}\n")
            .append("    .kpi.accent{border-left-color:var(--accent);}.kpi.accent .kpi-value{color:var(--accent);}\n")
            // overview layout: kpis + chart side by side
            .append("    .overview-grid{display:grid;grid-template-columns:1fr 240px;gap:24px;align-items:start;}\n")
            .append("    @media(max-width:900px){.overview-grid{grid-template-columns:1fr;}}\n")
            .append("    .chart-wrap{display:flex;flex-direction:column;align-items:center;}\n")
            .append("    .chart-wrap canvas{max-width:220px;max-height:220px;}\n")
            .append("    .chart-legend{margin-top:10px;display:flex;flex-direction:column;gap:4px;width:100%;}\n")
            .append("    .legend-item{display:flex;align-items:center;gap:6px;font-size:12px;}\n")
            .append("    .legend-dot{width:10px;height:10px;border-radius:50%;flex-shrink:0;}\n")
            // pass rate bar
            .append("    .pass-bar-wrap{margin-top:16px;}\n")
            .append("    .pass-bar-label{font-size:12px;color:var(--muted);margin-bottom:4px;display:flex;justify-content:space-between;}\n")
            .append("    .pass-bar-track{height:8px;background:#e2e8f0;border-radius:4px;overflow:hidden;}\n")
            .append("    .pass-bar-fill{height:100%;border-radius:4px;background:var(--ok);transition:width .4s;}\n")
            // table
            .append("    table{width:100%;border-collapse:collapse;font-size:13px;}\n")
            .append("    thead tr{background:#f8fafc;}\n")
            .append("    th{padding:9px 12px;font-weight:700;font-size:12px;text-transform:uppercase;letter-spacing:.4px;color:var(--muted);text-align:left;border-bottom:2px solid var(--border);}\n")
            .append("    td{padding:9px 12px;border-bottom:1px solid var(--border);vertical-align:middle;}\n")
            .append("    tr.row-pass td{background:#f0fdf4;}\n")
            .append("    tr.row-fail td{background:#fef2f2;}\n")
            .append("    tr.row-skip td{background:#fffbeb;}\n")
            .append("    tbody tr:last-child td{border-bottom:none;}\n")
            // badge
            .append("    .badge{display:inline-flex;align-items:center;padding:3px 9px;border-radius:20px;font-size:11px;font-weight:700;letter-spacing:.3px;}\n")
            .append("    .badge.PASSED{background:var(--ok-bg);color:#065f46;}\n")
            .append("    .badge.FAILED{background:var(--fail-bg);color:#991b1b;}\n")
            .append("    .badge.SKIPPED{background:var(--skip-bg);color:#92400e;}\n")
            .append("    .badge.UNDEFINED{background:var(--undef-bg);color:#5b21b6;}\n")
            // browser badge
            .append("    .br-badge{display:inline-block;padding:2px 7px;border-radius:4px;font-size:11px;font-weight:600;background:#e0e7ff;color:#3730a3;}\n")
            // expand button
            .append("    .expand-btn{background:none;border:1px solid var(--border);border-radius:5px;padding:3px 8px;cursor:pointer;font-size:12px;color:var(--accent);font-weight:600;}\n")
            .append("    .expand-btn:hover{background:#eef2ff;}\n")
            // steps panel
            .append("    .steps-panel{display:none;padding:14px 16px;background:#f8fafc;border-top:1px solid var(--border);}\n")
            .append("    .steps-title{font-size:12px;font-weight:700;color:var(--muted);text-transform:uppercase;letter-spacing:.5px;margin-bottom:10px;}\n")
            .append("    .step-row{display:flex;gap:10px;padding:6px 0;border-bottom:1px solid var(--border);align-items:flex-start;font-size:13px;}\n")
            .append("    .step-row:last-child{border-bottom:none;}\n")
            .append("    .step-kw{font-weight:700;color:var(--accent);min-width:40px;}\n")
            .append("    .step-text{flex:1;}\n")
            .append("    .step-dur{color:var(--muted);white-space:nowrap;font-size:12px;}\n")
            .append("    .step-err{color:var(--fail);font-size:12px;margin-top:3px;}\n")
            // failed card
            .append("    .fail-card{border:1px solid #fca5a5;border-radius:10px;overflow:hidden;margin-bottom:16px;}\n")
            .append("    .fail-card-header{background:#fef2f2;padding:12px 16px;display:flex;align-items:center;gap:10px;flex-wrap:wrap;border-bottom:1px solid #fca5a5;}\n")
            .append("    .fail-card-num{background:#ef4444;color:#fff;border-radius:5px;padding:2px 7px;font-size:11px;font-weight:800;flex-shrink:0;}\n")
            .append("    .fail-card-title{font-weight:700;font-size:14px;flex:1;color:#7f1d1d;}\n")
            .append("    .fail-card-meta{display:flex;gap:8px;align-items:center;flex-wrap:wrap;}\n")
            .append("    .fail-card-body{display:grid;grid-template-columns:1fr 1fr;gap:0;}\n")
            .append("    @media(max-width:900px){.fail-card-body{grid-template-columns:1fr;}}\n")
            .append("    .fail-info{padding:16px;border-right:1px solid #fca5a5;}\n")
            .append("    @media(max-width:900px){.fail-info{border-right:none;border-bottom:1px solid #fca5a5;}}\n")
            .append("    .fail-section-label{font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.5px;color:var(--muted);margin-bottom:5px;margin-top:12px;}\n")
            .append("    .fail-section-label:first-child{margin-top:0;}\n")
            .append("    .fail-step-text{font-size:13px;color:#1e293b;font-weight:600;padding:6px 8px;background:#fff0f0;border-radius:5px;border-left:3px solid #ef4444;}\n")
            .append("    .fail-error-text{font-size:12px;color:#b91c1c;padding:6px 8px;background:#fff5f5;border-radius:5px;word-break:break-word;}\n")
            .append("    .fail-stack-wrap summary{cursor:pointer;color:var(--accent);font-size:12px;font-weight:600;padding:4px 0;user-select:none;}\n")
            .append("    .fail-stack-wrap pre{margin-top:6px;white-space:pre-wrap;word-break:break-all;background:#1e293b;color:#e2e8f0;padding:10px;border-radius:7px;font-size:11px;max-height:200px;overflow:auto;}\n")
            .append("    .fail-screenshot{padding:16px;display:flex;flex-direction:column;align-items:center;justify-content:center;}\n")
            .append("    .fail-screenshot img{max-width:100%;border-radius:7px;border:1px solid #fca5a5;box-shadow:0 2px 8px rgba(239,68,68,.15);cursor:pointer;}\n")
            .append("    .fail-screenshot img:hover{transform:scale(1.01);transition:transform .2s;}\n")
            .append("    .no-screenshot{display:flex;flex-direction:column;align-items:center;justify-content:center;height:100%;min-height:120px;gap:6px;color:var(--muted);}\n")
            .append("    .no-screenshot .ns-icon{font-size:32px;}\n")
            // progress bar in browser table
            .append("    .prog-wrap{display:flex;align-items:center;gap:8px;}\n")
            .append("    .prog-track{flex:1;height:6px;background:#e2e8f0;border-radius:3px;overflow:hidden;min-width:60px;}\n")
            .append("    .prog-fill{height:100%;border-radius:3px;}\n")
            // env grid
            .append("    .env-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:12px;}\n")
            .append("    .env-item{background:#f8fafc;border-radius:7px;padding:12px 14px;border:1px solid var(--border);}\n")
            .append("    .env-item .el{font-size:11px;font-weight:700;text-transform:uppercase;color:var(--muted);margin-bottom:4px;}\n")
            .append("    .env-item .ev{font-size:13px;font-weight:600;color:var(--ink);word-break:break-all;}\n")
            // modal overlay for full-screen screenshot
            .append("    #img-modal{display:none;position:fixed;inset:0;background:rgba(0,0,0,.75);z-index:999;align-items:center;justify-content:center;}\n")
            .append("    #img-modal.open{display:flex;}\n")
            .append("    #img-modal img{max-width:92vw;max-height:92vh;border-radius:8px;box-shadow:0 8px 32px rgba(0,0,0,.5);}\n")
            .append("    #img-modal-close{position:absolute;top:16px;right:22px;color:#fff;font-size:28px;cursor:pointer;font-weight:700;line-height:1;}\n")
            .append("  </style>\n")
            .append("</head>\n<body>\n");

        // ── TOPBAR ───────────────────────────────────────────────────────────
        html.append("<div class=\"topbar\">\n")
            .append("  <h1>&#127381; Test Execution Report</h1>\n")
            .append("  <div class=\"meta\">\n")
            .append("    <span>&#128197; Started: ").append(escapeHtml(runStarted)).append("</span>\n")
            .append("    <span>&#9201; Finished: ").append(escapeHtml(runFinished)).append("</span>\n")
            .append("    <span>&#127760; Platform: ").append(escapeHtml(platform.trim())).append("</span>\n")
            .append("  </div>\n")
            .append("  <span class=\"env-badge\">").append(escapeHtml(env)).append("</span>\n")
            .append("</div>\n");

        // ── NAV ──────────────────────────────────────────────────────────────
        html.append("<nav>\n")
            .append("  <a href=\"#overview\">&#128202; Overview</a>\n")
            .append("  <a href=\"#scenarios\">&#9989; Scenarios (").append(totalScenarios).append(")</a>\n");
        if (!failedScenarios.isEmpty()) {
            html.append("  <a href=\"#failures\" style=\"color:#ef4444;\">&#10060; Failures (").append(failed).append(")</a>\n");
        }
        html.append("  <a href=\"#browsers\">&#127760; Browsers</a>\n")
            .append("  <a href=\"#environment\">&#9881; Environment</a>\n")
            .append("</nav>\n");

        html.append("<div class=\"page\">\n");

        // ── SECTION 1: OVERVIEW ──────────────────────────────────────────────
        html.append("<div class=\"section\" id=\"overview\">\n")
            .append("  <div class=\"section-title\"><span class=\"icon\" style=\"background:#6366f1;\">1</span> Overview</div>\n")
            .append("  <div class=\"overview-grid\">\n")
            .append("    <div>\n")
            .append("      <div class=\"kpi-grid\">\n")
            .append(kpiCard("Total Scenarios", String.valueOf(totalScenarios), "accent"))
            .append(kpiCard("Passed", String.valueOf(passed), "ok"))
            .append(kpiCard("Failed", String.valueOf(failed), "fail"))
            .append(kpiCard("Skipped", String.valueOf(skipped), "skip"))
            .append(kpiCard("Undefined", String.valueOf(undefined), "undef"))
            .append(kpiCard("Total Steps", String.valueOf(totalSteps), "accent"))
            .append(kpiCard("Execution Time", formatDuration(executionTimeMs), "accent"))
            .append(kpiCard("Browsers Tested", "3", "accent"))
            .append("      </div>\n")
            .append("      <div class=\"pass-bar-wrap\">\n")
            .append("        <div class=\"pass-bar-label\"><span>Pass Rate</span><span>").append(formatPercent(passPercent)).append("</span></div>\n")
            .append("        <div class=\"pass-bar-track\"><div class=\"pass-bar-fill\" style=\"width:").append(passBarWidth).append("%;\"></div></div>\n")
            .append("      </div>\n")
            .append("    </div>\n")
            // doughnut chart
            .append("    <div class=\"chart-wrap\">\n")
            .append("      <canvas id=\"statusDoughnut\"></canvas>\n")
            .append("      <div class=\"chart-legend\">\n")
            .append("        <div class=\"legend-item\"><div class=\"legend-dot\" style=\"background:#10b981\"></div><span>Passed — ").append(passed).append("</span></div>\n")
            .append("        <div class=\"legend-item\"><div class=\"legend-dot\" style=\"background:#ef4444\"></div><span>Failed — ").append(failed).append("</span></div>\n")
            .append("        <div class=\"legend-item\"><div class=\"legend-dot\" style=\"background:#f59e0b\"></div><span>Skipped — ").append(skipped).append("</span></div>\n")
            .append("        <div class=\"legend-item\"><div class=\"legend-dot\" style=\"background:#8b5cf6\"></div><span>Undefined — ").append(undefined).append("</span></div>\n")
            .append("      </div>\n")
            .append("    </div>\n")
            .append("  </div>\n")
            .append("</div>\n");

        // ── SECTION 2: SCENARIO DETAILS ──────────────────────────────────────
        html.append("<div class=\"section\" id=\"scenarios\">\n")
            .append("  <div class=\"section-title\"><span class=\"icon\" style=\"background:#10b981;\">2</span> Scenario Results</div>\n")
            .append("  <table>\n")
            .append("    <thead><tr>")
            .append("<th>Steps</th><th>Feature</th><th>Scenario</th><th>Status</th><th>Duration</th><th>Browser</th>")
            .append("</tr></thead>\n")
            .append("    <tbody>\n");

        int scenIdx = 0;
        for (ScenarioResult scenario : scenarios) {
            scenIdx++;
            String sid = "scen-" + scenIdx;
            String rowCls = "PASSED".equals(scenario.status) ? "row-pass" : "FAILED".equals(scenario.status) ? "row-fail" : "row-skip";
            html.append("      <tr class=\"").append(rowCls).append("\">\n")
                .append("        <td><button class=\"expand-btn\" onclick=\"togglePanel('").append(sid).append("')\">&#9654; Steps</button></td>\n")
                .append("        <td>").append(escapeHtml(scenario.feature)).append("</td>\n")
                .append("        <td>").append(escapeHtml(scenario.scenario)).append("</td>\n")
                .append("        <td><span class=\"badge ").append(scenario.status).append("\">").append(escapeHtml(scenario.status)).append("</span></td>\n")
                .append("        <td>").append(formatDuration(scenario.durationMs)).append("</td>\n")
                .append("        <td><span class=\"br-badge\">").append(escapeHtml(capitalize(normalizeBrowserName(scenario.browser)))).append("</span></td>\n")
                .append("      </tr>\n");

            // steps panel row
            html.append("      <tr><td colspan=\"6\" style=\"padding:0;border-bottom:2px solid var(--border);\">\n")
                .append("        <div id=\"").append(sid).append("\" class=\"steps-panel\">\n")
                .append("          <div class=\"steps-title\">Step Execution</div>\n");

            if (scenario.steps.isEmpty()) {
                html.append("          <div style=\"color:var(--muted);font-size:13px;\">No executable steps recorded for this scenario.</div>\n");
            } else {
                for (StepResult step : scenario.steps) {
                    html.append("          <div class=\"step-row\">\n")
                        .append("            <span class=\"step-kw\">").append(escapeHtml(step.keyword)).append("</span>\n")
                        .append("            <span class=\"step-text\">\n")
                        .append("              ").append(escapeHtml(step.text)).append("\n");
                    if (step.errorMessage != null && !step.errorMessage.isBlank()) {
                        html.append("              <div class=\"step-err\">&#9888; ").append(escapeHtml(step.errorMessage)).append("</div>\n");
                    }
                    html.append("            </span>\n")
                        .append("            <span class=\"badge ").append(step.status).append("\">").append(escapeHtml(step.status)).append("</span>\n")
                        .append("            <span class=\"step-dur\">").append(formatDuration(step.durationMs)).append("</span>\n")
                        .append("          </div>\n");
                }
            }
            html.append("        </div>\n      </td></tr>\n");
        }

        html.append("    </tbody>\n  </table>\n</div>\n");

        // ── SECTION 3: FAILURE ANALYSIS ──────────────────────────────────────
        html.append("<div class=\"section\" id=\"failures\">\n");
        if (failedScenarios.isEmpty()) {
            html.append("  <div class=\"section-title\"><span class=\"icon\" style=\"background:#10b981;\">3</span> Failure Analysis</div>\n")
                .append("  <div style=\"text-align:center;padding:28px;color:var(--muted);\">\n")
                .append("    <div style=\"font-size:36px;\">&#127881;</div>\n")
                .append("    <div style=\"font-size:15px;font-weight:600;margin-top:8px;\">All scenarios passed!</div>\n")
                .append("  </div>\n");
        } else {
            html.append("  <div class=\"section-title\"><span class=\"icon\" style=\"background:#ef4444;\">3</span> Failure Analysis — ")
                .append(failed).append(" Failed</div>\n");

            int failIdx = 0;
            for (ScenarioResult fs : failedScenarios) {
                failIdx++;
                html.append("  <div class=\"fail-card\">\n")
                    // header
                    .append("    <div class=\"fail-card-header\">\n")
                    .append("      <span class=\"fail-card-num\">#").append(failIdx).append("</span>\n")
                    .append("      <div class=\"fail-card-title\">")
                    .append(escapeHtml(fs.feature)).append(" &rsaquo; ").append(escapeHtml(fs.scenario))
                    .append("</div>\n")
                    .append("      <div class=\"fail-card-meta\">\n")
                    .append("        <span class=\"badge FAILED\">FAILED</span>\n")
                    .append("        <span class=\"br-badge\">").append(escapeHtml(capitalize(normalizeBrowserName(fs.browser)))).append("</span>\n")
                    .append("        <span style=\"font-size:12px;color:var(--muted);\">&#8987; ").append(formatDuration(fs.durationMs)).append("</span>\n")
                    .append("      </div>\n")
                    .append("    </div>\n")
                    // body
                    .append("    <div class=\"fail-card-body\">\n")
                    // left: error details
                    .append("      <div class=\"fail-info\">\n");

                html.append("        <div class=\"fail-section-label\">Failed Step</div>\n")
                    .append("        <div class=\"fail-step-text\">")
                    .append(escapeHtml(nonBlank(fs.failedStep, "Step information not available"))).append("</div>\n");

                html.append("        <div class=\"fail-section-label\">Error Message</div>\n")
                    .append("        <div class=\"fail-error-text\">")
                    .append(escapeHtml(nonBlank(fs.failedErrorMessage, "Error details not available"))).append("</div>\n");

                if (fs.failedStackTrace != null && !fs.failedStackTrace.isBlank()) {
                    html.append("        <div class=\"fail-section-label\">Stack Trace</div>\n")
                        .append("        <details class=\"fail-stack-wrap\">\n")
                        .append("          <summary>&#128196; View full stack trace</summary>\n")
                        .append("          <pre>").append(escapeHtml(fs.failedStackTrace)).append("</pre>\n")
                        .append("        </details>\n");
                }

                html.append("      </div>\n")
                    // right: screenshot
                    .append("      <div class=\"fail-screenshot\">\n");

                if (fs.failureScreenshotBase64 != null && !fs.failureScreenshotBase64.isBlank()) {
                    String mime = nonBlank(fs.failureScreenshotMimeType, "image/png");
                    String imgSrc = "data:" + escapeHtml(mime) + ";base64," + fs.failureScreenshotBase64;
                    html.append("        <div class=\"fail-section-label\" style=\"align-self:flex-start;width:100%;\">Failure Screenshot</div>\n")
                        .append("        <img src=\"").append(imgSrc).append("\" alt=\"Failure screenshot\" ")
                        .append("onclick=\"openModal(this.src)\" title=\"Click to enlarge\" />\n")
                        .append("        <div style=\"font-size:11px;color:var(--muted);margin-top:6px;\">&#128247; Click to enlarge</div>\n");
                } else {
                    html.append("        <div class=\"no-screenshot\">\n")
                        .append("          <span class=\"ns-icon\">&#128247;</span>\n")
                        .append("          <span style=\"font-weight:600;\">No Screenshot Available</span>\n")
                        .append("          <span style=\"font-size:11px;text-align:center;\">Browser may not have launched<br>or screenshot capture was disabled</span>\n")
                        .append("        </div>\n");
                }

                html.append("      </div>\n    </div>\n  </div>\n");
            }
        }
        html.append("</div>\n");

        // ── SECTION 4: CROSS-BROWSER SUMMARY ────────────────────────────────
        html.append("<div class=\"section\" id=\"browsers\">\n")
            .append("  <div class=\"section-title\"><span class=\"icon\" style=\"background:#f59e0b;\">4</span> Cross-Browser Summary</div>\n")
            .append("  <table>\n")
            .append("    <thead><tr>")
            .append("<th>Browser</th><th>Total</th><th>Passed</th><th>Failed</th><th>Skipped</th><th>Pass Rate</th>")
            .append("</tr></thead>\n")
            .append("    <tbody>\n");

        for (String bname : List.of("chrome", "firefox", "edge")) {
            BrowserSummary bs = browserSummary.getOrDefault(bname, new BrowserSummary(bname));
            int bSkipped = bs.skipped + bs.undefined;
            double bPass = percentage(bs.passed, bs.total);
            String bPassStr = String.format(Locale.ROOT, "%.1f", bPass);
            String progColor = bPass >= 80 ? "#10b981" : bPass >= 50 ? "#f59e0b" : "#ef4444";
            html.append("      <tr>\n")
                .append("        <td><strong>").append(escapeHtml(capitalize(bname))).append("</strong></td>\n")
                .append("        <td>").append(bs.total).append("</td>\n")
                .append("        <td style=\"color:#065f46;font-weight:700;\">").append(bs.passed).append("</td>\n")
                .append("        <td style=\"color:#991b1b;font-weight:700;\">").append(bs.failed).append("</td>\n")
                .append("        <td style=\"color:#92400e;\">").append(bSkipped).append("</td>\n")
                .append("        <td><div class=\"prog-wrap\">")
                .append("<div class=\"prog-track\"><div class=\"prog-fill\" style=\"width:").append(bPassStr).append("%;background:").append(progColor).append(";\"></div></div>")
                .append("<span style=\"font-size:12px;font-weight:700;color:").append(progColor).append(";\">").append(formatPercent(bPass)).append("</span>")
                .append("</div></td>\n")
                .append("      </tr>\n");
        }

        html.append("    </tbody>\n  </table>\n</div>\n");

        // ── SECTION 5: ENVIRONMENT ────────────────────────────────────────────
        html.append("<div class=\"section\" id=\"environment\">\n")
            .append("  <div class=\"section-title\"><span class=\"icon\" style=\"background:#64748b;\">5</span> Run Environment</div>\n")
            .append("  <div class=\"env-grid\">\n")
            .append(envItem("Environment", env))
            .append(envItem("Execution Mode", executionMode))
            .append(envItem("Browser Config", capitalize(browser)))
            .append(envItem("Platform", platform.trim()))
            .append(envItem("Grid URL", gridInfo))
            .append(envItem("Grid Node", nodeInfo))
            .append(envItem("Run Started", runStarted))
            .append(envItem("Run Finished", runFinished))
            .append("  </div>\n</div>\n");

        html.append("</div>\n"); // end .page

        // ── IMAGE MODAL ──────────────────────────────────────────────────────
        html.append("<div id=\"img-modal\" onclick=\"closeModal()\">\n")
            .append("  <span id=\"img-modal-close\" onclick=\"closeModal()\">&times;</span>\n")
            .append("  <img id=\"img-modal-img\" src=\"\" alt=\"Screenshot\" onclick=\"event.stopPropagation()\">\n")
            .append("</div>\n");

        // ── JAVASCRIPT ───────────────────────────────────────────────────────
        html.append("<script>\n")
            .append("  new Chart(document.getElementById('statusDoughnut'),{\n")
            .append("    type:'doughnut',\n")
            .append("    data:{labels:['Passed','Failed','Skipped','Undefined'],\n")
            .append("      datasets:[{data:[").append(passed).append(",").append(failed).append(",")
            .append(skipped).append(",").append(undefined).append("],\n")
            .append("        backgroundColor:['#10b981','#ef4444','#f59e0b','#8b5cf6'],\n")
            .append("        borderWidth:2,borderColor:'#fff',hoverOffset:4}]},\n")
            .append("    options:{responsive:true,maintainAspectRatio:false,cutout:'65%',\n")
            .append("      plugins:{legend:{display:false},tooltip:{callbacks:{label:function(c){return c.label+': '+c.raw+' ('+Math.round(c.raw*100/(c.dataset.data.reduce((a,b)=>a+b,0)||1))+'%)';}}}}}\n")
            .append("  });\n")
            .append("  function togglePanel(id){\n")
            .append("    const p=document.getElementById(id);\n")
            .append("    if(!p)return;\n")
            .append("    p.style.display=p.style.display==='block'?'none':'block';\n")
            .append("    const btn=p.closest('tr').previousElementSibling.querySelector('.expand-btn');\n")
            .append("    if(btn)btn.innerHTML=p.style.display==='block'?'&#9660; Steps':'&#9654; Steps';\n")
            .append("  }\n")
            .append("  function openModal(src){const m=document.getElementById('img-modal');document.getElementById('img-modal-img').src=src;m.classList.add('open');}\n")
            .append("  function closeModal(){document.getElementById('img-modal').classList.remove('open');}\n")
            .append("  document.addEventListener('keydown',e=>{if(e.key==='Escape')closeModal();});\n")
            .append("</script>\n")
            .append("</body>\n</html>\n");

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

    private static String threadCaseKey(TestCase testCase) {
        return testCase.getUri() + ":" + testCase.getLine() + ":" + testCase.getName() + ":" + Thread.currentThread().threadId();
    }

    private static ScenarioResult getActiveScenario(TestCase testCase) {
        String key = SHARED.activeScenarioByThreadCase.get(threadCaseKey(testCase));
        if (key == null) {
            return null;
        }
        return SHARED.scenariosByKey.get(key);
    }

    private String resolveFeatureName(URI uri) {
        String uriText = uri.toString();
        if (SHARED.featureNameByUri.containsKey(uriText)) {
            return SHARED.featureNameByUri.get(uriText);
        }

        String featureName = readFeatureNameFromResource(uri);
        if (featureName == null || featureName.isBlank()) {
            featureName = fileNameWithoutExtension(uriText);
        }

        SHARED.featureNameByUri.put(uriText, featureName);
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
        return "        <div class=\"kpi" + klass + "\"><div class=\"kpi-label\">" + escapeHtml(key) + "</div>"
                + "<div class=\"kpi-value\">" + escapeHtml(value) + "</div></div>\n";
    }

    private static String envItem(String label, String value) {
        return "    <div class=\"env-item\"><div class=\"el\">" + escapeHtml(label) + "</div>"
                + "<div class=\"ev\">" + escapeHtml(nonBlank(value, "—")) + "</div></div>\n";
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
        try {
            return BrowserContext.getBrowser().toLowerCase(Locale.ROOT);
        } catch (Exception ignored) {
            return normalizeBrowserName(resolveFirstNonBlank(
                    System.getProperty("browser"),
                    System.getenv("BROWSER"),
                    "chrome"
            ));
        }
    }

    private static String resolveReportBrowser(Map<String, BrowserSummary> browserSummary) {
        int activeBrowsers = 0;
        String lastSeen = "chrome";

        for (String browserName : List.of("chrome", "firefox", "edge")) {
            BrowserSummary summary = browserSummary.get(browserName);
            if (summary != null && summary.total > 0) {
                activeBrowsers++;
                lastSeen = browserName;
            }
        }

        return activeBrowsers > 1 ? "multiple" : lastSeen;
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
        if (values.length == 0) {
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

    private static final class SharedState {
        private final Object lock = new Object();
        private final Map<String, ScenarioResult> scenariosByKey = new LinkedHashMap<>();
        private final Map<String, String> featureNameByUri = new LinkedHashMap<>();
        private final Map<String, String> activeScenarioByThreadCase = new LinkedHashMap<>();
        private Instant testRunStartedAt;
        private Instant testRunFinishedAt;
        private int activeRuns;
        private long sequence;
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
        private int total;
        private int passed;
        private int failed;
        private int skipped;
        private int undefined;

        private BrowserSummary(@SuppressWarnings("unused") String browser) {
        }
    }
}

