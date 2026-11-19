# Implementation Plan — Split PDF

## Summary

| | |
|---|---|
| **Extension title** | Split PDF |
| **Module / slug** | `split-pdf` |
| **Version** | 1.0.0 |
| **Java package** | `app.krista.extensions.essentials.documents.splitpdf` |
| **Gradle group** | `app.krista.extensions.Essentials.Documents` |
| **Ecosystem / Domain** | Essentials / Documents |
| **Auth pattern** | **NONE** |
| **krista-apis** | 1.0.126 (≥ 1.0.125-sp1 required because one request sets `tool = true`) |
| **Third-party dependency** | Apache PDFBox 3.0.x (`org.apache.pdfbox:pdfbox`), Apache 2.0 — your choice |

**What it does.** One catalog request takes an uploaded PDF and a page count, cuts the document into
consecutive chunks of that size, and returns every chunk as its own downloadable PDF with the page
range it covers. A 110-page PDF at 20 pages per split returns 6 files — five of 20 pages, a final one
of 10. All processing is in-process on the appliance; nothing leaves the machine.

---

## Area: PDF

Placed in the domain's **existing** `PDF` area (rather than a new "Split PDF" area) so the request
sits alongside the other document operations and doesn't cause area drift.

| Request | Type | Inputs | Output | External endpoint | Reused or new |
|---|---|---|---|---|---|
| **Split PDF By Page Count** | `CHANGE_SYSTEM`, `tool = true` | `PDF File` (File, **required**)<br>`Pages Per Split` (Number, **required**)<br>`Output File Name Prefix` (Text, optional) | `Split Files` — `[ { Part Number: Number, File Name: Text, Start Page: Number, End Page: Number, Page Count: Number, File: File } ]`<br>`File Count` (Number)<br>`Total Pages` (Number)<br>`Success` (Boolean)<br>`Message` (Text) | none — in-process PDFBox | **NEW** |

### Behaviour decided by your answers

- **Output shape** — a list of file entries, each carrying the PDF plus its part number, file name
  and page range (your chosen option).
- **File naming** — page range in the name: `report_p1-20.pdf`, `report_p21-40.pdf`, …
  `Output File Name Prefix` replaces the `report` base when supplied; otherwise the source file's
  base name is used. Names are sanitized before upload.
- **Password-protected PDFs** — rejected immediately with a clear `PDF is password protected`
  message. No password input field is added.

### Behaviour decided by the requirement (not open questions)

- Final chunk carries the remainder (110 @ 20 → 20,20,20,20,20,10).
- A source shorter than the split size returns a single file containing every page.
- An exact multiple produces no empty trailing part (100 @ 20 → exactly 5 files).
- `Pages Per Split` ≤ 0, null, or non-integer is a validation failure with an actionable message —
  not a silent default.

### Why the output is a typed composite list and **not** an `@Entity`

The builder's default is an `@Entity` per domain object, so this deviation is called out
deliberately. Each split result must carry an actual **file** alongside its metadata, and the
framework reference (`krista-extension-scaffolding/references/field-types.md`) documents a `File`
member only inside the composite object DSL — `"{ key: Text, value: Text, file: File }"` and its list
form — never as a field inside an `@Entity` class. Inventing a `@Field.File` entity field would mean
guessing at framework behaviour the skills do not document, which the rules forbid.

The concern behind the entity rule is still satisfied: every member is explicitly typed and visible
in Krista, and nothing is returned as a raw `FreeForm`/`Map` passthrough. Worth noting that the
domain's three existing split requests all return a bare untyped `List` — this output is strictly
more structured than what's published today.

---

## Area: Setup

No credentials exist to test, so these verify the extension itself rather than a connection.

| Request | Type | Inputs | Output | External endpoint | Reused or new |
|---|---|---|---|---|---|
| **Test Connection** | `QUERY_SYSTEM`, `tool = true` | — | `Success` (Boolean), `Message` (Text) | none — confirms the PDF engine loads and round-trips a one-page document | **NEW** |
| **Health Check** | `CHANGE_SYSTEM`, `tool = true` | — | `Is Healthy` (Boolean), `Heap Used MB` (Number), `Details` (Text) | none — JVM heap + the same engine probe | **NEW** |

---

## Authentication — none

You selected **none**, and the material agrees: this extension calls no external system, so there is
nothing to authenticate against. Concretely:

- **No secured connection fields**, no token/key/username/password on the Setup tab.
- No `RequestAuthenticator`, no OAuth callback resource, no JAX-RS application, no `AttributeStore`.
- `authPattern` is recorded as `NONE` in `implementation-plan.json`.
- Test Connection is kept anyway, repurposed as a self-check of the bundled PDF engine — operators
  expect the button to exist, and it gives a cheap way to confirm the extension deployed correctly.

---

## Capabilities

| Capability | Enabled | Why |
|---|---|---|
| Binary files | **Yes** | `@Field.File` input and File members in the output; uses `KristaMediaClient` + `FileRepository` to bridge `app.krista.model.base.File` ↔ bytes |
| MCP (`tool = true`) | **Yes** | All three requests are agent-callable; splitting a document is a natural agent action |
| Entity search | No | Nothing persistent or searchable — the extension holds no queryable objects |
| Webhooks / `WAIT_FOR_EVENT` | No | No external system pushes anything; the operation is synchronous |
| Solutions tab | No | Out of scope for a single-request utility; not requested |

---

## Domain reuse

`domain-catalog.json` was consulted: **Essentials/Documents exists** with areas
`Document Metadata, Files, Operations, Document Management, PDF`, **23 published requests and zero
entities**.

Three published requests overlapped with this build, and you reviewed each one:

| Existing item | Your decision | Effect on this plan |
|---|---|---|
| **Split PDF Into Two Equal Part** (`localDomainRequest_d02e13c0-…`) | **avoid** | Excluded entirely — not reused, not modelled on, not rebuilt |
| **Split PDF into Pages** (`localDomainRequest_ac1eb24f-…`) | **avoid** | Excluded entirely |
| **Split PDF Based On Condition** (`localDomainRequest_c81cf245-…`) | **avoid** | Excluded entirely |

**Nothing is reused; one item is genuinely new.** `Split PDF By Page Count` delivers fixed-size
N-page chunking, which none of the three avoided requests performs — "Into Two Equal Part" always
yields two parts, "into Pages" yields one file per page, and "Based On Condition" splits on a
condition. It is therefore planned as net new rather than as a variant of any avoided item.

> **Assumption worth confirming at approval.** Marking all three "avoid" removes those three
> capabilities from this extension. It does **not** remove the capability you originally asked for,
> which is why `Split PDF By Page Count` remains in the plan — if your intent was that this build
> should produce no splitting request at all, reject the plan and say so, because there would then be
> nothing left to build.

Naming follows the domain's established vendor-agnostic style (`Split PDF …` in the `PDF` area), so a
future extension in Essentials/Documents can reuse this request instead of inventing another variant.

---

## Tests (pure Java only, per `krista-extension-testing`)

Logic lives in framework-free classes so it is directly testable; the `@CatalogRequest` methods stay
thin delegators and are validated by a green build plus a manual pass on an appliance.

| Class under test | Coverage |
|---|---|
| `PdfSplitter` | 110 @ 20 → six parts 20,20,20,20,20,10 with contiguous ranges; exact multiple produces no empty trailing part; source shorter than the split size → one part; single-page source; page order and per-part page counts |
| `SplitInputValidator` | `Pages Per Split` of 0 / negative / null / 2.5 each rejected with actionable messages; corrupt or non-PDF bytes rejected; encrypted source rejected with exactly `PDF is password protected` |
| `SplitFileNamer` | `<base>_p1-20.pdf` / `_p21-40.pdf` form; caller prefix wins over source name; unsafe characters sanitized; `.pdf` preserved |
| `PdfEngineProbe` | Healthy round-trip; failure reported as a message rather than thrown |

---

## ⚠️ Placeholder IDs — must be replaced before deploy

`domain-catalog.json` returns titles and request shapes but **not** the registered UUIDs, so the build
will mint stable placeholders that compile but **will not bind** to the real catalog:

| Placeholder | Needed value | How to get it |
|---|---|---|
| `{{DOMAIN_ID}}` | The registered `catEntryDomain_…` for **Documents** | Copy verbatim from any sibling extension already published in Essentials/Documents |
| `{{ECOSYSTEM_ID}}` | The registered `catEntryEcosystem_…` for **Essentials** | Same sibling — this value is shared byte-for-byte across the ecosystem |
| `{{ECOSYSTEM_VERSION}}` | Per-file value | A stable UUID is acceptable |

Because Essentials/Documents is an established domain with 23 published requests, these real IDs
already exist — grabbing them from a sibling is the fastest fix, and doing so before deploy is
mandatory.

Request IDs (`localDomainRequest_…`) and any entity IDs are **local** to this extension and are
correctly generated random-with-prefix (stable across releases) — those need no replacement.

## Out of scope

Build stops at the compiled jar in `build/libs/`. Binding the domain and publishing into
`krista-global-catalog` / a Krista appliance need Krista's registered infrastructure.
