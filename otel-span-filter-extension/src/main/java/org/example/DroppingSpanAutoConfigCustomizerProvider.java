package org.example;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DroppingSpanAutoConfigCustomizerProvider implements AutoConfigurationCustomizerProvider {
    private static final Logger logger = java.util.logging.Logger.getLogger(DroppingSpanAutoConfigCustomizerProvider.class.getName());
    private static final String CONFIG_DROP_ATTR_PREFIX = "my.otel.drop.attribute.prefix";
    @Override
    public void customize(AutoConfigurationCustomizer autoConfigurationCustomizer) {
        autoConfigurationCustomizer.addSpanProcessorCustomizer((spanProcessor, config) -> {
            // Read the prefix configuration property
            String attributeKeyPrefix = config.getString(CONFIG_DROP_ATTR_PREFIX);
            return new DroppingSpanProcessor(spanProcessor, "HiTest");
//            if (attributeKeyPrefix != null && !attributeKeyPrefix.isEmpty()) {
//                logger.log(Level.INFO, "Adding ConditionalDroppingSpanProcessor (prefix mode) via extension.");
//                // Wrap the original spanProcessor (likely BatchSpanProcessor)
//                // with our custom one, passing the prefix.
//                return new DroppingSpanProcessor(spanProcessor, attributeKeyPrefix);
//            } else {
//                logger.log(Level.WARNING, String.format("Configuration property '%s' not set or is empty. ConditionalDroppingSpanProcessor disabled.", CONFIG_DROP_ATTR_PREFIX));
//                // Return the original processor if config is missing
//                return spanProcessor;
//            }
        });
        logger.log(Level.INFO, "Registered SpanProcessor customizer for ConditionalDroppingSpanProcessor (prefix mode).");
    }

    @Override
    public int order() {
        // Adjust order if needed relative to other customizers. Default is 0.
        // Lower numbers run first. Run early to drop before others process.
        return -10;
    }
}
