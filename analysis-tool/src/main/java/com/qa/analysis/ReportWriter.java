package com.qa.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.qa.analysis.model.AnalysisResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReportWriter {

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public void writeJson(AnalysisResult result, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        mapper.writeValue(outputPath.toFile(), result);
    }

    public void writeMarkdown(AnalysisResult result, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());

        StringBuilder md = new StringBuilder();
        md.append("# QA Failure Analysis Report\n\n");

        String badge = "GO".equalsIgnoreCase(result.overallRecommendation) ? "✅ GO" : "🛑 NO-GO";
        md.append("## Release Recommendation: ").append(badge).append("\n\n");
        md.append(result.overallReasoning).append("\n\n");

        md.append("## Failures by Severity\n\n");
        for (String severity : new String[]{"Critical", "High", "Medium", "Low"}) {
            var atThisSeverity = result.failures.stream()
                    .filter(f -> severity.equalsIgnoreCase(f.severity))
                    .toList();
            if (atThisSeverity.isEmpty()) {
                continue;
            }

            md.append("### ").append(severityIcon(severity)).append(" ").append(severity).append("\n\n");
            for (var f : atThisSeverity) {
                md.append("**").append(f.scenario).append("**\n\n");
                md.append("- **What went wrong:** ").append(f.whatWentWrong).append("\n");
                md.append("- **Why it happened:** ").append(f.whyItHappened).append("\n");
                md.append("- **What to fix:** ").append(f.whatToFix).append("\n\n");
            }
        }

        Files.writeString(outputPath, md.toString());
    }

    /** A simple "no failures at all" report - skips calling Claude entirely
     * since there's nothing to explain, saving an unnecessary API call. */
    public void writeAllPassedMarkdown(Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        String md = "# QA Failure Analysis Report\n\n"
                + "## Release Recommendation: ✅ GO\n\n"
                + "All test scenarios passed. No failures to analyze.\n";
        Files.writeString(outputPath, md);
    }

    private String severityIcon(String severity) {
        return switch (severity) {
            case "Critical" -> "🔴";
            case "High" -> "🟠";
            case "Medium" -> "🟡";
            case "Low" -> "🟢";
            default -> "⚪";
        };
    }
}
