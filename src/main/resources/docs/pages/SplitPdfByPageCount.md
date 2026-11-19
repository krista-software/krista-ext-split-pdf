# Split PDF By Page Count

## Overview

Splits an uploaded PDF into consecutive files of a fixed number of pages each, and returns every
piece as its own PDF file alongside the page range it covers. The last file carries the remainder,
so the document's page count does not have to divide evenly: a 110-page PDF split at 20 pages comes
back as six files of 20, 20, 20, 20, 20 and 10 pages.

The uploaded document is never modified.

## Request Details

- **Area**: PDF
- **Type**: CHANGE_SYSTEM
- **External API**: none — the split runs in-process using Apache PDFBox 3.0.x
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| PDF File | File | Yes | The PDF document to split. Must be a readable, non-password-protected PDF with at least one page. |
| Pages Per Split | Number | Yes | How many pages each output file should contain. Must be a whole number of 1 or more. |
| Output File Name Prefix | Text | No | Base name for the generated files. Defaults to the uploaded file's own name with its extension removed. |

## Output Parameters

| Parameter | Type | Description |
|---|---|---|
| Split Files | List of records | One entry per generated file, in page order. |
| File Count | Number | How many files were produced. |
| Total Pages | Number | Page count of the source document. |
| Success | Switch | `true` when the split completed. |
| Message | Text | Human-readable summary, e.g. `Split 110 page(s) into 6 file(s) of up to 20 page(s) each.` |

Each item in **Split Files** contains:

| Field | Type | Description |
|---|---|---|
| PartNumber | Number | Position of this file in the sequence, starting at 1. |
| FileName | Text | Name of the generated file as stored in Krista. |
| StartPage | Number | First source page in this file (1-based, inclusive). |
| EndPage | Number | Last source page in this file (1-based, inclusive). |
| PageCount | Number | Pages in this file — equals Pages Per Split except, possibly, for the last entry. |
| File | File | The generated PDF, ready to download, attach or pass to another request. |

## File naming

Generated files are named after the page range they contain:

`<base>_p<start>-<end>.pdf`

Splitting `report.pdf` (110 pages) at 20 pages produces:

```
report_p1-20.pdf
report_p21-40.pdf
report_p41-60.pdf
report_p61-80.pdf
report_p81-100.pdf
report_p101-110.pdf
```

The base name comes from **Output File Name Prefix** when supplied, otherwise from the uploaded
file's name. Any character outside `A–Z a–z 0–9 . _ -` is replaced with an underscore, so a prefix
of `Q3 summary` yields `Q3_summary_p1-20.pdf`. If neither a prefix nor a usable source name is
available, the base name `document` is used.

## Example

**Input**: PDF File = `report.pdf` (110 pages), Pages Per Split = `20`

**Output**:

- Split Files = 6 entries —
  `{PartNumber: 1, FileName: report_p1-20.pdf, StartPage: 1, EndPage: 20, PageCount: 20, File: …}`
  through
  `{PartNumber: 6, FileName: report_p101-110.pdf, StartPage: 101, EndPage: 110, PageCount: 10, File: …}`
- File Count = `6`
- Total Pages = `110`
- Success = `true`
- Message = `Split 110 page(s) into 6 file(s) of up to 20 page(s) each.`

## Validation Rules

| Validation | Error Message | Resolution |
|---|---|---|
| Pages Per Split missing | `Pages Per Split is required. Provide the number of pages each output file should contain. Examples: 1, 10, 20.` | Supply a page count. |
| Pages Per Split below 1 | `Pages Per Split must be at least 1, but was 0. …` | Use 1 or more. |
| Pages Per Split not whole | `Pages Per Split must be a whole number of pages, but was 2.5. Examples: 1, 10, 20.` | Round to a whole number. |
| PDF File missing | `PDF File is required. Upload a PDF document to split.` | Attach a document. |
| Uploaded file empty | `PDF File is empty. Upload a PDF document with at least one page.` | Attach a non-empty document. |
| Not a PDF / unreadable | `PDF File is not a valid PDF document and could not be read (…). Upload a readable PDF file.` | Check the file really is a PDF and is not truncated. |
| Password protected | `PDF is password protected. This request cannot split protected documents — remove the password from the PDF and upload it again.` | Remove the protection, then retry. |
| Document has no pages | `PDF File contains no pages. Upload a PDF document with at least one page.` | Use a document with content. |

## Business Rules

1. Pages are taken in source order; no page is dropped, duplicated or reordered.
2. The final file contains the remainder when the page count is not an exact multiple of
   Pages Per Split.
3. An exact multiple produces no empty trailing file — 100 pages at 20 yields exactly 5 files.
4. A document with fewer pages than Pages Per Split yields a single file containing every page.
5. Password-protected documents are rejected outright. This includes documents that open without a
   password but still carry an owner password — the request does not accept a password input.
6. The source document is read only; it is never altered or replaced.

## Error Handling

- **Input errors** — everything in the Validation Rules table is returned as an input-classified
  failure carrying the message shown, so a workflow can branch on it and tell the user what to fix.
- **Password protected** — reported as an input error, not a system failure: the document itself is
  the problem, and the fix is in the caller's hands.
- **System errors** — an unreadable media file or a failure writing a generated chunk is returned as
  a system-classified failure with the underlying message, and logged in full on the appliance.
- **Very large documents** — the whole document is held in memory while it is split. A document of
  many thousands of pages may need more appliance heap; the
  [Health Check](pages/HealthCheck.md) request reports current heap usage.

## Related Requests

- [Test Connection](pages/TestConnection.md) — confirm the PDF engine is working.
- [Health Check](pages/HealthCheck.md) — JVM heap and PDF engine readiness.

## Technical Implementation

Page arithmetic lives in `PdfSplitter`, input rejection in `SplitInputValidator`, and the naming
convention in `SplitFileNamer` — all plain Java classes covered by unit tests. The catalog-request
method itself only validates, delegates, and builds the response; file movement between Krista's
media store and the PDF engine goes through `KristaMediaClient`. Timing and success/failure counts
are recorded under the telemetry prefix `splitpdf.SplitPdfByPageCount`.
