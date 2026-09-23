# QA agents

Claude Code subagents in `.claude/agents/` that cover the test lifecycle around this framework.
They follow the existing framework conventions (page objects, explicit waits, tags) and write to `qa-artifacts/`.

## The flow
```
Jira / Azure DevOps ──► requirements-reader ──► qa-artifacts/requirements/<ID>.md
                                                   │
                                                   ▼
                         test-case-designer ──► qa-artifacts/test-cases/<ID>.md + .csv
                                                   │  (Automate)
                                                   ▼
                      test-script-generator ──► features (@REQ/@TC tags) + steps + pages
                                                   │  mvn test / CI
                                                   ▼
                  failed run ──► failure-analyzer ──► test fix  (test / env / flaky)
                                      │ product bug
                                      ▼
                             defect-reporter ──► draft ──(you confirm)──► Jira / ADO bug
Figma ──► figma-validator ──► qa-artifacts/figma/*.md ──► likely defects ──► defect-reporter
All of the above ──► rtm-builder ──► qa-artifacts/rtm/RTM.md + RTM.csv (coverage gaps)
```

| Agent | Reads | Writes | External writes |
|---|---|---|---|
| requirements-reader | Jira / ADO | `qa-artifacts/requirements` | none (read-only) |
| test-case-designer | requirements, features | `qa-artifacts/test-cases` | only on request + confirmation (Test Plans / Xray) |
| test-script-generator | test cases, framework code | features, steps, pages, TestContext | none |
| failure-analyzer | reports, screenshots, logs | proposed fix (applies only on request) | none |
| defect-reporter | reports, screenshots, logs, trackers | `qa-artifacts/defects` | **only after explicit "yes, create"** |
| rtm-builder | all of the above | `qa-artifacts/rtm` | none (read-only) |
| figma-validator | Figma, live app | `qa-artifacts/figma` | none |

## How to use
Open Claude Code in this folder and ask in plain language, naming the agent, e.g.
- "Use requirements-reader to pull SL-12 and SL-13 from Jira"
- "Use test-case-designer for SL-12"
- "Use test-script-generator to automate the P1 cases of SL-12"
- "Use failure-analyzer on the last run", then "use defect-reporter for the product bugs"
- "Use rtm-builder" / "Use figma-validator for the Products screen: <figma link>"

## Traceability convention
- Test case id: `TC-<REQ-ID>-<NN>` (stable, never renumbered).
- Every automated scenario carries `@REQ-<REQ-ID>` and `@TC-<TC-ID>` next to its suite tags.
- Run one requirement's tests: `mvn test -Dcucumber.filter.tags="@REQ-SL-12"`.

## Connecting Jira / Azure DevOps / Figma (MCP)
1. Copy `.mcp.json.example` to `.mcp.json` (project scope) and adjust:
   - **Atlassian (Jira)** - Atlassian's remote MCP server; you sign in via OAuth in the browser on first use.
   - **Azure DevOps** - Microsoft's `@azure-devops/mcp` server via `npx`; replace `YOUR_ADO_ORGANIZATION`
     and sign in when prompted (needs Node.js).
   - **Figma** - Figma desktop app -> Preferences -> enable the Dev Mode MCP server (local endpoint);
     alternatively Figma's remote MCP server.
2. Restart Claude Code in this folder and approve the project MCP servers when asked.
3. Check the connections with `/mcp` in an interactive `claude` terminal.
Keep `.mcp.json` free of tokens; servers that need keys should read them from environment variables.

## Guardrails built into the agents
- Tracker/design systems are read-only except defect creation and test-case push, which always show a
  draft and wait for explicit confirmation.
- Locators are never guessed: taken from real page source or marked `TODO(locator)`.
- Verification is cheap-first: compile -> dry run -> one scenario.
- The user commits and pushes; agents only give git commands.
