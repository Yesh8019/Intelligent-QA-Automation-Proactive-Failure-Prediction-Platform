# analysis-tool

Reads the Cucumber JSON test report produced by the saucedemo-tests project,
sends every failure to Claude for a plain-English explanation and severity
classification, and produces a Go/No-Go release recommendation.

## How it works

1. **`ReportParser`** reads `../target/cucumber-reports/cucumber-report.json`
   and pulls out every failed scenario: which feature/scenario, its tags,
   the failing step, the error message, and the path to its failure
   screenshot (if one was captured).
2. **`ClaudeAnalyzer`** sends ALL failures to Claude in a single request, so
   it can reason about them holistically - consistent severity judgments
   across failures, and one overall release recommendation informed by the
   full picture.
   - **Severity is hybrid**: failures tagged `@criticalPath` (login,
     checkout) get a floor in the prompt - Claude may only classify them as
     High or Critical, never Medium/Low. Everything else is fully up to
     Claude's judgment based on the failure content.
3. **`ReportWriter`** writes the result as both:
   - `../target/analysis-report/analysis-report.json` (structured, for
     future tooling/dashboards)
   - `../target/analysis-report/analysis-report.md` (human-readable,
     grouped by severity with a Go/No-Go banner at the top)

If there are no failures at all, Claude is never called (no point paying for
an API call to say "everything passed") - a simple GO report is written
directly.

## Requirements

- JDK 21
- The **`ANTHROPIC_API_KEY`** environment variable set to a valid Anthropic
  API key (get one at console.anthropic.com → API Keys)

## How to run locally

```bash
# from the analysis-tool/ folder
mvn clean package -DskipTests

# set your key for this terminal session (Windows PowerShell)
$env:ANTHROPIC_API_KEY = "sk-ant-..."

java -jar target/analysis-tool-1.0-SNAPSHOT.jar
```

This assumes `saucedemo-tests` was already run (via `mvn clean test` from
the repo root) and produced `target/cucumber-reports/cucumber-report.json`.

## Optional arguments

```bash
java -jar target/analysis-tool-1.0-SNAPSHOT.jar <path-to-report.json> <path-to-screenshots-dir>
```

Both default to the paths the saucedemo-tests project produces, assuming
this tool runs from its own `analysis-tool/` folder one level below the repo
root.

## A note on the Jackson dependency

This project deliberately does **not** declare its own `jackson-databind`
version. The Anthropic SDK already brings in a compatible Jackson stack
(including `jackson-module-kotlin`) as a transitive dependency - declaring
a second, separate version here caused a runtime `NoSuchMethodError` (the
two versions weren't compatible with each other). We reuse the SDK's own
Jackson for our own JSON parsing instead.
