package org.example;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DroppingSpanProcessor implements SpanProcessor {
    private static final Logger logger = Logger.getLogger(DroppingSpanProcessor.class.getName());
    private final SpanProcessor nextProcessor;
    private final String attributeKeyPrefixToMatch;

    public DroppingSpanProcessor(SpanProcessor nextProcessor, String attributeKeyPrefix) {
        this.nextProcessor = Objects.requireNonNull(nextProcessor, "nextProcessor must not be null");
        this.attributeKeyPrefixToMatch = Objects.requireNonNull(attributeKeyPrefix, "attributeKeyPrefix must not be null");
        if (attributeKeyPrefix.isEmpty()) {
            throw new IllegalArgumentException("attributeKeyPrefix must not be empty");
        }
        // Updated log message slightly
        logger.log(Level.INFO, "Initialized DroppingSpanProcessor to drop individual spans with attribute keys starting with: " + attributeKeyPrefix);
    }

    @Override
    public void onStart(Context context, ReadWriteSpan readWriteSpan) {
        nextProcessor.onStart(context, readWriteSpan);
    }

    @Override
    public boolean isStartRequired() {
        return nextProcessor.isStartRequired();
    }

    @Override
    public void onEnd(ReadableSpan span) {
        // Flag to check if we found a matching attribute in *this* span
        boolean keyPrefixMatchFound = false;
        String matchingKey = null; // Store the key that caused the match for logging
        var spanContext = span.getSpanContext();

        // --- Check attributes of the current span for the prefix ---
        var attributes = span.getAttributes().asMap();
        for (var attr : attributes.entrySet()) {
            logger.log(Level.INFO, String.format("Key: %s, Value: %s", attr.getKey(), attr.getValue()));
            System.out.printf("\nKey: %s, Value: %s", attr.getKey(), attr.getValue());
        }
        if (attributes != null && !attributes.isEmpty()) {
            // Use a temporary variable to store the matching key found within the lambda
            final String[] foundKeyHolder = {null};
            try {
                attributes.forEach((key, value) -> {
                    String keyString = key.getKey();
                    if (keyString != null && keyString.startsWith(attributeKeyPrefixToMatch)) {
                        foundKeyHolder[0] = keyString; // Store the key
                        // Throwing an exception is a way to break out of forEach early
                        throw new BreakIterationException();
                    }
                });
            } catch (BreakIterationException e) {
                // We found a match and broke out
                keyPrefixMatchFound = true;
                matchingKey = foundKeyHolder[0];
            }
        }
        // --- End attribute check ---


        // If a match was found in *this* span, drop *this span only* by returning early.
        if (keyPrefixMatchFound) {
            ;
            logger.log(Level.INFO, String.format("Dropping spanId %s from traceId: %s due to attribute key: %s starting with prefix: %s", spanContext.getSpanId(), spanContext.getTraceId(), matchingKey, attributeKeyPrefixToMatch));
            // Do not delegate this span to the next processor
            return;
        }

        // If no match was found in this span's attributes, process it normally.
        // Removed: Check for traceIdsToDrop as we no longer track trace state.
        logger.log(Level.INFO, String.format("Processing span: %s from traceId: %s", spanContext.getSpanId(), spanContext.getTraceId()));
        nextProcessor.onEnd(span);
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    @Override
    public CompletableResultCode shutdown() {
        // No trace state to clear
        return nextProcessor.shutdown();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return nextProcessor.forceFlush();
    }

    // Helper exception class to break out of forEach
    private static class BreakIterationException extends RuntimeException {
        private static final long serialVersionUID = 1L; // Optional
    }

}
