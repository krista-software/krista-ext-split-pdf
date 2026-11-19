# Supported Requests

Split PDF exposes **3 catalog requests** across **2 areas**. Each links to its detailed page.

## PDF

| Request | Type | Description |
|---|---|---|
| [Split PDF By Page Count](pages/SplitPdfByPageCount.md) | CHANGE_SYSTEM | Split a PDF into consecutive files of a fixed number of pages each; the last file carries the remainder. |

## Setup

| Request | Type | Description |
|---|---|---|
| [Test Connection](pages/TestConnection.md) | QUERY_SYSTEM | Confirm the extension is installed correctly and its PDF engine works. |
| [Health Check](pages/HealthCheck.md) | QUERY_SYSTEM | Report JVM heap usage and PDF engine readiness. |

All three requests are **Ask Agent (MCP) enabled**, so a Krista AI agent can call them directly.

None of them requires credentials or network access — see [Authentication](pages/Authentication.md).
