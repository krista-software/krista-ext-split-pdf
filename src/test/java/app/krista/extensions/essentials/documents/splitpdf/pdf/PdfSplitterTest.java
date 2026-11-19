package app.krista.extensions.essentials.documents.splitpdf.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Page arithmetic and content of the split operation. */
class PdfSplitterTest {

    private final PdfSplitter splitter = new PdfSplitter();

    @Test
    @DisplayName("110 pages split at 20 yields 6 parts of 20,20,20,20,20,10 with contiguous ranges")
    void splitsOneHundredTenPagesAtTwenty() throws IOException {
        try (PDDocument source = load(TestPdfs.withPages(110))) {
            List<PdfSplitPart> parts = splitter.split(source, 20);

            assertEquals(6, parts.size(), "a 110-page document split at 20 produces 6 files");
            assertEquals(List.of(20, 20, 20, 20, 20, 10), parts.stream().map(PdfSplitPart::pageCount).toList());
            assertEquals(List.of(1, 21, 41, 61, 81, 101), parts.stream().map(PdfSplitPart::startPage).toList());
            assertEquals(List.of(20, 40, 60, 80, 100, 110), parts.stream().map(PdfSplitPart::endPage).toList());
            assertEquals(List.of(1, 2, 3, 4, 5, 6), parts.stream().map(PdfSplitPart::partNumber).toList());
        }
    }

    @Test
    @DisplayName("every generated part is a readable PDF whose real page count matches its declared count")
    void generatedPartsAreReadablePdfs() throws IOException {
        try (PDDocument source = load(TestPdfs.withPages(110))) {
            for (PdfSplitPart part : splitter.split(source, 20)) {
                assertTrue(part.content().length > 0, "part " + part.partNumber() + " has content");
                try (PDDocument reloaded = load(part.content())) {
                    assertEquals(part.pageCount(), reloaded.getNumberOfPages(),
                            "part " + part.partNumber() + " page count");
                }
            }
        }
    }

    @Test
    @DisplayName("an exact multiple produces no empty trailing part")
    void exactMultipleProducesNoTrailingPart() throws IOException {
        try (PDDocument source = load(TestPdfs.withPages(100))) {
            List<PdfSplitPart> parts = splitter.split(source, 20);

            assertEquals(5, parts.size());
            assertEquals(100, parts.get(4).endPage());
            assertFalse(parts.stream().anyMatch(p -> p.pageCount() == 0), "no empty part");
        }
    }

    @Test
    @DisplayName("a document shorter than the split size yields a single part covering every page")
    void shorterThanSplitSizeYieldsOnePart() throws IOException {
        try (PDDocument source = load(TestPdfs.withPages(7))) {
            List<PdfSplitPart> parts = splitter.split(source, 20);

            assertEquals(1, parts.size());
            assertEquals(1, parts.get(0).startPage());
            assertEquals(7, parts.get(0).endPage());
            assertEquals(7, parts.get(0).pageCount());
        }
    }

    @Test
    @DisplayName("a single-page document yields one part covering page 1 to 1")
    void singlePageDocument() throws IOException {
        try (PDDocument source = load(TestPdfs.withPages(1))) {
            List<PdfSplitPart> parts = splitter.split(source, 5);

            assertEquals(1, parts.size());
            assertEquals(1, parts.get(0).startPage());
            assertEquals(1, parts.get(0).endPage());
        }
    }

    @Test
    @DisplayName("splitting at 1 page yields one file per page")
    void splitAtOnePagePerFile() throws IOException {
        try (PDDocument source = load(TestPdfs.withPages(4))) {
            List<PdfSplitPart> parts = splitter.split(source, 1);

            assertEquals(4, parts.size());
            assertEquals(List.of(1, 2, 3, 4), parts.stream().map(PdfSplitPart::startPage).toList());
            assertEquals(List.of(1, 2, 3, 4), parts.stream().map(PdfSplitPart::endPage).toList());
        }
    }

    @Test
    @DisplayName("pages land in the right part, in source order")
    void pagesArePreservedInOrder() throws IOException {
        try (PDDocument source = load(TestPdfs.withPages(10))) {
            List<PdfSplitPart> parts = splitter.split(source, 4);

            int sourceIndex = 0;
            for (PdfSplitPart part : parts) {
                try (PDDocument reloaded = load(part.content())) {
                    for (int i = 0; i < reloaded.getNumberOfPages(); i++) {
                        float width = reloaded.getPage(i).getMediaBox().getWidth();
                        assertEquals(TestPdfs.expectedWidth(sourceIndex), width, 0.01f,
                                "source page " + (sourceIndex + 1) + " should be page " + (i + 1)
                                        + " of part " + part.partNumber());
                        sourceIndex++;
                    }
                }
            }
            assertEquals(10, sourceIndex, "every source page appears exactly once across the parts");
        }
    }

    @Test
    @DisplayName("a non-positive split size is rejected")
    void rejectsNonPositiveSplitSize() throws IOException {
        try (PDDocument source = load(TestPdfs.withPages(3))) {
            assertThrows(IllegalArgumentException.class, () -> splitter.split(source, 0));
            assertThrows(IllegalArgumentException.class, () -> splitter.split(source, -4));
        }
    }

    @Test
    @DisplayName("a missing source document is rejected")
    void rejectsNullSource() {
        assertThrows(IllegalArgumentException.class, () -> splitter.split(null, 10));
    }

    private static PDDocument load(byte[] bytes) throws IOException {
        return Loader.loadPDF(bytes);
    }
}
