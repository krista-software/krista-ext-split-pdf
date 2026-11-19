package app.krista.extensions.essentials.documents.splitpdf.pdf;

/**
 * Names the generated PDF parts.
 *
 * <p>The naming convention for this extension is the page range: splitting {@code report.pdf} at 20
 * pages produces {@code report_p1-20.pdf}, {@code report_p21-40.pdf}, and so on. The base name comes
 * from the caller's Output File Name Prefix when supplied, otherwise from the uploaded file's own
 * name with its extension removed.</p>
 *
 * <p>Pure Java, no framework types.</p>
 */
public final class SplitFileNamer {

    /** Used when neither a prefix nor a usable source file name is available. */
    public static final String DEFAULT_BASE_NAME = "document";

    private static final String PDF_EXTENSION = ".pdf";

    private SplitFileNamer() {
    }

    /**
     * Chooses the base name for every generated part: the caller's prefix wins, then the source file
     * name with its extension stripped, then a safe default.
     *
     * @param prefix         the Output File Name Prefix input, may be null or blank
     * @param sourceFileName the uploaded file's name, may be null or blank
     * @return a sanitized, non-blank base name with no extension
     */
    public static String baseName(String prefix, String sourceFileName) {
        String chosen = isBlank(prefix) ? sourceFileName : prefix;
        // Strip the extension exactly once, whichever source the name came from, so a name that
        // legitimately contains dots ("annual.report.2026.pdf") keeps them.
        String safe = sanitize(stripExtension(chosen));
        return safe.isEmpty() ? DEFAULT_BASE_NAME : safe;
    }

    /**
     * Builds the file name for one part: {@code <base>_p<start>-<end>.pdf}.
     *
     * @param baseName  the sanitized base name (see {@link #baseName(String, String)})
     * @param startPage 1-based first page in the part
     * @param endPage   1-based last page in the part
     * @return the file name including the {@code .pdf} extension
     */
    public static String partFileName(String baseName, int startPage, int endPage) {
        String base = isBlank(baseName) ? DEFAULT_BASE_NAME : sanitize(baseName.trim());
        if (base.isEmpty()) {
            base = DEFAULT_BASE_NAME;
        }
        return base + "_p" + startPage + "-" + endPage + PDF_EXTENSION;
    }

    /** Removes a trailing file extension, if any ({@code report.pdf} → {@code report}). */
    public static String stripExtension(String fileName) {
        if (isBlank(fileName)) {
            return "";
        }
        String trimmed = fileName.trim();
        int dot = trimmed.lastIndexOf('.');
        // A leading dot is part of the name (".profile"), not an extension separator.
        return dot > 0 ? trimmed.substring(0, dot) : trimmed;
    }

    /**
     * Replaces anything outside {@code [A-Za-z0-9._-]} with an underscore so the name is safe for the
     * media store and for any file system it is later written to.
     */
    public static String sanitize(String name) {
        return name == null ? "" : name.trim().replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
