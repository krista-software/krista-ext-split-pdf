# Overview

Split PDF breaks a PDF document into several smaller PDF documents, cut at a page count you choose.
Upload one PDF, say how many pages each piece should contain, and the extension returns every piece
as its own downloadable file together with the page range it covers.

All of the work happens inside the extension on the Krista appliance using the bundled Apache PDFBox
library. Nothing is sent to an outside service, and no credentials, API keys or sign-in are needed.

## Use cases

- **Break a long report into reviewable chunks** — split a 110-page report at 20 pages and route the
  six resulting files to six different reviewers.
- **Keep attachments under a size or page limit** — split a large scanned document before sending it
  on to a system that rejects oversized files.
- **Feed documents page-group by page-group** — split at 1 page to get one file per page, then loop
  over the result in a workflow.

## What you can do

The extension exposes **3 catalog requests** across **2 areas** — see
[Supported Requests](pages/supported-requests.md) for the full list, or the sidebar for each
request's detailed page.

| Area | Request | What it does |
|---|---|---|
| PDF | [Split PDF By Page Count](pages/SplitPdfByPageCount.md) | Split a PDF into consecutive fixed-size page chunks |
| Setup | [Test Connection](pages/TestConnection.md) | Confirm the extension and its PDF engine are working |
| Setup | [Health Check](pages/HealthCheck.md) | Report JVM health and PDF engine readiness |

## How the split works

Pages are taken in order and grouped into consecutive chunks. The final chunk carries whatever is
left over, so the page count never has to divide evenly:

| Source document | Pages Per Split | Result |
|---|---|---|
| 110 pages | 20 | 6 files — 20, 20, 20, 20, 20, 10 pages |
| 100 pages | 20 | 5 files — 20 pages each, no empty trailing file |
| 7 pages | 20 | 1 file containing all 7 pages |
| 4 pages | 1 | 4 files — one page each |

## Before you start

1. No credentials are required — see [Authentication](pages/Authentication.md).
2. Add the extension as an invoker; there is nothing to fill in on the Setup tab — see
   [Configuration](pages/ExtensionConfiguration.md).
3. Run **Test Connection** (Setup area) to confirm the extension installed correctly.
