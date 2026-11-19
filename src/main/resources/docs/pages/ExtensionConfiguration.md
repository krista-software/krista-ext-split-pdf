# Configuration

The extension is configured through the **Setup** tab in Krista. Configuration is stored per-invoker
— each invoker keeps its own independent settings.

## Setup tab fields

**There are none.** Split PDF declares no connection fields at all, because it calls no external
system: the PDF work is done in-process on the appliance. Add the invoker and it is ready to use.

| Field | Required | Default | Description |
|-------|----------|---------|-------------|
| — | — | — | This extension has no Setup-tab configuration fields. |

## Required vs optional

Nothing is validated on save, because there is nothing to fill in. Everything that varies between
runs — the document, the chunk size, the output file name prefix — is supplied as an input to the
[Split PDF By Page Count](pages/SplitPdfByPageCount.md) request each time it is called, not as
stored configuration.

## Verifying the installation

Use the Setup tab's **Test Connection** button, or run the
[Test Connection](pages/TestConnection.md) catalog request. Because there is no server to reach,
both check the thing that can genuinely fail: that the bundled PDF engine is present and able to
create, write and read back a document.

## Notes

- **Tabs.** The extension contributes one custom tab, **Documentation** (this site). There is no
  Authentication tab, because there is no authentication — see [Authentication](pages/Authentication.md).
- **Ask Agent (MCP).** All three requests are agent-callable, so a Krista agent can split a document
  on a user's behalf.
- **Temporary files.** Each run writes the uploaded document and the generated chunks to a private
  temporary directory on the appliance and removes the source copy as soon as it has been read. File
  contents are never written to the log.
