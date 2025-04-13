package org.example;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import io.opentelemetry.semconv.UrlAttributes;

import java.util.List;
import java.util.logging.Logger;

public class CustomSampler implements Sampler {
    private static final String name = CustomSampler.class.getSimpleName();
    private static final Logger logger = java.util.logging.Logger.getLogger(CustomSampler.class.getName());
    private final Sampler rootSampler;

    public CustomSampler(Sampler rootSampler) {
        this.rootSampler = rootSampler;
    }
    @Override
    public SamplingResult shouldSample(Context parentContext, String traceId, String name, SpanKind spanKind, Attributes attributes, List<LinkData> parentLinks) {
        var map = attributes.asMap();
        for(var entry : map.entrySet()) {
            System.out.println("Entry: " + entry);
            logger.info("Entry: " + entry);
        }
        var urlPath = attributes.get(UrlAttributes.URL_PATH);
        // Example: Never sample /health checks
        if ("/health".equals(urlPath)) {
            logger.info("Dropping /health check span based on endpoint.");
            return SamplingResult.create(SamplingDecision.DROP);
        }
        logger.info("Falling back to parent sampler.");
        return rootSampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    @Override
    public String getDescription() {
        return name + "{delegates to=" + rootSampler.getDescription() + "}";
    }
}
