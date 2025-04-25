package org.example.utils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class CustomTracer {

    private final Tracer tracer;

    public CustomTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    private Span startSpan(String name) {
        return tracer.spanBuilder(name).startSpan();
    }

    public <T> T inSpan(String name, Supplier<T> supplier) {
        val span = startSpan(name);
        try (val scope = span.makeCurrent()) {
            return supplier.get();
        } finally {
            span.end();
        }
    }

    public void inSpan(String name, Runnable runnable) {
        Span span = startSpan(name);
        try (val scope = span.makeCurrent()) {
            runnable.run();
        } finally {
            span.end();
        }
    }
}