package com.qa.analysis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * These classes mirror the structure of Cucumber's JSON report format
 * (target/cucumber-reports/cucumber-report.json). Only the fields we
 * actually need are mapped; @JsonIgnoreProperties(ignoreUnknown = true)
 * lets Jackson silently skip everything else (hooks, embeddings, match
 * locations, durations, etc.) without failing to parse.
 */
public class CucumberReport {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        @JsonProperty("name")
        public String name;

        @JsonProperty("tags")
        public List<Tag> tags;

        @JsonProperty("elements")
        public List<Scenario> elements;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Scenario {
        @JsonProperty("name")
        public String name;

        @JsonProperty("tags")
        public List<Tag> tags;

        @JsonProperty("steps")
        public List<Step> steps;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        @JsonProperty("keyword")
        public String keyword;

        @JsonProperty("name")
        public String name;

        @JsonProperty("result")
        public StepResult result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StepResult {
        @JsonProperty("status")
        public String status;

        @JsonProperty("error_message")
        public String errorMessage;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tag {
        @JsonProperty("name")
        public String name;
    }
}
