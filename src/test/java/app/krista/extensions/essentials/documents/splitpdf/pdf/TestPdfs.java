package app.krista.extensions.essentials.documents.splitpdf.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Test fixtures: in-memory PDFs with a known number of pages.
 *
 * <p>Each page is given a distinct width ({@code 100 + pageIndex} points) so a test can prove that
 * pages landed in the right output part, in the right order, without needing text extraction.</p>
 */
final class TestPdfs {

    /** Width of page {@code index} (0-based) in a generated document. */
    static float expectedWidth(int pageIndex) {
        return 100f + pageIndex;
    }

    private TestPdfs() {
    }

    /** A plain, unencrypted PDF with {@code pageCount} pages. */
    static byte[] withPages(int pageCount) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addPages(document, pageCount);
            document.save(out);
            return out.toByteArray();
        }
    }

    /** A PDF that cannot be opened at all without the user password. */
    static byte[] userPasswordProtected(int pageCount) throws IOException {
        return protect(pageCount, "owner-secret", "user-secret");
    }

    /** A PDF that opens with an empty password but is still flagged as encrypted. */
    static byte[] ownerPasswordOnly(int pageCount) throws IOException {
        return protect(pageCount, "owner-secret", "");
    }

    private static byte[] protect(int pageCount, String ownerPassword, String userPassword) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addPages(document, pageCount);
            StandardProtectionPolicy policy =
                    new StandardProtectionPolicy(ownerPassword, userPassword, new AccessPermission());
            policy.setEncryptionKeyLength(128);
            document.protect(policy);
            document.save(out);
            return out.toByteArray();
        }
    }

    private static void addPages(PDDocument document, int pageCount) {
        for (int i = 0; i < pageCount; i++) {
            document.addPage(new PDPage(new PDRectangle(expectedWidth(i), 200f)));
        }
    }
}
