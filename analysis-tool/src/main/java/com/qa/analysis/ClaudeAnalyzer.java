package com.qa.analysis;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ContentBlock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.analysis.model.AnalysisResult;
import com.qa.analysis.model.FailureRecord;

import java.util.List;

/**
 * Sends every failure to Claude in a single request, so the model can
 * reason about them holistically (consistent severity judgments across
 * failures, and one release recommendation informed by the full picture)
 * rather than one isolated call per failure.
 *
 * Severity is HYBRID: failures tagged @criticalPath (login, checkout) get
 * an explicit floor in the prompt - Claude may only classify them as High
 * or Critical, never Medium/Low. Everything else is fully up to Claude's
 * judgment based on the failure content.
 */
public class ClaudeAnalyzer {

    // Reads the ANTHROPIC_API_KEY environment variable automatically.
    private final AnthropicClient client = AnthropicOkHttpClient.fromEnv();
    private final ObjectMapper mapper = new ObjectMapper();

    public AnalysisResult analyze(List<FailureRecord> failures) throws Exception {
        String prompt = buildPrompt(failures);

        MessageCreateParams params = MessageCreateParams.builder()
                .maxTokens(4096L)
                .model(Model.CLAUDE_SONNET_4_6)
                .addUserMessage(prompt)
                .build();

        Message message = client.messages().create(params);
        String responseText = extractText(message);

        return mapper.readValue(stripCodeFences(responseText), AnalysisResult.class);
    }

    private String buildPrompt(List<FailureRecord> failures) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a QA release analyst. Below are ").append(failures.size())
                .append(" failed test scenario(s) from an automated E2E test run against an e-commerce site.\n\n")
                .append("For EACH failure, provide:\n")
                .append("- severity: Critical, High, Medium, or Low\n")
                .append("- whatWentWrong: 1-2 plain-English sentences, no jargon\n")
                .append("- whyItHappened: the likely root cause, in plain English\n")
                .append("- whatToFix: a concrete, actionable next step\n\n")
                .append("Then, looking at ALL failures together, give ONE overall release recommendation:\n")
                .append("\"GO\" (safe to release) or \"NO-GO\" (do not release), with reasoning.\n\n")
                .append("IMPORTANT RULE: any failure tagged [CRITICAL-PATH] below covers a core business flow ")
                .append("(login or checkout). For those, you may ONLY choose severity Critical or High - never Medium or Low.\n\n")
                .append("Respond with ONLY valid JSON, no markdown fences, no prose outside the JSON, matching exactly this shape:\n")
                .append("{\n")
                .append("  \"failures\": [\n")
                .append("    {\"scenario\": \"...\", \"severity\": \"...\", \"whatWentWrong\": \"...\", ")
                .append("\"whyItHappened\": \"...\", \"whatToFix\": \"...\"}\n")
                .append("  ],\n")
                .append("  \"overallRecommendation\": \"GO or NO-GO\",\n")
                .append("  \"overallReasoning\": \"...\"\n")
                .append("}\n\n")
                .append("FAILURES:\n\n");

        for (int i = 0; i < failures.size(); i++) {
            FailureRecord f = failures.get(i);
            sb.append("--- Failure ").append(i + 1).append(" ---\n")
                    .append("Feature: ").append(f.featureName).append("\n")
                    .append("Scenario: ").append(f.scenarioName).append("\n")
                    .append("Tags: ").append(String.join(", ", f.tags)).append("\n")
                    .append(f.isCriticalPath() ? "[CRITICAL-PATH]\n" : "")
                    .append("Failing step: ").append(f.failingStep).append("\n")
                    .append("Error message: ").append(truncate(f.errorMessage, 800)).append("\n")
                    .append(f.screenshotPath != null
                            ? "Screenshot saved at: " + f.screenshotPath + "\n" : "")
                    .append("\n");
        }

        return sb.toString();
    }

    private String extractText(Message message) {
        StringBuilder text = new StringBuilder();
        for (ContentBlock block : message.content()) {
            block.text().ifPresent(t -> text.append(t.text()));
        }
        return text.toString();
    }

    /** Claude sometimes wraps JSON in ```json fences despite instructions -
     * strip them defensively so parsing doesn't fail on that alone. */
    private String stripCodeFences(String text) {
        return text.trim()
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("```\\s*$", "");
    }

    private String truncate(String text, int maxLen) {
        if (text == null) {
            return "(no error message captured)";
        }
        return text.length() > maxLen ? text.substring(0, maxLen) + "... [truncated]" : text;
    }
}
