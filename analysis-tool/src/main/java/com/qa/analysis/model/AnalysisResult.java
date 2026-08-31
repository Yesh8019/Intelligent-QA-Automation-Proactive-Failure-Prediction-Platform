package com.qa.analysis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The structured result we ask Claude to return: a severity-classified,
 * plain-English explanation for every failure, plus one holistic
 * Go/No-Go release recommendation based on all of them together.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisResult {

    @JsonProperty("failures")
    public List<FailureAnalysis> failures;

    @JsonProperty("overallRecommendation")
    public String overallRecommendation; // "GO" or "NO-GO"

    @JsonProperty("overallReasoning")
    public String overallReasoning;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FailureAnalysis {
        @JsonProperty("scenario")
        public String scenario;

        @JsonProperty("severity")
        public String severity; // "Critical" | "High" | "Medium" | "Low"

        @JsonProperty("whatWentWrong")
        public String whatWentWrong;

        @JsonProperty("whyItHappened")
        public String whyItHappened;

        @JsonProperty("whatToFix")
        public String whatToFix;
    }
}
