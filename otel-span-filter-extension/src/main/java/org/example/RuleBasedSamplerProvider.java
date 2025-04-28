package org.example;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.UrlAttributes;

import java.util.logging.Logger;

public class RuleBasedSamplerProvider implements ConfigurableSamplerProvider {
    private static final Logger logger = Logger.getLogger(RuleBasedSamplerProvider.class.getName());
    private static final String name = "RuleBasedSamplerProvider";
    @Override
    public Sampler createSampler(ConfigProperties configProperties) {
        logger.info("Creating Sampler for " + name);
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
