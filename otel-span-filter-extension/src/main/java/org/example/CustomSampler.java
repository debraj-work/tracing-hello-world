package org.example;

import io.opentelemetry.api.common.AttributeKey;
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
        if (isRedisMaintaince(attributes)) {
            return SamplingResult.create(SamplingDecision.DROP);
        }
        if (isMetricEndpoint(name)) {
            return SamplingResult.create(SamplingDecision.DROP);
        }
        var urlPath = attributes.get(UrlAttributes.URL_PATH);
        logger.info("Falling back to parent sampler.");
        return rootSampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    @Override
    public String getDescription() {
        return name + "{delegates to=" + rootSampler.getDescription() + "}";
    }

    private boolean isRedisMaintaince(Attributes attributes) {
        // Check if the span is a Redis maintenance operation
        String dbSystem = attributes.get(AttributeKey.stringKey("db.system"));
        String dbStatement = attributes.get(AttributeKey.stringKey("db.statement"));

        // Suppress Redis cluster health checks like CLUSTER NODES, PING, etc.
        if ("redis".equals(dbSystem)) {
            if (dbStatement != null &&
                    (dbStatement.contains("CLUSTER NODES") ||
                            dbStatement.contains("PING") ||
                            dbStatement.contains("CLIENT SETNAME lettuce#ClusterTopologyRefresh"))) {
                return true;
            }
        }
        return false;
    }

    private boolean isMetricEndpoint(String name) {
        // Check if the span is a metrics endpoint
        return  name.startsWith("HTTP") && (
                name.contains("/health") || name.contains("/metrics"));
    }
}
