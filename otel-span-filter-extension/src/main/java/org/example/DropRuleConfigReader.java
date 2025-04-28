package org.example;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class DropRuleConfigReader {
    private static final Logger logger = Logger.getLogger(DropRuleConfigReader.class.getName());

    /**
     * Reads the sampler-drop-config.yaml file and returns a map containing drop rules indexed by SpanKind.
     * The YAML file should have the following structure:
     * rules:
     *   drop:
     *     - spanKind: SERVER
     *       attributes:
     *         - url.path:
     *             - ^/health$
     *             - ^/metrics$
     *         - http.route:
     *             - ^/health$
     *             - ^/mad$
     *
     * @return Map<SpanKind, Map<AttributeKey<String>, Set<String>>> where the inner map contains attribute keys and sets of pattern values
     */
    Map<SpanKind, Map<AttributeKey<String>, Set<String>>> readDropRulesFromYaml(final Path yamlFile) {
        Map<SpanKind, Map<AttributeKey<String>, Set<String>>> dropRulesBySpanKind = new HashMap<>();
        try {
            // Use the DropRuleConfig.fromYaml method to create a DropRuleConfig instance
            DropRuleConfig dropRuleConfig = DropRuleConfig.fromYaml(yamlFile);

            // Convert the DropRuleConfig to the required format
            for (DropRuleConfig.DropRule rule : dropRuleConfig.getDrop()) {
                SpanKind spanKind = rule.getSpanKind();
                if (spanKind == null) {
                    logger.warning("Invalid rule: missing 'spanKind'");
                    continue;
                }

                List<Map<String, Set<String>>> attributesList = rule.getAttributes();
                if (attributesList == null || attributesList.isEmpty()) {
                    logger.warning("Invalid rule: missing or empty 'attributes'");
                    continue;
                }

                // Get or create the attribute map for this SpanKind
                Map<AttributeKey<String>, Set<String>> spanKindAttributes = dropRulesBySpanKind.computeIfAbsent(spanKind, k -> new HashMap<>());

                // Use the getAttributesAsMap method to get a flattened map of attributes
                Map<String, Set<String>> attributes = rule.getAttributesAsMap();

                // Convert String keys to AttributeKey<String> and merge the attribute patterns
                for (Map.Entry<String, Set<String>> entry : attributes.entrySet()) {
                    AttributeKey<String> attributeKey = AttributeKey.stringKey(entry.getKey());
                    spanKindAttributes.computeIfAbsent(attributeKey, k -> new HashSet<>()).addAll(entry.getValue());
                }
            }

            // Log the parsed rules
            for (Map.Entry<SpanKind, Map<AttributeKey<String>, Set<String>>> entry : dropRulesBySpanKind.entrySet()) {
                logger.info("Drop rules for SpanKind " + entry.getKey() + ":");
                logger.info("  Attributes: " + entry.getValue());
            }

            return dropRulesBySpanKind;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load YAML config file: " + e.getMessage(), e);
        }

        return dropRulesBySpanKind;
    }
}
