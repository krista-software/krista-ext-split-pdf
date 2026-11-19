package app.krista.extensions.essentials.documents.splitpdf.pdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Setup-tab self-check: reports, never throws. */
class PdfEngineProbeTest {

    private final PdfEngineProbe probe = new PdfEngineProbe();

    @Test
    @DisplayName("the real round-trip reports a healthy PDF engine")
    void realRoundTripIsHealthy() {
        PdfEngineProbe.ProbeResult result = probe.probe();

        assertTrue(result.healthy(), result.details());
        assertTrue(result.details().contains("PDF engine ready"), result.details());
    }

    @Test
    @DisplayName("a failing engine is reported as a message, not thrown")
    void failureIsReportedNotThrown() {
        PdfEngineProbe.ProbeResult result = probe.probe(() -> {
            throw new IllegalStateException("pdfbox missing from the classpath");
        });

        assertFalse(result.healthy());
        assertTrue(result.details().contains("PDF engine unavailable"), result.details());
        assertTrue(result.details().contains("pdfbox missing from the classpath"), result.details());
    }

    @Test
    @DisplayName("an exception with no message still produces a usable detail line")
    void failureWithoutMessageStillDescribed() {
        PdfEngineProbe.ProbeResult result = probe.probe(() -> {
            throw new IllegalStateException();
        });

        assertFalse(result.healthy());
        assertTrue(result.details().contains("IllegalStateException"), result.details());
    }

    @Test
    @DisplayName("a round-trip that reads back the wrong page count is unhealthy")
    void wrongPageCountIsUnhealthy() {
        PdfEngineProbe.ProbeResult result = probe.probe(() -> 0);

        assertFalse(result.healthy());
        assertTrue(result.details().contains("expected to read back 1 page"), result.details());
    }
}
