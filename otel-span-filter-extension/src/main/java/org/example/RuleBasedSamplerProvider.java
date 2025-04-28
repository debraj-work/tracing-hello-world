package org.example;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class RuleBasedSamplerProvider implements ConfigurableSamplerProvider {
    private static final String ENV_RULE_SAMPLER_DROP_YAML_FILE = "rule.sampler.drop.conf.file";
    private static final Logger logger = Logger.getLogger(RuleBasedSamplerProvider.class.getName());
    private static final String name = "RuleBasedSamplerProvider";
    @Override
    public Sampler createSampler(ConfigProperties configProperties) {
        logger.info("Creating Sampler for " + name);
        var defaultSampler = Sampler.parentBased(Sampler.alwaysOn());
        var dropRules = readDropRulesFromYaml();
        var samplersBySpanKind = new HashMap<SpanKind, Sampler>();

        // Create samplers for each SpanKind
        dropRules.forEach((spanKind, rules) -> {
            var builder = RuleBasedRoutingSampler.builder(spanKind, defaultSampler);
            rules.forEach(attributes ->
                    attributes.forEach(builder::drop)
            );
            samplersBySpanKind.put(spanKind, builder.build());
        });

        // Return a sampler that falls back to the default sampler
        return new Sampler() {
            @Override
            public SamplingResult shouldSample(Context context, String s, String s1, SpanKind spanKind, Attributes attributes, List<LinkData> list) {
                Sampler sampler = samplersBySpanKind.getOrDefault(spanKind, defaultSampler);
                return sampler.shouldSample(context, s, s1, spanKind, attributes, list);
            }

            @Override
            public String getDescription() {
                return "CustomCompositeSampler";
            }
        };
    }

    @Override
    public String getName() {
        return name;
    }


    public Map<SpanKind, List<Map<AttributeKey<String>, String>>> readDropRulesFromYaml() {
        String yamlFile = System.getProperty(ENV_RULE_SAMPLER_DROP_YAML_FILE);

        if (yamlFile == null || yamlFile.trim().isEmpty()) {
            yamlFile = System.getenv(ENV_RULE_SAMPLER_DROP_YAML_FILE.replace('.', '_').toUpperCase());
        }

        if (yamlFile == null || yamlFile.trim().isEmpty()) {
            logger.warning("Neither system property nor environment variable " + ENV_RULE_SAMPLER_DROP_YAML_FILE + " is set.");
            return Map.of();
        }
        return new DropRuleConfigReader().readDropRulesFromYaml(Paths.get(yamlFile));
    }
}
