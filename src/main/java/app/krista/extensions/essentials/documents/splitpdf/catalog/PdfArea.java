package app.krista.extensions.essentials.documents.splitpdf.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.documents.splitpdf.catalog.support.AbstractArea;
import app.krista.extensions.essentials.documents.splitpdf.catalog.support.ExtensionResponses;
import app.krista.extensions.essentials.documents.splitpdf.catalog.support.TelemetryHelper;
import app.krista.extensions.essentials.documents.splitpdf.integration.KristaMediaClient;
import app.krista.extensions.essentials.documents.splitpdf.pdf.PdfSplitPart;
import app.krista.extensions.essentials.documents.splitpdf.pdf.PdfSplitter;
import app.krista.extensions.essentials.documents.splitpdf.pdf.SplitFileNamer;
import app.krista.extensions.essentials.documents.splitpdf.pdf.SplitInputValidator;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.inject.Inject;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** PDF catalog requests. */
@Domain(id = "catEntryDomain_49c18799-c16b-4d33-98ac-f048f446a152",
        name = "Documents",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "bb29f50b-cfca-4958-a95f-0fa52fc5c494")
public class PdfArea extends AbstractArea {

    private final PdfSplitter splitter = new PdfSplitter();

    @Inject
    public PdfArea(KristaMediaClient media, TelemetryHelper telemetry, Invoker invoker) {
        super(media, telemetry, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_7b1f0c62-5a94-4d3e-9f21-0c6a2d41b8e5",
            name = "Split PDF By Page Count",
            description = "Split a PDF into consecutive files of a fixed number of pages each. "
                    + "A 110-page document split at 20 pages returns 6 files: five of 20 pages and a "
                    + "final one of 10. The source document is left unchanged.",
            area = "PDF",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Split Files",
            type = "[ { PartNumber: Number, FileName: Text, StartPage: Number, EndPage: Number, PageCount: Number, File: File } ]",
            required = false)
    @Field(name = "File Count", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field(name = "Total Pages", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Boolean(name = "Success", required = false, attributes = {}, options = {})
    @Field.Text(name = "Message", required = false, attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {})
    public ExtensionResponse splitPdfByPageCount(
            @Field.File(name = "PDF File", multipleFileUpload = false, required = true,
                    attributes = {}, options = {}) app.krista.model.base.File pdfFile,
            @Field(name = "Pages Per Split", type = "Number", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Double pagesPerSplit,
            @Field.Text(name = "Output File Name Prefix", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String outputFileNamePrefix) {
        return audited("SplitPdfByPageCount", () -> {
            int chunkSize = SplitInputValidator.pagesPerSplit(pagesPerSplit);
            if (pdfFile == null) {
                throw new IllegalArgumentException("PDF File is required. Upload a PDF document to split.");
            }

            java.io.File localSource = media.toJavaFile(pdfFile);
            byte[] sourceBytes;
            try {
                sourceBytes = Files.readAllBytes(localSource.toPath());
            } finally {
                media.deleteQuietly(localSource);
            }

            String baseName = SplitFileNamer.baseName(outputFileNamePrefix, pdfFile.getFileName());
            List<Map<String, Object>> rows = new ArrayList<>();
            int totalPages;

            try (PDDocument source = SplitInputValidator.loadPdf(sourceBytes)) {
                totalPages = source.getNumberOfPages();
                for (PdfSplitPart part : splitter.split(source, chunkSize)) {
                    String fileName = SplitFileNamer.partFileName(baseName, part.startPage(), part.endPage());
                    app.krista.model.base.File stored = media.uploadBytes(part.content(), fileName);

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("PartNumber", (double) part.partNumber());
                    row.put("FileName", stored.getFileName());
                    row.put("StartPage", (double) part.startPage());
                    row.put("EndPage", (double) part.endPage());
                    row.put("PageCount", (double) part.pageCount());
                    row.put("File", stored);
                    rows.add(row);
                }
            }

            Map<String, Object> out = out();
            out.put("Split Files", rows);
            out.put("File Count", (double) rows.size());
            out.put("Total Pages", (double) totalPages);
            out.put("Success", true);
            out.put("Message", "Split " + totalPages + " page(s) into " + rows.size()
                    + " file(s) of up to " + chunkSize + " page(s) each.");
            return ExtensionResponses.ok(out);
        });
    }
}
