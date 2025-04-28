package org.example;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

public class RuleBasedSamplerProvider implements ConfigurableSamplerProvider {
    private static final String ENV_RULE_SAMPLER_DROP_PROPS_FILE = "rule.sampler.drop.props.file";
    private static final Logger logger = Logger.getLogger(RuleBasedSamplerProvider.class.getName());
    private static final String name = "RuleBasedSamplerProvider";
    @Override
    public Sampler createSampler(ConfigProperties configProperties) {
        logger.info("Creating Sampler for " + name);
        var props = dropProps();
        var builder = RuleBasedRoutingSampler.builder(SpanKind.SERVER, Sampler.parentBased(Sampler.alwaysOn()));
        props.forEach((key, value) ->
                builder.drop(AttributeKey.stringKey((String) key), (String) value)
        );
        return builder.build();
    }

    private Properties dropProps() {
        var props = new Properties();
        var propsFile = System.getProperty(ENV_RULE_SAMPLER_DROP_PROPS_FILE);
        if (propsFile == null || propsFile.trim().isEmpty()) {
            propsFile = System.getenv(ENV_RULE_SAMPLER_DROP_PROPS_FILE.replace('.', '_').toUpperCase());
        }
        if (propsFile == null || propsFile.trim().isEmpty()) {
            logger.warning("Neither system property nor environment variable " + ENV_RULE_SAMPLER_DROP_PROPS_FILE + " is set.");
            return props;
        }
        try (var reader = Files.newBufferedReader(Paths.get(propsFile))) {
            props.load(reader);
            props.forEach((key, value) -> logger.info(key + " = " + value));
            return props;
        } catch (Exception e) {
            logger.warning("Failed to load config file: " + e.getMessage());
        }
        return props;
    }

    @Override
    public String getName() {
        return name;
    }
}
