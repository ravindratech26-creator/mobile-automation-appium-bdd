# Technical Assessment - Mobile UI Automation (Appium) - Submission Report

**Candidate:** Ravindra
**Repository:** https://github.com/ravindratech26-creator/mobile-automation-appium-bdd
**App under test:** Sauce Labs sample app v2.7.1 (Android `.apk`, iOS simulator `.app` / real-device `.ipa`)
**Stack:** Java 21 · Maven · Appium 3 (java-client 9.3.0, Selenium 4.27.0) · Cucumber 7 (BDD) · TestNG · PicoContainer · Allure · Log4j2

---

## 1. Scope delivered

| Required scenario | Implemented as | Tags | Status (Android 13 emulator) |
|---|---|---|---|
| 1. Successful login | `login.feature` - *Successful login with valid credentials* | `@smoke @regression` | ✅ Passed |
| 2. Unsuccessful login | `login.feature` - invalid credentials **+ Scenario Outline** (locked-out user, empty username, empty password, wrong password) | `@smoke` / `@regression` | ✅ Passed (all 5) |
| 3. Logout | `login.feature` - *Logout returns the user to the login screen* | `@smoke @regression` | ✅ Passed |
| 4. Additional flow | `cart.feature` - *Add a product to the cart* (badge, cart contents, price) + *Remove a product* | `@smoke` / `@regression` | ✅ Add passed · Remove dry-run verified |

**Beyond the brief:** parallel execution with a device pool, Allure + Cucumber reports with live-device environment,
retry analyzer, GitHub Actions (Android emulator, iOS simulator, BrowserStack jobs), parameterised Jenkins pipeline,
BrowserStack App Automate integration, and AI QA agents (requirements → test cases → scripts → defects → RTM → Figma checks).
A separate **Playwright framework** is also provided: `[Playwright repo link]`.

---

## 2. "What we expect to see" - evidence map

| Expectation | How it is met | Where |
|---|---|---|
| **Page Object Model** | One class per screen/component extending `BasePage`; pages hold locators + actions only, **no assertions**; navigation methods return the next page (`productsPage().openMenu().logout()`). | `src/main/java/.../pages/` - `BasePage`, `LoginPage`, `ProductsPage`, `CartPage`, `MenuComponent` |
| **Modularisation & separation of concerns** | Framework core in `src/main` (config, driver, pages, utils, listeners) has no test knowledge; BDD glue in `src/test` (steps, hooks, context, runner). Features = behaviour, steps = flow + assertions, pages = UI mechanics, `WaitUtils` = the only place that waits. | `src/main/java/.../{config,driver,pages,utils,listeners}` · `src/test/java/.../{steps,hooks,context,runners}` |
| **Cross-platform (Android vs iOS), clean & scalable** | Every element declares **both** `@AndroidFindBy` and `@iOSXCUITFindBy`; the right one is picked at runtime → no `if (android)` in pages. Dynamic locators use `byPlatform(androidBy, iosBy)`. `DriverFactory` builds `UiAutomator2Options` / `XCUITestOptions` from config; platform chosen with `-Dplatform=android|ios`. Adding a platform/provider = one options method + one properties file. | `BasePage.byPlatform`, `DriverFactory`, `config/android.properties`, `config/ios.properties` |
| **Stable locator strategy** | Priority: accessibility id (`test-*`, identical on Android & iOS) → XPath/class chain **scoped to an id container** → never index/absolute XPath or coordinates. Products located **by name within their card**, not list position. **All locators were captured from real page source** on the emulator - none guessed. | `LoginPage`, `ProductsPage.productButton()`, `CartPage` |
| **Synchronisation** | Explicit waits only: `FluentWait` (500 ms polling, config timeout) ignoring `NoSuchElement`/`StaleElement` (React Native re-renders); implicit wait = 0; **no `Thread.sleep`**. `WaitUtils.until(condition)` polls async UI state (cart badge, REMOVE button, cart list). Android animations disabled. | `utils/WaitUtils`, `BasePage.tap/type/textOf` |
| **Config-driven execution** | Layered properties: `common` → `{platform}` → `{env}` → `{platform}-{env}` → **env var** → **`-D` flag**. Platform, device, OS version, app path, Appium URL, waits, retries, threads, devices, cloud target - all overridable without code changes. Placeholders (`${BROWSERSTACK_APP_ANDROID}`), fail-fast on missing keys, **no secrets in files**. | `config/ConfigReader`, `src/test/resources/config/*.properties` |
| **Failure handling - screenshots** | On failure: screenshot + **page source** attached to Cucumber & Allure reports and saved to `target/screenshots/<scenario>_<timestamp>.png`. Evidence capture can never throw and mask the real failure; the device is always released in `finally`. | `hooks/Hooks`, `utils/ScreenshotUtils` |
| **Failure handling - logging** | Log4j2 console + rolling file; every line tagged with thread + **scenario name** (readable in parallel); intent logged, never values (passwords never logged); `-Dlog.level=DEBUG`. | `src/test/resources/log4j2.xml`, `target/logs/automation.log` |
| **Failure handling - reporting** | Cucumber HTML (single shareable file), Cucumber JSON (CI), rerun file, **Allure** (steps, attachments, retries, categories, **Environment panel filled from the live session**: real device model, OS, udid). | `runners/TestRunner`, `utils/AllureEnvironmentWriter` |
| **Clean, readable, maintainable code** | Small single-purpose classes, intention-revealing names, assertion messages with actual values, constructor DI (PicoContainer) instead of statics, pinned dependency versions (Selenium BOM) for reproducible builds. | whole repo |
| **BDD-style implementation** | Cucumber 7 + Gherkin in business language, `Background`, `Scenario Outline` with examples table, credentials resolved by **role** (`a "standard" user`) - no passwords in features. | `src/test/resources/features/` |
| **Tagging (smoke / regression)** | `@smoke` (critical path), `@regression` (all), feature tags `@login @logout @cart`. Any tag expression at run time: `-Dcucumber.filter.tags="@smoke"`, `"@regression and not @logout"`. Traceability convention `@REQ-<id> @TC-<id>` ready for tracker-driven tests. | feature files, `TestRunner` |
| **Flakiness & reliability notes** | See section 4. | README §8 |

---

## 3. Submission expectations

| # | Expected | Provided |
|---|---|---|
| 1 | GitHub repository link | https://github.com/ravindratech26-creator/mobile-automation-appium-bdd (+ Playwright: `[Playwright repo link]`) |
| 2 | README - prerequisites & setup | README §1-2 |
| 2 | README - how to run mobile tests (Android & iOS) | README §3 (local, iOS, parallel, BrowserStack, GitHub Actions, Jenkins) |
| 2 | README - how to run API tests | README §3 *API tests* - not part of the mobile repo; `[see Playwright repo if it contains API tests]` |
| 2 | README - configuration approach | README §5 |
| 2 | README - structure & key decisions | README §6-7 |
| 2 | README - how and where AI was used | README §9 |
| 3 | Sample report / output | README §4 + `docs/images/` (Allure overview, passed test, failed test, Cucumber HTML, device failure screenshot) |

---

## 4. Flakiness handling & test reliability

1. **Condition-based waits only** - no sleeps, no implicit waits; stale/absent elements retried inside the wait.
2. **Async UI polled, not read once** - three race conditions (cart badge, REMOVE button, cart list) found and fixed at design time.
3. **Stable, scoped locators** - accessibility ids; name-scoped product locators.
4. **Isolation** - a fresh app session per scenario; any scenario runs alone or in any order.
5. **Animations disabled** - taps never land mid-transition; measured **~20-30 % faster** per scenario (51 s → 36-42 s).
6. **Cold-start tolerance** - UiAutomator2 install/launch and adb timeouts moved to config (the 30 s default failed on a fresh emulator).
7. **Visible retries** - `retry.count` (default 1), fresh session per retry, logged as `WARN RETRY n/m` with the reason and shown as a skipped attempt in reports - flaky tests are surfaced, not hidden. `-Dretry.count=0` for strict runs.
8. **Parallel safety** - ThreadLocal driver, per-scenario context, device pool, unique `systemPort`/`wdaLocalPort` per session.
9. **Reproducible builds** - Selenium pinned via BOM after an open version range broke the driver.

---

## 5. Additional integrations (beyond the brief)

| Integration | What it does | Where |
|---|---|---|
| **Parallel execution** | `mvn test -Dthreads=2 -Ddevices=emulator-5554,emulator-5556` - device pool, queueing when threads > devices, unique ports, one Appium server. Verified: two scenarios ran concurrently on two emulators. | `driver/DevicePool`, `pom.xml` (`threads`) |
| **Allure reporting** | Rich report with steps, screenshots, page source, retries, live-device environment. `mvn allure:serve`. | `pom.xml`, `AllureEnvironmentWriter` |
| **GitHub Actions** | Android API 33 emulator job (push/PR → `@smoke`), optional iOS simulator job (macOS Intel runner) and BrowserStack matrix job; reports + evidence uploaded as artifacts. Nightly schedule available (currently paused). | `.github/workflows/mobile-tests.yml` |
| **Jenkins** | Parameterised pipeline (platform, env, tags, threads, devices, app path, retries, agent); toolchain checks; Appium lifecycle; failures → UNSTABLE; JUnit + Allure + archived evidence; Linux/macOS/Windows agents. | `Jenkinsfile` |
| **Cloud - BrowserStack** | `-Denv=browserstack`: cloud capabilities, session named per scenario, pass/fail status pushed to the dashboard, app upload script, credentials only from env vars / CI secrets / Jenkins credentials. | `driver/BrowserStack`, `scripts/browserstack-upload.sh`, `config/*browserstack.properties` |
| **AI QA agents** | Claude Code subagents: requirements-reader (Jira/ADO), test-case-designer, test-script-generator, failure-analyzer, defect-reporter (creates bugs only after confirmation), rtm-builder, figma-validator - connected via MCP. | `.claude/agents/`, `docs/qa-agents.md` |
| **Playwright framework** | `[1-2 lines describing it: language, scope (web/API), reporting]` | `[Playwright repo link]` |

---

## 6. Verification performed

| Check | Result |
|---|---|
| All login scenarios (7) on Android 13 emulator | ✅ 7/7 passed, 24/24 steps |
| `@smoke` via `mvn test` | ✅ 3/3 passed, BUILD SUCCESS |
| Add to cart | ✅ 7/7 steps passed |
| Deliberate failure (to prove evidence + retry) | ✅ screenshot + page source attached, `RETRY 1/1` logged, build failed correctly |
| Parallel run, 2 emulators | ✅ concurrent sessions, unique ports, device released after failure |
| Cucumber dry run, all features | ✅ 9 scenarios / 38 steps defined, 0 undefined |

---

## 7. Challenges & how they were handled

| Problem | Root cause (evidence) | Resolution |
|---|---|---|
| `LocationContext not found` at driver start | java-client's open Selenium range pulled Selenium 4.49 (API removed) | Pinned `selenium-bom` 4.27.0 + java-client 9.3.0 |
| `instrumentation process cannot be initialized within 30000ms` | UiAutomator2 server slow on a cold emulator | Timeouts externalised to config (90 s) |
| App failed on the 2nd parallel device | Failure screenshot = home screen; logcat = app crash on an Android 17 **16 KB page-size** image (2020 React Native build) | Documented device-matrix limitation; use API 33 devices - not a framework defect |
| Latent Windows bug | `Path.of("bs://…")` throws on Windows | Cloud app ids handled separately before any cloud run |
| Hook noise in Allure | `@AfterStep` hook listed after every step | Evidence moved to `@After` (same screen, cleaner report) |

---

## 8. How AI was used

Built with **Claude Code** as a pair-programmer, **step by step** (each layer designed, explained, verified and committed separately - visible in the git history).
- **AI:** proposed architecture and config layering, generated code, captured real locators by driving Appium sessions, ran verification after each step, diagnosed failures from logs/screenshots/logcat, authored CI files and QA agents.
- **Me:** chose the BDD/enterprise scope and step-by-step pace, reviewed every step, supplied the device capabilities and the known-good dependency versions that fixed the classpath issue, decided the integrations and their order, and controlled all git commits.
- **Guardrails:** no guessed locators, no claims without a run, no secrets in the repo, external writes (defects, test-plan pushes) only after explicit confirmation.

---

## 9. Known limitations (transparent)

- **iOS not executed locally** (Windows machine, no Xcode). iOS locators share the Android accessibility ids (low risk); two class-chain locators are unverified. The GitHub Actions iOS job is set up as non-blocking until proven.
- **GitHub Actions, Jenkins and BrowserStack** pipelines are written and statically checked; not yet executed end-to-end.
- **API tests** are not part of the mobile repository.
- One app session per scenario favours isolation over speed (~40 s/scenario locally); parallel execution is the scaling lever.
