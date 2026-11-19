package app.krista.extensions.essentials.documents.splitpdf.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;

/**
 * Self-check for the bundled PDF engine.
 *
 * <p>This extension calls no external system, so "Test Connection" has no server to reach. Instead it
 * proves the thing that can actually be broken at runtime: that Apache PDFBox is on the classpath and
 * can create, write and read back a document. That is exactly the capability every catalog request
 * here depends on.</p>
 *
 * <p>The probe never throws — a failure is reported as a result so the operator sees a message rather
 * than a stack trace.</p>
 */
public final class PdfEngineProbe {

    /**
     * Outcome of a probe.
     *
     * @param healthy whether the PDF engine round-tripped a document successfully
     * @param details human-readable detail, shown on the Setup tab and in Health Check
     */
    public record ProbeResult(boolean healthy, String details) {
    }

    /** Runs the real round-trip check. */
    public ProbeResult probe() {
        return probe(PdfEngineProbe::roundTripPageCount);
    }

    /**
     * Runs a round-trip check and converts any failure into an unhealthy result.
     *
     * <p>Package-private so a unit test can supply a failing round-trip and verify that the failure is
     * reported rather than thrown.</p>
     *
     * @param roundTrip returns the page count read back from a freshly written document
     */
    ProbeResult probe(Callable<Integer> roundTrip) {
        try {
            Integer pages = roundTrip.call();
            if (pages != null && pages == 1) {
                return new ProbeResult(true, "PDF engine ready (Apache PDFBox round-trip succeeded).");
            }
            return new ProbeResult(false,
                    "PDF engine check failed: expected to read back 1 page but read " + pages + ".");
        } catch (Exception e) {
            return new ProbeResult(false, "PDF engine unavailable: " + describe(e));
        }
    }

    /** Creates a one-page document, serializes it, reads it back, and returns its page count. */
    private static int roundTripPageCount() throws Exception {
        byte[] bytes;
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.addPage(new PDPage(PDRectangle.LETTER));
            document.save(out);
            bytes = out.toByteArray();
        }
        try (PDDocument reloaded = Loader.loadPDF(bytes)) {
            return reloaded.getNumberOfPages();
        }
    }

    private static String describe(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
