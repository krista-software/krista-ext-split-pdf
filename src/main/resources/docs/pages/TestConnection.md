# Test Connection

## Overview

Confirms the extension is installed correctly and its PDF engine is working. Because Split PDF calls
no external system, there is no connection to test — this request instead verifies the one thing
that can genuinely be broken at runtime: that Apache PDFBox is present and able to create, write and
read back a document.

## Request Details

- **Area**: Setup
- **Type**: QUERY_SYSTEM
- **External API**: none — no credentials and no network access are used
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters

This request takes no inputs.

## Output Parameters

| Parameter | Type | Description |
|---|---|---|
| Success | Switch | `true` when the PDF engine round-trip succeeded. |
| Message | Text | Detail of the check — the confirmation text on success, the reason on failure. |

## Example

**Input**: none

**Output**: Success = `true`, Message = `PDF engine ready (Apache PDFBox round-trip succeeded).`

## Error Handling

- **`PDF engine unavailable: <reason>`** — the bundled PDF library could not be used. This normally
  means the extension jar is damaged or was repackaged without its dependencies; reinstall it.
- **`PDF engine check failed: expected to read back 1 page but read <n>.`** — the library loaded but
  behaved unexpectedly. Report this with the appliance logs.
- The check never throws: a failure is reported in **Message** so the result is readable in a
  workflow.

The Setup tab's own **Test Connection** button runs the same check and surfaces the failure text
directly in the tab.

## Related Requests

- [Health Check](pages/HealthCheck.md) — the same engine check plus JVM heap usage.
- [Split PDF By Page Count](pages/SplitPdfByPageCount.md) — the request this check protects.
