package app.krista.extensions.essentials.documents.splitpdf;

import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.ChangeLog;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.Java;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extensions.essentials.documents.splitpdf.pdf.PdfEngineProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

/**
 * Split PDF extension entry point — splits an uploaded PDF into fixed-size page chunks.
 *
 * <p>There is no authentication of any kind: the extension calls no external system, all work is
 * done in-process with Apache PDFBox, and so there are no connection fields on the Setup tab and no
 * request authenticator. The Setup tab's Test Connection button instead checks that the bundled PDF
 * engine is present and working.</p>
 */
@Extension(name = "Split PDF", version = "1.0.0",
        description = "Split a PDF document into consecutive files of a fixed number of pages each.")
@StaticResource(path = "docs", file = "docs")
@ChangeLog(file = "docs/pages/release-notes.md")
@Java(version = Java.Version.JAVA_21)
public final class SplitPdfExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplitPdfExtension.class);

    private final Invoker invoker;
    private final PdfEngineProbe probe = new PdfEngineProbe();

    @Inject
    public SplitPdfExtension(Invoker invoker) {
        this.invoker = invoker;
    }

    /** Setup-tab tabs. Documentation only — there is no authentication tab because there is no auth. */
    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    /**
     * Test-Connection button. With no external system and no credentials, the meaningful check is
     * that the PDF engine this extension depends on is actually usable — throwing makes the button
     * report the failure to the operator.
     */
    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() throws IOException {
        PdfEngineProbe.ProbeResult result = probe.probe();
        if (!result.healthy()) {
            LOGGER.error("Split PDF self-check failed for invoker {}: {}", invoker.getInvokerId(),
                    result.details());
            throw new IOException(result.details());
        }
        LOGGER.info("Split PDF self-check succeeded for invoker {}", invoker.getInvokerId());
    }
}
