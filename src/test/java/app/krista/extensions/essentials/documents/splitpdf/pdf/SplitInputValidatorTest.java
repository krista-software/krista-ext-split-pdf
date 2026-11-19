package app.krista.extensions.essentials.documents.splitpdf.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Input rejection rules: every bad input produces an actionable message, never a cryptic failure. */
class SplitInputValidatorTest {

    // ----------------------------------------------------------------- Pages Per Split

    @Test
    @DisplayName("a valid whole number is accepted")
    void acceptsWholeNumber() {
        assertEquals(20, SplitInputValidator.pagesPerSplit(20.0d));
        assertEquals(1, SplitInputValidator.pagesPerSplit(1.0d));
    }

    @Test
    @DisplayName("a missing Pages Per Split is rejected and says what to provide")
    void rejectsNull() {
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> SplitInputValidator.pagesPerSplit(null));
        assertTrue(e.getMessage().contains("Pages Per Split is required"), e.getMessage());
    }

    @Test
    @DisplayName("zero and negative page counts are rejected")
    void rejectsZeroAndNegative() {
        IllegalArgumentException zero =
                assertThrows(IllegalArgumentException.class, () -> SplitInputValidator.pagesPerSplit(0.0d));
        assertTrue(zero.getMessage().contains("at least 1"), zero.getMessage());

        IllegalArgumentException negative =
                assertThrows(IllegalArgumentException.class, () -> SplitInputValidator.pagesPerSplit(-3.0d));
        assertTrue(negative.getMessage().contains("at least 1"), negative.getMessage());
        assertTrue(negative.getMessage().contains("-3"), negative.getMessage());
    }

    @Test
    @DisplayName("a fractional page count is rejected as not a whole number")
    void rejectsFractional() {
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> SplitInputValidator.pagesPerSplit(2.5d));
        assertTrue(e.getMessage().contains("whole number"), e.getMessage());
    }

    @Test
    @DisplayName("NaN and infinity are rejected as not valid numbers")
    void rejectsNonFiniteNumbers() {
        assertThrows(IllegalArgumentException.class, () -> SplitInputValidator.pagesPerSplit(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> SplitInputValidator.pagesPerSplit(Double.POSITIVE_INFINITY));
    }

    // ----------------------------------------------------------------- the uploaded document

    @Test
    @DisplayName("a readable PDF loads and reports its page count")
    void loadsValidPdf() throws IOException {
        try (PDDocument document = SplitInputValidator.loadPdf(TestPdfs.withPages(3))) {
            assertEquals(3, document.getNumberOfPages());
        }
    }

    @Test
    @DisplayName("empty content is rejected")
    void rejectsEmptyContent() {
        IllegalArgumentException nullBytes =
                assertThrows(IllegalArgumentException.class, () -> SplitInputValidator.loadPdf(null));
        assertTrue(nullBytes.getMessage().contains("empty"), nullBytes.getMessage());

        assertThrows(IllegalArgumentException.class, () -> SplitInputValidator.loadPdf(new byte[0]));
    }

    @Test
    @DisplayName("content that is not a PDF is rejected with a readable message")
    void rejectsNonPdfContent() {
        byte[] notAPdf = "this is plainly not a pdf document".getBytes(StandardCharsets.UTF_8);

        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> SplitInputValidator.loadPdf(notAPdf));
        assertTrue(e.getMessage().contains("not a valid PDF"), e.getMessage());
    }

    @Test
    @DisplayName("a PDF locked with a user password is rejected as password protected")
    void rejectsUserPasswordProtectedPdf() throws IOException {
        byte[] protectedPdf = TestPdfs.userPasswordProtected(4);

        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> SplitInputValidator.loadPdf(protectedPdf));
        assertEquals(SplitInputValidator.PASSWORD_PROTECTED_MESSAGE, e.getMessage());
        assertTrue(e.getMessage().startsWith("PDF is password protected"), e.getMessage());
    }

    @Test
    @DisplayName("a PDF with only an owner password opens but is still rejected as protected")
    void rejectsOwnerPasswordOnlyPdf() throws IOException {
        byte[] protectedPdf = TestPdfs.ownerPasswordOnly(4);

        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> SplitInputValidator.loadPdf(protectedPdf));
        assertEquals(SplitInputValidator.PASSWORD_PROTECTED_MESSAGE, e.getMessage());
    }
}
