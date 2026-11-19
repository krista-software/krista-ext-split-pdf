package app.krista.extensions.essentials.documents.splitpdf.pdf;

/**
 * One chunk produced by splitting a PDF — the generated PDF bytes plus the range of source pages it
 * covers. Pure data: no Krista types, no framework, so it is directly unit-testable.
 *
 * <p>Page numbers are 1-based and inclusive, matching what a reader sees in a PDF viewer: a 110-page
 * document split at 20 pages produces parts {@code 1-20, 21-40, 41-60, 61-80, 81-100, 101-110}.</p>
 *
 * @param partNumber 1-based index of this part in the output sequence
 * @param startPage  1-based first source page included in this part
 * @param endPage    1-based last source page included in this part (inclusive)
 * @param content    the generated PDF document as bytes
 */
public record PdfSplitPart(int partNumber, int startPage, int endPage, byte[] content) {

    /** Number of pages in this part — {@code endPage - startPage + 1}. */
    public int pageCount() {
        return endPage - startPage + 1;
    }
}
