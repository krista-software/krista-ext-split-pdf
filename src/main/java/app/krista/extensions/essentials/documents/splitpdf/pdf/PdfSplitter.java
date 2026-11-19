package app.krista.extensions.essentials.documents.splitpdf.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Splits an open PDF into consecutive chunks of a fixed page count.
 *
 * <p>The last chunk carries whatever is left over: a 110-page document split at 20 pages yields six
 * parts of 20, 20, 20, 20, 20 and 10 pages. A document with fewer pages than the chunk size yields a
 * single part containing every page, and an exact multiple yields no empty trailing part.</p>
 *
 * <p>Pure Java (PDFBox only — no Krista types), so the page arithmetic is unit-tested directly.</p>
 */
public final class PdfSplitter {

    /**
     * Splits the document into chunks of {@code pagesPerSplit} pages.
     *
     * @param source        an open source document; it is read, never modified, and not closed here
     * @param pagesPerSplit pages per output chunk — already validated as a positive whole number
     * @return the generated parts, in page order, never empty for a document with at least one page
     * @throws IOException if PDFBox cannot copy a page or serialize a generated document
     */
    public List<PdfSplitPart> split(PDDocument source, int pagesPerSplit) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("PDF File is required.");
        }
        if (pagesPerSplit < 1) {
            throw new IllegalArgumentException("Pages Per Split must be at least 1, but was " + pagesPerSplit + ".");
        }

        int totalPages = source.getNumberOfPages();
        List<PdfSplitPart> parts = new ArrayList<>();
        int partNumber = 1;

        for (int firstIndex = 0; firstIndex < totalPages; firstIndex += pagesPerSplit) {
            int endIndexExclusive = Math.min(firstIndex + pagesPerSplit, totalPages);
            byte[] content = buildPart(source, firstIndex, endIndexExclusive);
            // Page numbers reported to the caller are 1-based and inclusive.
            parts.add(new PdfSplitPart(partNumber, firstIndex + 1, endIndexExclusive, content));
            partNumber++;
        }

        return parts;
    }

    /** Copies pages [firstIndex, endIndexExclusive) into a fresh document and serializes it. */
    private static byte[] buildPart(PDDocument source, int firstIndex, int endIndexExclusive) throws IOException {
        try (PDDocument part = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int i = firstIndex; i < endIndexExclusive; i++) {
                part.importPage(source.getPage(i));
            }
            part.save(out);
            return out.toByteArray();
        }
    }
}
