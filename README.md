# Mobile UI Automation - Appium + Cucumber (BDD)

Cross-platform (Android + iOS) mobile UI automation framework for the
[Sauce Labs sample app](https://github.com/saucelabs/sample-app-mobile/releases) (v2.7.1).

**Stack:** Java 21 · Maven · Appium 3 (java-client 9.3.0 / Selenium 4.27.0) · Cucumber 7 · TestNG · PicoContainer · Allure · Log4j2
**CI / cloud:** GitHub Actions · Jenkins · BrowserStack App Automate

| Scenario (assessment) | Feature | Tags |
|---|---|---|
| 1. Successful login | `login.feature` | `@smoke @regression` |
| 2. Unsuccessful login (+ 4 validation cases: locked out, empty username, empty password, wrong password) | `login.feature` | `@smoke` / `@regression` |
| 3. Logout | `login.feature` | `@smoke @regression` |
| 4. Add to cart (+ remove from cart) | `cart.feature` | `@smoke` / `@regression` |

---

## 1. Prerequisites

| Tool | Version used | Notes |
|---|---|---|
| JDK | 21 | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Node.js | 20+ | for Appium |
| Appium | 3.x | `npm i -g appium` |
| UiAutomator2 driver | 7.x | `appium driver install uiautomator2` |
| XCUITest driver (iOS, macOS only) | latest | `appium driver install xcuitest` |
| Android SDK + emulator | API 33 (Android 13) recommended | see *Known limitations* for newer images |
| Xcode + iOS simulator (iOS only) | recent | macOS only |

## 2. Setup

```bash
git clone https://github.com/<you>/mobile-automation-appium-bdd.git
cd mobile-automation-appium-bdd
mvn -B -q test-compile          # downloads dependencies, compiles
```

Get the app (binaries are not committed - see `.gitignore`):
- **Android**: download `Android.SauceLabs.Mobile.Sample.app.2.7.1.apk` from the releases page.
- **iOS simulator**: download `iOS.Simulator.SauceLabs.Mobile.Sample.app.2.7.1.zip` and unzip it.
- **iOS real device / BrowserStack**: `iOS.RealDevice.SauceLabs.Mobile.Sample.app.2.7.1.ipa`.

Point the framework at your app with `-Dapp.path=...` (or env var `APP_PATH`), or edit `app.path` in
`src/test/resources/config/android.properties` / `ios.properties`.

Start a device and Appium:
```bash
emulator -avd <your_API_33_avd>      # or start it from Android Studio > Device Manager
appium                               # separate terminal, default http://127.0.0.1:4723
```

## 3. How to run

### Android (local emulator / device)
```bash
mvn test                                              # everything except @ignore
mvn test -Dcucumber.filter.tags="@smoke"              # smoke pack
mvn test -Dcucumber.filter.tags="@regression"         # full regression
mvn test -Dcucumber.filter.tags="@cart and not @ignore"
mvn test -Dcucumber.filter.name="Successful login"    # a single scenario
mvn test -Dapp.path=apps/SauceLabs.apk -Ddevice.name=emulator-5554 -Dplatform.version=13
```

### iOS (macOS + simulator)
```bash
mvn test -Dplatform=ios -Dapp.path="apps/<unzipped>.app" -Ddevice.name="iPhone 16" -Dplatform.version=18.0
```

### Parallel (one device per thread)
```bash
mvn test -Dthreads=2 -Ddevices=emulator-5554,emulator-5556 -Dcucumber.filter.tags="@smoke"
```
Each scenario borrows a device from a pool and gets a unique UiAutomator2 `systemPort` / iOS `wdaLocalPort`,
so several sessions share one Appium server safely. More threads than devices = scenarios queue, never collide.

### Cloud - BrowserStack App Automate
```bash
export BROWSERSTACK_USERNAME=...  BROWSERSTACK_ACCESS_KEY=...          # never committed
bash scripts/browserstack-upload.sh android apps/SauceLabs.apk        # prints bs://<id>
export BROWSERSTACK_APP_ANDROID=bs://<id>
mvn test -Denv=browserstack -Dcucumber.filter.tags="@smoke"
```
Device / OS for the cloud are in `android-browserstack.properties` / `ios-browserstack.properties`.
Each cloud session is named after the scenario and marked passed/failed on the BrowserStack dashboard.

### CI
- **GitHub Actions** (`.github/workflows/mobile-tests.yml`)
  - push / PR -> `@smoke` on an Android API 33 emulator (blocking)
  - nightly -> `@regression`
  - manual *Run workflow* -> choose tags, optionally the iOS simulator job (`macos-15-intel`, experimental)
    and the BrowserStack job (real Android + iOS devices; needs repo secrets `BROWSERSTACK_USERNAME`,
    `BROWSERSTACK_ACCESS_KEY`)
  - Allure report, Cucumber HTML, screenshots and logs uploaded as artifacts on every run.
- **Jenkins** (`Jenkinsfile`) - parameterised pipeline: `PLATFORM`, `EXEC_ENV` (local/browserstack), `TAGS`,
  `THREADS`, `DEVICES`, `APP_PATH`, `RETRY_COUNT`, `AGENT_LABEL`. Test failures mark the build **UNSTABLE**
  (not FAILED) so reports are always published; JUnit + Allure + archived evidence. Works on Linux/macOS
  and Windows agents. BrowserStack keys come from a Jenkins credential with id `browserstack`.

### API tests
Not part of this submission - the scope delivered is the mobile UI framework. (The layered config,
reporting and CI pipelines are reusable if an API module, e.g. REST Assured, is added later.)

## 4. Reports & evidence

| Output | Location | Open with |
|---|---|---|
| Cucumber HTML (single file) | `target/cucumber-reports/cucumber.html` | any browser |
| Cucumber JSON (CI tools) | `target/cucumber-reports/cucumber.json` | - |
| Allure | `target/allure-results` | `mvn allure:serve` or `mvn allure:report` |
| Failure screenshots | `target/screenshots/<scenario>_<timestamp>.png` | - |
| Logs | `target/logs/automation.log` (per-scenario context) | - |
| Failed scenarios list | `target/cucumber-reports/rerun.txt` | - |

On failure the report gets a **screenshot** and the **page source** (for locator debugging without a rerun).
The Allure *Environment* panel is filled from the **live session** (real device model, OS version, udid),
not from config. Tip: run `mvn clean` before a run you want to screenshot for a report.

Sample output (Android 13 emulator, `@smoke`):
```
3 Scenarios (3 passed)
12 Steps (12 passed)
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
<!-- Add report screenshots to docs/images/ and link them here, e.g. ![Allure overview](docs/images/allure-overview.png) -->

## 5. Configuration approach

Layered properties, later wins - no code changes between local, CI and cloud:
```
config/common.properties              waits, retries, test data, parallel ports
  -> config/{platform}.properties     android / ios capabilities
  -> config/{env}.properties          e.g. browserstack (only when -Denv != local)
  -> config/{platform}-{env}.properties   e.g. ios-browserstack (device/OS on the cloud)
  -> environment variable             explicit.wait.seconds -> EXPLICIT_WAIT_SECONDS
  -> JVM system property              -Dexplicit.wait.seconds=20
```
- Values can reference env vars: `app.path=${BROWSERSTACK_APP_ANDROID}`.
- Missing required keys fail fast with a clear message (e.g. BrowserStack app id not set).
- **Secrets never live in files** - BrowserStack credentials come only from env vars / CI secrets.
- Useful keys: `platform`, `env`, `device.name`, `platform.version`, `app.path`, `explicit.wait.seconds`,
  `retry.count`, `devices`, `threads` (Maven), `log.level`, `skip.device.initialization`.

## 6. Framework structure

```
src/main/java/com/saucelabs/mobile/          FRAMEWORK CORE (no test knowledge)
  config/     ConfigReader (layered), Platform
  driver/     DriverManager (ThreadLocal), DriverFactory (options per platform/target),
              DevicePool (parallel), BrowserStack (cloud caps + session status)
  pages/      BasePage, LoginPage, ProductsPage, CartPage, MenuComponent
  utils/      WaitUtils (only place that waits), ScreenshotUtils, AllureEnvironmentWriter
  listeners/  RetryAnalyzer, RetryTransformer
src/test/java/com/saucelabs/mobile/          TEST LAYER (BDD glue)
  context/    TestContext (per-scenario state, PicoContainer DI, lazy pages)
  steps/      LoginSteps, ProductsSteps, CartSteps
  hooks/      Hooks (session per scenario, failure evidence, cloud status)
  runners/    TestRunner (Cucumber + TestNG, parallel-ready)
src/test/resources/
  features/   login.feature, cart.feature
  config/     layered properties
  log4j2.xml
testng.xml · Jenkinsfile · .github/workflows · scripts/browserstack-upload.sh
.claude/agents · docs/qa-agents.md · qa-artifacts/     AI QA agents (see section 9)
```

## 7. Key decisions (and why)

| Decision | Why |
|---|---|
| **Java + Cucumber + TestNG** | Typical enterprise stack; Gherkin for BDD + tags; TestNG for parallel data provider & listeners. |
| **Core in `src/main`, glue in `src/test`** | Pages/driver/config don't know about tests; could become a shared library. |
| **Page Object Model with dual annotations** | Each field has `@AndroidFindBy` + `@iOSXCUITFindBy`; platform chosen at runtime - no `if (android)` in pages. Dynamic locators use `byPlatform(androidBy, iosBy)`. |
| **Accessibility ids first** | The app exposes identical `test-*` ids on Android and iOS; stable across layout changes. Scoped XPath only relative to an id container; never index-based. |
| **Locators captured from the real app** | Every locator came from page source captured on the emulator - none guessed. |
| **Pages have no assertions; steps have no locators/waits** | Pages are reusable; steps read as business flow; assertion messages show actual values. |
| **PicoContainer `TestContext` with lazy pages** | Step classes share state without statics; pages are created after the driver starts; fresh per scenario. |
| **One app session per scenario** | Full isolation - any scenario can run alone or in any order. Costs time (~40 s/scenario) but removes cross-test coupling. |
| **ThreadLocal driver + device pool + unique ports** | Parallel-safe by design; thread count is a Maven parameter. |
| **Pinned Selenium via BOM** | java-client declares an open Selenium range; without a pin Maven pulled a Selenium that broke `AndroidDriver`. |
| **Failures -> UNSTABLE in Jenkins, blocking Android job in Actions, non-blocking iOS** | Report honestly what is proven vs experimental. |

## 8. Flakiness handling & test reliability

1. **Explicit waits only** - `FluentWait` polling, ignores `NoSuchElement`/`StaleElementReference` (React Native re-renders); implicit wait = 0; no `Thread.sleep` anywhere.
2. **Condition polling for async UI** - cart badge, REMOVE button and cart list update after a tap; `WaitUtils.until(condition)` instead of reading once (3 races fixed before they flaked).
3. **Stable locators** - accessibility ids; products located by *name within their card*, not position.
4. **Isolation** - fresh session per scenario; no scenario depends on another.
5. **Animations disabled** (Android) - taps never land mid-transition; also measured ~20-30 % faster per scenario.
6. **Cold-start tolerance** - UiAutomator2 server install/launch and adb timeouts raised to 90 s via config (the default 30 s failed on a freshly booted emulator).
7. **Retry as a safety net, not a fix** - `retry.count=1` (config), each retry on a fresh session, logged as `WARN RETRY n/m` with the reason and shown as a skipped attempt in reports, so flaky tests stay visible. `-Dretry.count=0` for strict runs.
8. **Evidence on every failure** - screenshot + page source + per-scenario logs; screenshot capture can never throw and hide the real failure; the device is always released in `finally`.
9. **Reproducible builds** - dependency versions pinned.

## 9. How AI was used

The framework was built with **Claude Code (Anthropic)** as a pair-programmer, **step by step** (one layer per
step, each explained, verified and committed separately) - the git history mirrors those steps.

| Area | How AI was used | Human role |
|---|---|---|
| Tool selection & design | Proposed stack, layering, config resolution order, parallel design | Chose BDD/enterprise scope, step-by-step pace, integrations-last order |
| Code generation | Wrote framework classes, features, steps, CI files | Reviewed each step; provided device capabilities and a known-good `pom` |
| Locator discovery | Drove throwaway Appium sessions to capture real page source per screen | - |
| Verification | Compiled, dry-ran and executed scenarios on the emulator after each step; deliberately failed a scenario to prove screenshots/retry | Limited runs to one scenario to save time |
| Debugging | Diagnosed issues from logs/screenshots/logcat (below) | Supplied the Selenium/java-client versions that fixed the classpath issue |
| QA agents | Authored the Claude Code subagents in `.claude/agents/` | Defined which lifecycle stages to cover |

**AI QA agents** (`.claude/agents/`, see `docs/qa-agents.md`): requirements-reader (Jira/ADO), test-case-designer,
test-script-generator, failure-analyzer, defect-reporter (creates bugs only after confirmation), rtm-builder,
figma-validator. They connect to Jira / Azure DevOps / Figma through MCP servers (`.mcp.json.example`) and write
to `qa-artifacts/`, linked by `@REQ-<id>` / `@TC-<id>` tags.

Rules followed with AI: no guessed locators, no claims without a run, no secrets in the repo, every
external write (defects, test-plan pushes) behind explicit confirmation.

## 10. When things didn't go to plan

| Problem | How it showed up | Root cause | Resolution |
|---|---|---|---|
| Classpath error `LocationContext not found` | First session failed at driver creation | java-client's open Selenium range pulled Selenium 4.49 (API removed) | Pinned `selenium-bom` 4.27.0 + java-client 9.3.0 |
| `instrumentation process cannot be initialized within 30000ms` | First session on a cold emulator | UiAutomator2 server slow to install/start | Timeouts moved to config (90 s) |
| Emulator shut down after boot | Background-launched emulator booted, then received a shutdown | Not conclusively identified (likely the launching process/window closing) | Start emulator from Android Studio / own terminal |
| App crashed on 2nd parallel device | Parallel run: 1 pass, 1 "Login screen not displayed" | Failure screenshot showed home screen; logcat showed the app crashing on an Android 17 **16 KB page-size** image - 2020 React Native build incompatible | Use API 33 devices; documented as a device-matrix limitation, not a framework bug |
| Latent Windows bug | Code review | `Path.of("bs://...")` throws on Windows | Cloud app ids handled separately |
| Hook noise in Allure | Report review | `@AfterStep` hook listed after every step | Evidence moved to `@After` (same screen, cleaner report) |

## 11. Known limitations / not yet verified

- **iOS not executed** - developed on Windows (no Xcode). iOS locators use the same accessibility ids as
  Android (low risk); the error-message and cart class-chain locators are unverified. The GitHub Actions
  iOS job is non-blocking until it passes. The 2020 simulator build is x86_64-only -> Intel macOS runner.
- **CI pipelines and BrowserStack** are written and statically checked but were not executed in this session.
- **Device compatibility** - the sample app crashes on Android 17 16 KB-page emulator images; use API 33.
- Per-scenario sessions trade speed for isolation (~40 s/scenario locally); parallelism is the lever for speed.
- Existing scenarios have no `@REQ`/`@TC` tags (they came from the brief, not a tracker).

## 12. Useful commands

```bash
mvn test -Dcucumber.filter.tags="@smoke"                 # smoke
mvn test -Dcucumber.filter.name="Add a product"          # one scenario
mvn test -Dlog.level=DEBUG                               # verbose framework logs
mvn test -Dretry.count=0                                 # strict, no retries
mvn allure:serve                                         # open Allure report
```
