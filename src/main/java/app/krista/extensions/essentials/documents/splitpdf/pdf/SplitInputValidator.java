package app.krista.extensions.essentials.documents.splitpdf.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import java.io.IOException;

/**
 * Input validation for the Split PDF request. Every rejection throws {@link IllegalArgumentException}
 * with a message naming the field, why it failed, and what a good value looks like — the area's
 * audited wrapper turns that exception into an INPUT-classified failure response.
 *
 * <p>Password-protected documents are rejected outright rather than prompting for a password: that is
 * the behaviour chosen for this build, so the message says so in plain words.</p>
 *
 * <p>Pure Java — no Krista or framework types — so it is unit-tested directly.</p>
 */
public final class SplitInputValidator {

    /** Exact wording used whenever the source document is protected, in any form. */
    public static final String PASSWORD_PROTECTED_MESSAGE =
            "PDF is password protected. This request cannot split protected documents — "
                    + "remove the password from the PDF and upload it again.";

    private SplitInputValidator() {
    }

    /**
     * Validates the Pages Per Split input and returns it as a positive whole number.
     *
     * @param pagesPerSplit the raw Number input from Krista (a {@code Double})
     * @return the validated chunk size
     * @throws IllegalArgumentException if it is missing, zero, negative, or not a whole number
     */
    public static int pagesPerSplit(Double pagesPerSplit) {
        if (pagesPerSplit == null) {
            throw new IllegalArgumentException(
                    "Pages Per Split is required. Provide the number of pages each output file should "
                            + "contain. Examples: 1, 10, 20.");
        }
        double value = pagesPerSplit;
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(
                    "Pages Per Split '" + pagesPerSplit + "' is not a valid number. Provide a positive "
                            + "whole number of pages. Examples: 1, 10, 20.");
        }
        if (value != Math.floor(value)) {
            throw new IllegalArgumentException(
                    "Pages Per Split must be a whole number of pages, but was " + trim(value)
                            + ". Examples: 1, 10, 20.");
        }
        if (value < 1) {
            throw new IllegalArgumentException(
                    "Pages Per Split must be at least 1, but was " + trim(value)
                            + ". Provide the number of pages each output file should contain. "
                            + "Examples: 1, 10, 20.");
        }
        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Pages Per Split " + trim(value) + " is larger than any PDF can be. "
                            + "Provide a realistic page count. Examples: 1, 10, 20.");
        }
        return (int) value;
    }

    /**
     * Loads the uploaded bytes as a PDF, rejecting anything this request cannot split.
     *
     * @param pdfBytes the uploaded document's bytes
     * @return an open {@link PDDocument} — the caller owns it and must close it
     * @throws IllegalArgumentException if the bytes are empty, not a PDF, or password protected
     * @throws IOException              only for genuinely unexpected I/O problems
     */
    public static PDDocument loadPdf(byte[] pdfBytes) throws IOException {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException(
                    "PDF File is empty. Upload a PDF document with at least one page.");
        }

        PDDocument document;
        try {
            document = Loader.loadPDF(pdfBytes);
        } catch (InvalidPasswordException e) {
            // Encrypted with a user password PDFBox cannot open without one.
            throw new IllegalArgumentException(PASSWORD_PROTECTED_MESSAGE, e);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "PDF File is not a valid PDF document and could not be read (" + e.getMessage()
                            + "). Upload a readable PDF file.", e);
        }

        // Opened with an empty password (owner-password-only documents land here) — still protected,
        // and still out of scope for this request, so close it and reject.
        if (document.isEncrypted()) {
            closeQuietly(document);
            throw new IllegalArgumentException(PASSWORD_PROTECTED_MESSAGE);
        }

        if (document.getNumberOfPages() < 1) {
            closeQuietly(document);
            throw new IllegalArgumentException(
                    "PDF File contains no pages. Upload a PDF document with at least one page.");
        }

        return document;
    }

    private static void closeQuietly(PDDocument document) {
        try {
            document.close();
        } catch (IOException ignored) {
            // Nothing useful to do while already rejecting the input.
        }
    }

    /** Renders a double without a pointless trailing {@code .0} for use in messages. */
    private static String trim(double value) {
        return value == Math.floor(value) && !Double.isInfinite(value)
                ? String.valueOf((long) value)
                : String.valueOf(value);
    }
}
