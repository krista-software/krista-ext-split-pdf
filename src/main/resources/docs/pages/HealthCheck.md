# Health Check

## Overview

Reports the extension's runtime health: how much JVM heap is currently in use, and whether the
bundled PDF engine is ready. Useful as a periodic probe in a monitoring workflow, and as the first
thing to run when a large split fails unexpectedly.

## Request Details

- **Area**: Setup
- **Type**: QUERY_SYSTEM
- **External API**: none — JVM metrics plus an in-process PDF engine check
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters

This request takes no inputs.

## Output Parameters

| Parameter | Type | Description |
|---|---|---|
| Is Healthy | Switch | `true` when the PDF engine round-trip succeeded. |
| Heap Used MB | Number | JVM heap currently in use by the extension container, in megabytes. |
| Details | Text | Detail of the PDF engine check — the confirmation text, or the failure reason. |

## Example

**Input**: none

**Output**: Is Healthy = `true`, Heap Used MB = `128`, Details =
`PDF engine ready (Apache PDFBox round-trip succeeded).`

## Error Handling

- **Is Healthy = `false`** — the PDF engine could not round-trip a document; **Details** carries the
  reason. Splitting will fail until this is resolved, normally by reinstalling the extension.
- **Heap Used MB climbing toward the container limit** — splitting holds the whole document in
  memory, so very large PDFs need headroom. Check this figure before and after a large split if
  memory is suspected.

## Related Requests

- [Test Connection](pages/TestConnection.md) — the PDF engine check on its own.
- [Split PDF By Page Count](pages/SplitPdfByPageCount.md) — the request whose memory use this helps
  diagnose.
