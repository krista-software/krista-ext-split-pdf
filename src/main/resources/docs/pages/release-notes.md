# Release Notes

## Version 1.0.0

- **Krista Service APIs Java** : 1.0.126
- **Global Catalog Version** : Not Available

### New Features

* **Split PDF By Page Count** — split an uploaded PDF into consecutive files of a fixed number of
  pages each. The last file carries the remainder, so a 110-page document split at 20 pages returns
  six files of 20, 20, 20, 20, 20 and 10 pages. Each result carries its page range and is returned
  as a downloadable PDF.
* Generated files are named after the pages they contain — `report_p1-20.pdf`, `report_p21-40.pdf`,
  and so on — with an optional caller-supplied name prefix.
* **Test Connection** and **Health Check** verify that the bundled PDF engine is present and
  working, and report JVM heap usage. Because the extension calls no external system, these replace
  the usual connectivity check.
* All three requests are Ask Agent (MCP) enabled.

### Resolved Bugs

* Not Available

### Dependency Updates

* Apache PDFBox 3.0.3 (Apache 2.0) added — the engine that performs the split.

### Known Issues

* Not Available

### Limitations and Caveats

* Password-protected PDFs are rejected with a clear message rather than split; the request takes no
  password input. Remove the protection from the document first.
* The document is held in memory while it is split, so very large PDFs need corresponding appliance
  heap. Use [Health Check](pages/HealthCheck.md) to see current heap usage.
* The extension needs no credentials and makes no outbound network calls — there is nothing to
  configure on the Setup tab and no Authentication tab.
* Splitting is by fixed page count only. Splitting on bookmarks, text matches or other conditions is
  not part of this release.
