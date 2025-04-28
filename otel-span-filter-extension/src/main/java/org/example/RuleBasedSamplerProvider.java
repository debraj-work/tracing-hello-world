package org.example;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSampler;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.UrlAttributes;

public class RuleBasedSamplerProvider implements ConfigurableSamplerProvider {
    private static final String name = "rule-based-sampler";
    @Override
    public Sampler createSampler(ConfigProperties configProperties) {
        return RuleBasedRoutingSampler.builder(SpanKind.SERVER, Sampler.parentBased(Sampler.alwaysOn()))
                .drop(UrlAttributes.URL_PATH, "^/metrics$")
                .drop(UrlAttributes.URL_PATH, "^/health$")
                .build();
    }

    @Override
    public String getName() {
        return name;
    }
}
