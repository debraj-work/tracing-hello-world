package org.example;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.util.logging.Logger;

// https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/
// https://opentelemetry.io/docs/languages/java/configuration/
public class CustomSamplerProvider implements ConfigurableSamplerProvider {
    private static final String name = CustomSamplerProvider.class.getSimpleName();
    private static final Logger logger = java.util.logging.Logger.getLogger(CustomSamplerProvider.class.getName());
    @Override
    public Sampler createSampler(ConfigProperties config) {
        logger.finer("Creating Sampler for " + name);
        // Get the user-specified sampler name and argument
        // https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/
        String samplerName = config.getString("otel.traces.sampler", SamplerType.PARENT_BASED_ALWAYS_ON.name());
        String samplerArg = config.getString("otel.traces.sampler.arg", "0.05");
        logger.info("Using " + samplerName + " with argument " + samplerArg);
        System.out.println("Using " + samplerName + " with argument " + samplerArg);

        // Example: Configure a default parent-based trace ID ratio sampler to delegate to
        // which is the default
        return new CustomSampler(Sampler.parentBased(Sampler.alwaysOn()));
    }

    @Override
    public String getName() {
        return name;
    }

    private Sampler buildSampler(final SamplerType samplerType, final String parseRatio) {
        switch (samplerType) {
            case ALWAYS_ON:
                return Sampler.alwaysOn();
            case ALWAYS_OFF:
                return Sampler.alwaysOff();
            case TRACE_ID_RATIO:
                return Sampler.traceIdRatioBased(parseRatio(parseRatio));
            case PARENT_BASED_ALWAYS_ON:
                return Sampler.parentBased(Sampler.alwaysOn());
            case PARENT_BASED_ALWAYS_OFF:
                return Sampler.parentBased(Sampler.alwaysOff());
            case PARENT_BASED_TRACE_ID_RATIO:
                return Sampler.parentBased(Sampler.traceIdRatioBased(parseRatio(parseRatio)));
            default:
                return Sampler.parentBased(Sampler.alwaysOn());
        }
    }

    private double parseRatio(String arg) {
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            return 0.05;
        }
    }

    // https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/
    // https://opentelemetry.io/docs/languages/java/configuration/
    private enum SamplerType {
        ALWAYS_ON("always_on"),
        ALWAYS_OFF("always_off"),
        TRACE_ID_RATIO("traceidratio"),
        PARENT_BASED_ALWAYS_ON("parentbased_always_on"),
        PARENT_BASED_ALWAYS_OFF("parentbased_always_off"),
        PARENT_BASED_TRACE_ID_RATIO("parentbased_traceidratio");

        private final String name;

        SamplerType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        private static SamplerType fromString(String name) {
            for (SamplerType type : SamplerType.values()) {
                if (type.getName().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            logger.warning("Unsupported sampler type: " + name);
            return SamplerType.PARENT_BASED_ALWAYS_ON;
        }
    }
}
