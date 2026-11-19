package app.krista.extensions.essentials.documents.splitpdf.catalog.support;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponse.Error.ExceptionType;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.documents.splitpdf.integration.KristaMediaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared base for every Split PDF area. Holds the injected {@link KristaMediaClient},
 * {@link TelemetryHelper} and the {@code invokerId} (multi-tenant scope), and owns the one
 * {@code audited(...)} wrapper so each catalog-request method stays thin: validate &rarr; process
 * &rarr; build response. The wrapper writes the uniform try/catch &rarr; classify &rarr;
 * {@link ExtensionResponse} + telemetry + timing exactly once.
 *
 * <p>The constructor takes only injectable services and {@link Invoker} — never a bare {@code String}
 * such as the invoker id, which HK2 cannot bind in this container.</p>
 */
public abstract class AbstractArea {

    protected static final String PREFIX = "splitpdf";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractArea.class);

    protected final KristaMediaClient media;
    protected final TelemetryHelper telemetry;
    protected final String invokerId;

    protected AbstractArea(KristaMediaClient media, TelemetryHelper telemetry, Invoker invoker) {
        this.media = media;
        this.telemetry = telemetry;
        this.invokerId = invoker.getInvokerId();
    }

    @FunctionalInterface
    protected interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    /** The uniform boundary: timing + telemetry + try/catch → classified ExtensionResponse. */
    protected ExtensionResponse audited(String name, CheckedSupplier<ExtensionResponse> action) {
        long t0 = telemetry.start(PREFIX + "." + name);
        LOG.info("Catalog '{}' started for invoker {}", name, invokerId);
        try {
            ExtensionResponse response = action.get();
            LOG.info("Catalog '{}' ok in {}ms for invoker {}", name, System.currentTimeMillis() - t0, invokerId);
            telemetry.recordSuccess(PREFIX + "." + name, t0);
            return response;
        } catch (SecurityException e) {
            LOG.error("Catalog '{}' AUTH: {}", name, e.getMessage());
            telemetry.recordError(PREFIX + "." + name, t0, e);
            return ExtensionResponses.failure(e.getMessage(), ExceptionType.AUTHENTICATION_ERROR);
        } catch (IllegalArgumentException e) {
            LOG.warn("Catalog '{}' INPUT: {}", name, e.getMessage());
            telemetry.recordError(PREFIX + "." + name, t0, e);
            return ExtensionResponses.failure(e.getMessage(), ExceptionType.INPUT_ERROR);
        } catch (Exception e) {
            LOG.error("Catalog '{}' SYSTEM: {}", name, e.getMessage(), e);
            telemetry.recordError(PREFIX + "." + name, t0, e);
            return ExtensionResponses.failure(e.getMessage(), ExceptionType.SYSTEM_ERROR);
        }
    }

    /** A fresh, insertion-ordered output map for a response. */
    protected static Map<String, Object> out() {
        return new LinkedHashMap<>();
    }
}
