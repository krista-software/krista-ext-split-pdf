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
import app.krista.extensions.essentials.documents.splitpdf.pdf.PdfEngineProbe;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * Setup area — Test Connection + Health Check.
 *
 * <p>This extension calls no external system and needs no credentials, so there is no connection to
 * test. Both requests instead verify what can genuinely fail at runtime: that the bundled PDF engine
 * is present and able to create, write and read back a document.</p>
 */
@Domain(id = "catEntryDomain_49c18799-c16b-4d33-98ac-f048f446a152",
        name = "Documents",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "bb29f50b-cfca-4958-a95f-0fa52fc5c494")
public class SetupArea extends AbstractArea {

    private final PdfEngineProbe probe = new PdfEngineProbe();

    @Inject
    public SetupArea(KristaMediaClient media, TelemetryHelper telemetry, Invoker invoker) {
        super(media, telemetry, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_2c8d41ab-69f0-4e57-8a13-5d9b7c0e64f2",
            name = "Test Connection",
            description = "Verify the extension is installed correctly and its PDF engine is working. "
                    + "No credentials or network access are required.",
            area = "Setup",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Boolean(name = "Success", required = false, attributes = {}, options = {})
    @Field.Text(name = "Message", required = false, attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {})
    public ExtensionResponse testConnection() {
        return audited("TestConnection", () -> {
            PdfEngineProbe.ProbeResult result = probe.probe();
            Map<String, Object> out = out();
            out.put("Success", result.healthy());
            out.put("Message", result.details());
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_9e4a37d1-0b82-4c66-bf59-3a71e85d2c04",
            name = "Health Check",
            description = "Report extension JVM health and PDF engine readiness.",
            area = "Setup",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Boolean(name = "Is Healthy", required = false, attributes = {}, options = {})
    @Field(name = "Heap Used MB", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = "Details", required = false, attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {})
    public ExtensionResponse healthCheck() {
        return audited("HealthCheck", () -> {
            long heapUsedMb = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024);
            PdfEngineProbe.ProbeResult result = probe.probe();
            Map<String, Object> out = out();
            out.put("Is Healthy", result.healthy());
            out.put("Heap Used MB", (double) heapUsedMb);
            out.put("Details", result.details());
            return ExtensionResponses.ok(out);
        });
    }
}
