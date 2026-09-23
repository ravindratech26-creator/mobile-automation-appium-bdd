# QA artefacts

Generated and maintained by the agents in `.claude/agents/` (see `docs/qa-agents.md`).

| Folder | Written by | Content |
|---|---|---|
| `requirements/` | requirements-reader | One `<ID>.md` per Jira/ADO item + `index.md` |
| `test-cases/` | test-case-designer | `<REQ-ID>.md` (readable) + `<REQ-ID>.csv` (Azure Test Plans / Xray import) |
| `defects/` | defect-reporter | `DRAFT-*.md` until confirmed, then `<KEY>.md` |
| `rtm/` | rtm-builder | `RTM.md` + `RTM.csv` |
| `figma/` | figma-validator | `<platform>-<screen>.md` design comparison reports |

IDs link everything: requirement `SL-12` -> test case `TC-SL-12-01` -> scenario tags `@REQ-SL-12 @TC-SL-12-01`.
