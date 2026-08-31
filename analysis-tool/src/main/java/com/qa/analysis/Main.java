package com.qa.analysis;

import com.qa.analysis.model.AnalysisResult;
import com.qa.analysis.model.FailureRecord;

import java.nio.file.Path;
import java.util.List;

/**
 * Entry point. Expects the ANTHROPIC_API_KEY environment variable to be set.
 *
 * Usage: java -jar analysis-tool.jar [cucumber-report.json path] [screenshots dir]
 * Both arguments are optional - default paths assume this tool runs from
 * the analysis-tool/ folder, sitting next to the saucedemo-tests project's
 * target/ output.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Path reportPath = Path.of(args.length > 0 ? args[0]
                : "../target/cucumber-reports/cucumber-report.json");
        Path screenshotDir = Path.of(args.length > 1 ? args[1]
                : "../target/screenshots");
        Path jsonOutput = Path.of("../target/analysis-report/analysis-report.json");
        Path markdownOutput = Path.of("../target/analysis-report/analysis-report.md");

        System.out.println("Reading report: " + reportPath.toAbsolutePath());

        ReportParser parser = new ReportParser();
        List<FailureRecord> failures = parser.parseFailures(reportPath, screenshotDir);

        ReportWriter writer = new ReportWriter();

        if (failures.isEmpty()) {
            System.out.println("No failures found - skipping Claude call.");
            writer.writeAllPassedMarkdown(markdownOutput);
            System.out.println("Report written to " + markdownOutput.toAbsolutePath());
            return;
        }

        System.out.println("Found " + failures.size() + " failure(s). Sending to Claude for analysis...");

        ClaudeAnalyzer analyzer = new ClaudeAnalyzer();
        AnalysisResult result = analyzer.analyze(failures);

        writer.writeJson(result, jsonOutput);
        writer.writeMarkdown(result, markdownOutput);

        System.out.println("Overall recommendation: " + result.overallRecommendation);
        System.out.println("Reports written to:");
        System.out.println("  " + jsonOutput.toAbsolutePath());
        System.out.println("  " + markdownOutput.toAbsolutePath());
    }
}
