package org.example.utils;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import java.util.function.Supplier;
import lombok.val;

/**
 * An utility class for adding custom spans for an application already instrumented via
 * OpenTelemetry Java agent.
 */
public class TracingUtils {
    private static final String NAME = "nexla_custom_span";

    private final Tracer tracer;

    /**
     * Constructs a new instance of TracingUtils with the provided Tracer.
     *
     * @param tracer the Tracer to be used for creating spans
     */
    public TracingUtils(final Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Initializes the TracingUtils with the global OpenTelemetry tracer. Global tracer is set
     * OpenTelemetry java agent.
     */
    @SuppressWarnings("unused")
    public TracingUtils() {
        this(GlobalOpenTelemetry.getTracer(NAME));
    }

    /**
     * Executes a supplier within a span context. The span is automatically ended after execution.
     *
     * @param <T> the type of the result returned by the supplier
     * @param name the name of the span
     * @param supplier the supplier to execute within the span
     * @return the result of the supplier
     */
    public <T> T inSpan(final String name, final Supplier<T> supplier) {
        return runInSpan(spanBuilder(name), supplier);
    }

    /**
     * Executes a runnable within a span context. The span is automatically ended after execution.
     *
     * @param name the name of the span
     * @param runnable the runnable to execute within the span
     */
    public void inSpan(final String name, final Runnable runnable) {
        runInSpan(
                spanBuilder(name),
                () -> {
                    runnable.run();
                    return null;
                });
    }

    /**
     * Starts a new trace for each request, linking it to the current trace.
     *
     * <p>Sometimes there may be cases like below Batch of requests -> Process Each Request -> Send to
     * downstream services.
     *
     * <p>And for each procssing of request, we want to create a new trace_id rather than using the
     * original trace_id.
     *
     * @param name name of the new span (root of new trace)
     * @param runnable logic for processing the individual request
     */
    public void inNewLinkedTrace(final String name, final Runnable runnable) {
        runInNewLinkedTrace(
                name,
                () -> {
                    runnable.run();
                    return null;
                });
    }

    public <T> T inNewLinkedTrace(final String name, final Supplier<T> supplier) {
        return runInNewLinkedTrace(name, supplier);
    }

    private <T> T runInNewLinkedTrace(final String name, final Supplier<T> supplier) {
        val currentSpan = Span.current();
        val parentContext = currentSpan.getSpanContext();

        val spanBuilder = spanBuilder(name);
        spanBuilder.setNoParent(); // new trace_id
        if (parentContext.isValid()) {
            spanBuilder.addLink(parentContext);
        }
        return runInSpan(spanBuilder, supplier);
    }

    private <T> T runInSpan(final SpanBuilder spanBuilder, Supplier<T> supplier) {
        val span = spanBuilder.startSpan();
        try (val ignored = span.makeCurrent()) {
            return supplier.get();
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }

    private SpanBuilder spanBuilder(final String name) {
        return tracer.spanBuilder(name).setSpanKind(SpanKind.INTERNAL);
    }
}