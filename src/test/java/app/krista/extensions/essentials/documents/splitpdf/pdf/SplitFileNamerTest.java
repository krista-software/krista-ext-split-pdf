package app.krista.extensions.essentials.documents.splitpdf.pdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The page-range naming convention chosen for this extension. */
class SplitFileNamerTest {

    @Test
    @DisplayName("part names carry the page range: report_p1-20.pdf, report_p21-40.pdf, ...")
    void partNamesCarryThePageRange() {
        assertEquals("report_p1-20.pdf", SplitFileNamer.partFileName("report", 1, 20));
        assertEquals("report_p21-40.pdf", SplitFileNamer.partFileName("report", 21, 40));
        assertEquals("report_p101-110.pdf", SplitFileNamer.partFileName("report", 101, 110));
        assertEquals("report_p1-1.pdf", SplitFileNamer.partFileName("report", 1, 1));
    }

    @Test
    @DisplayName("the source file name supplies the base when no prefix is given")
    void sourceFileNameSuppliesTheBase() {
        assertEquals("report", SplitFileNamer.baseName(null, "report.pdf"));
        assertEquals("report", SplitFileNamer.baseName("  ", "report.pdf"));
        assertEquals("annual.report.2026", SplitFileNamer.baseName(null, "annual.report.2026.pdf"));
    }

    @Test
    @DisplayName("a caller-supplied prefix wins over the source file name")
    void prefixWinsOverSourceName() {
        assertEquals("Q3", SplitFileNamer.baseName("Q3", "report.pdf"));
        assertEquals("Q3_chunk_p1-20.pdf",
                SplitFileNamer.partFileName(SplitFileNamer.baseName("Q3 chunk", "report.pdf"), 1, 20));
    }

    @Test
    @DisplayName("an extension on the prefix is not repeated in the generated names")
    void prefixExtensionIsStripped() {
        assertEquals("summary", SplitFileNamer.baseName("summary.pdf", "report.pdf"));
    }

    @Test
    @DisplayName("unsafe characters are replaced so the name is safe to store")
    void unsafeCharactersAreSanitized() {
        assertEquals("my_report_v2", SplitFileNamer.baseName("my report/v2", "ignored.pdf"));
        assertEquals("a_b_c", SplitFileNamer.sanitize("a b|c"));
        assertEquals("keeps.dots-and_dashes", SplitFileNamer.sanitize("keeps.dots-and_dashes"));
    }

    @Test
    @DisplayName("with no usable prefix or source name, a safe default base is used")
    void fallsBackToDefaultBaseName() {
        assertEquals(SplitFileNamer.DEFAULT_BASE_NAME, SplitFileNamer.baseName(null, null));
        assertEquals(SplitFileNamer.DEFAULT_BASE_NAME, SplitFileNamer.baseName("", ""));
        assertEquals("document_p1-5.pdf", SplitFileNamer.partFileName(SplitFileNamer.baseName(null, null), 1, 5));
    }

    @Test
    @DisplayName("stripExtension removes only a real trailing extension")
    void stripExtensionBehaviour() {
        assertEquals("report", SplitFileNamer.stripExtension("report.pdf"));
        assertEquals("report", SplitFileNamer.stripExtension("report"));
        assertEquals(".profile", SplitFileNamer.stripExtension(".profile"));
        assertEquals("", SplitFileNamer.stripExtension(null));
    }
}
