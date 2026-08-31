package com.qa.analysis.model;

import java.util.List;

/**
 * Our own simplified representation of a single failed scenario, built by
 * ReportParser from the raw Cucumber JSON. This is what gets sent to Claude.
 */
public class FailureRecord {

    public final String featureName;
    public final String scenarioName;
    public final List<String> tags;
    public final String failingStep;
    public final String errorMessage;
    public final String screenshotPath; // null if no screenshot was captured

    public FailureRecord(String featureName, String scenarioName, List<String> tags,
                          String failingStep, String errorMessage, String screenshotPath) {
        this.featureName = featureName;
        this.scenarioName = scenarioName;
        this.tags = tags;
        this.failingStep = failingStep;
        this.errorMessage = errorMessage;
        this.screenshotPath = screenshotPath;
    }

    /** Known critical-path rule: tests tagged @criticalPath cover core
     * business flows (login, checkout). We don't let the AI freely
     * downgrade these to Medium/Low - only High or Critical are allowed. */
    public boolean isCriticalPath() {
        return tags.contains("@criticalPath");
    }
}
