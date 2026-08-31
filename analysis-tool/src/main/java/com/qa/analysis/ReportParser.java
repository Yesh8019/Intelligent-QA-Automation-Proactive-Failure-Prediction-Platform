package com.qa.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.analysis.model.CucumberReport;
import com.qa.analysis.model.FailureRecord;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the Cucumber JSON report and extracts every failed scenario as a
 * simple FailureRecord, ready to hand off to Claude.
 */
public class ReportParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<FailureRecord> parseFailures(Path jsonReportPath, Path screenshotDir) throws IOException {
        List<FailureRecord> failures = new ArrayList<>();

        CucumberReport.Feature[] features = mapper.readValue(
                jsonReportPath.toFile(), CucumberReport.Feature[].class);

        for (CucumberReport.Feature feature : features) {
            List<String> featureTags = extractTagNames(feature.tags);

            for (CucumberReport.Scenario scenario : feature.elements) {
                CucumberReport.Step failingStep = findFailingStep(scenario);
                if (failingStep == null) {
                    continue; // scenario passed - nothing to analyze
                }

                List<String> tags = new ArrayList<>(featureTags);
                tags.addAll(extractTagNames(scenario.tags));

                String screenshotPath = findScreenshot(screenshotDir, scenario.name);

                failures.add(new FailureRecord(
                        feature.name,
                        scenario.name,
                        tags,
                        failingStep.keyword + failingStep.name,
                        failingStep.result.errorMessage,
                        screenshotPath
                ));
            }
        }

        return failures;
    }

    private CucumberReport.Step findFailingStep(CucumberReport.Scenario scenario) {
        if (scenario.steps == null) {
            return null;
        }
        for (CucumberReport.Step step : scenario.steps) {
            if (step.result != null && "failed".equals(step.result.status)) {
                return step;
            }
        }
        return null;
    }

    private List<String> extractTagNames(List<CucumberReport.Tag> tags) {
        List<String> names = new ArrayList<>();
        if (tags != null) {
            for (CucumberReport.Tag tag : tags) {
                names.add(tag.name);
            }
        }
        return names;
    }

    /**
     * Screenshots are saved by Hooks.java as
     * target/screenshots/<sanitized-scenario-name>.png, using the same
     * sanitization rule (replace anything that isn't a letter/digit/-/_
     * with an underscore). We rebuild that same filename here to find it.
     */
    private String findScreenshot(Path screenshotDir, String scenarioName) {
        String safeName = scenarioName.replaceAll("[^a-zA-Z0-9-_]", "_");
        File candidate = screenshotDir.resolve(safeName + ".png").toFile();
        return candidate.exists() ? candidate.getPath() : null;
    }
}
