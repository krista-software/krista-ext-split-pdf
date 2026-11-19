# Authentication

**Split PDF requires no authentication.**

The extension does not call any external system. Splitting is performed in-process on the Krista
appliance by the bundled Apache PDFBox library, so there is no server to sign in to, no API key, no
OAuth flow, and nothing to renew or rotate.

Concretely, this means:

- No secured connection fields on the Setup tab — see [Configuration](pages/ExtensionConfiguration.md).
- No Authentication tab, and no authorization prompt when a request runs.
- No scopes or permissions to grant in any third-party portal.
- No outbound network access required at runtime.

## Obtaining credentials

Not applicable — there are no credentials to obtain.

## Required scopes / permissions

| Scope / Permission | Why it's needed |
|---|---|
| None | The extension calls no external API. |

The only access it needs is Krista's own file store, so it can read the uploaded PDF and save the
generated pieces back. That is granted by the platform to every installed extension; there is
nothing for an administrator to configure.

## Verifying

Run **Test Connection** in the Setup area (or press the button on the Setup tab). With no connection
to test, it verifies that the PDF engine loads and can round-trip a document, and returns
`PDF engine ready (Apache PDFBox round-trip succeeded).` on success. A failure means the extension
jar is damaged or incomplete rather than that a credential is wrong — see
[Test Connection](pages/TestConnection.md).
