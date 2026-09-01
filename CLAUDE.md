# Intelligent QA Automation & Proactive Failure Prediction Platform

## 1. Project Overview

This platform automatically runs E2E tests, explains failures in plain
English, classifies their severity, and gives a Go/No-Go release
recommendation - reducing the need for engineers to manually read through
test logs.

**Architecture: 3 pieces**

```
[1] Tests run automatically  ->  [2] Jenkins orchestrates  ->  [3] AI analyzes results
```

**Current status:**
- Piece 1 (Selenium + Cucumber test suite): ✅ built, working
- Piece 2 (Jenkins pipeline): ✅ built, working
- Piece 3 (AI analysis tool): ✅ built and tested, but **not yet merged** -
  waiting on an Anthropic API key (see "Current Status / Open Items" below)

## 2. Repo Map

- **Repo root** - Selenium + Cucumber E2E test suite (Java 21, Maven)
  - `pom.xml`, `Jenkinsfile`, `testng.xml`-free (converted to Cucumber)
  - `src/test/java/com/qa/saucedemo/pages/` - page objects (LoginPage,
    InventoryPage, CheckoutPage)
  - `src/test/java/com/qa/saucedemo/stepdefs/` - step definitions + Hooks
    (driver lifecycle, failure screenshots)
  - `src/test/resources/features/` - Login.feature, Cart.feature,
    Checkout.feature
- **`analysis-tool-wip` branch** - the AI analysis tool (separate Maven
  project in `analysis-tool/`). Fully built and tested (parsing + prompt
  building confirmed working against a real report). Not on `main` yet.

## 3. Key Decisions

- **Java 21**, not 24 - the Jenkins server only has Java 21 available.
- **Headless Chrome via "Chrome for Testing"** (a portable, no-installer
  zip) - the Jenkins agent has no OS-level admin access to install Chrome
  normally, and has no display anyway.
- **Cucumber + JUnit 4 runner** (not TestNG) - converted partway through the
  project per request.
- **Hybrid severity model**: scenarios tagged `@criticalPath` (login,
  checkout) get a severity floor of High/Critical; everything else is fully
  judged by the AI based on failure content.
- **3 intentional failures built into the suite on purpose** (one at each
  severity - Critical/High/Low) to give the analysis tool realistic data:
  - Login: broken button locator -> Critical
  - Checkout: missing postal code -> High
  - Cart: wrong badge count after removal -> Low
- **`maven.test.failure.ignore=true`** in the Jenkins build step - test
  failures are expected data, not a broken build; Jenkins should report
  UNSTABLE (yellow), not FAILURE (red), when only our known failures occur.
- **Screenshots captured automatically on failure** (Hooks.java `@After`),
  saved to `target/screenshots/` and embedded in the Cucumber JSON report.

## 4. Workflow / Decision Procedure

```
1. Check for local code changes (uncommitted).

   NO changes ->
     Just report current status. Don't run anything unless explicitly asked.

   YES, changes exist ->
     Run tests locally (via IntelliJ's RunCucumberTest run configuration)
     |-- Local run succeeds
     |     -> ASK: "Local tests passed. OK to push to Git and
     |       trigger a Jenkins build?"
     |     -> Only push + trigger Jenkins if the user confirms
     |
     `-- Local run fails
           -> STOP. Don't push, don't ask.
           -> Explain the failure in plain English
           -> Assign severity (Critical/High/Medium/Low)
           -> Give a Go/No-Go verdict
           -> Generate an HTML report of this analysis

2. Once Jenkins has run (user reports "build done"), check the logs:

   Jenkins passed ->
     Report the result. Done.

   Jenkins failed AND local passed ->
     Compare local vs Jenkins output, explain the likely cause,
     give a fix recommendation, and give a Go/No-Go verdict.
     -> Generate an HTML report of this analysis
```

**HTML report style**: color-coded severity badges (🔴 Critical / 🟠 High /
🟡 Medium / 🟢 Low), clear Go/No-Go banner at the top, one section per
failure with What Went Wrong / Why / What To Fix.

## 5. Current Status / Open Items

- **Anthropic API key**: not yet created. Blocks merging `analysis-tool-wip`
  into `main` and adding the `anthropic-api-key` Jenkins credential.
- **Jenkins credential `anthropic-api-key`**: not yet configured (needed
  before the AI analysis Jenkins stage can run).
- **RESOLVED - intermittent network/Chrome flakiness on Jenkins** (was
  causing varying failures across builds #5, #6, #7 - login page not
  loading, clicks not registering, long hangs). Root cause: Chrome running
  low on shared memory in the constrained CI environment. Fixed by adding
  `--disable-dev-shm-usage` and `--no-sandbox` to Chrome's launch options
  in `Hooks.java`. Confirmed fixed in build #8: exactly the expected
  baseline (9 tests, 3 known failures, 0 unexpected errors), and build
  duration dropped from ~15 minutes back down to ~2.6 minutes.
- **Open policy question - Go/No-Go rule for known/intentional failures**:
  currently informal - past verdicts (e.g. build #8) treated the 3
  intentional planted failures as non-blocking ("app is healthy" = GO),
  but this was an unstated assumption, not an agreed rule. Under strict
  CI/CD practice, any failing test should block release unless there's an
  explicit, documented waiver - "the AI happens to know why" is not a real
  waiver mechanism. Left unresolved for now by request - revisit before
  relying on Go/No-Go verdicts for anything beyond this demo.
- **Jenkins log access**: working. Authenticated via `JENKINS_USER` +
  `JENKINS_API_TOKEN` environment variables (Jenkins personal API token,
  not the account password). Verified working against `localhost:8080`.
- **Fully autonomous polling (trigger + wait + auto-detect completion in
  one turn) was attempted and does NOT work reliably** - the terminal tool
  cannot safely sleep/block for more than roughly 1-2 minutes without the
  connection destabilizing. Current approach: trigger the build via API
  when asked, then wait for the user to say "build done" before fetching
  logs.

## 6. Working Notes

- **Local verification**: always run via IntelliJ's `RunCucumberTest` run
  configuration before pushing. Expected healthy baseline: 9 tests run,
  3 failures (the intentional ones above), 0 unexpected errors.
- **Jenkins access**: reachable at `http://localhost:8080`. Job name:
  `saucedemo-tests`. Auth via Basic auth header built from
  `JENKINS_USER`:`JENKINS_API_TOKEN`, base64-encoded.
- **Triggering a build via API**: requires a CSRF crumb first
  (`GET /crumbIssuer/api/json`), then `POST /job/saucedemo-tests/build`
  with the crumb header set. The response's `Location` header is a queue
  URL - poll `<queue-url>api/json` until it has an `executable.number`
  field, which is the real build number.
- **Fetching results**: `GET /job/saucedemo-tests/<build-number>/api/json`
  for status (`building`, `result`), `GET
  /job/saucedemo-tests/<build-number>/consoleText` for the full log.
- **saucedemo.com test credentials**: `standard_user` / `secret_sauce`
  (valid), `locked_out_user` (blocked), `invalid_user` / `wrong_password`
  (invalid).
