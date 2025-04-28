package org.example;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
     *     - span_kind: SERVER
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
        try (var reader = Files.newBufferedReader(yamlFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(reader);

            if (config == null || !config.containsKey("rules") || !(config.get("rules") instanceof Map)) {
                logger.warning("Invalid YAML configuration: 'rules' section is missing or not a map");
                return dropRulesBySpanKind;
            }

            Map<String, Object> rules = (Map<String, Object>) config.get("rules");

            if (!rules.containsKey("drop") || !(rules.get("drop") instanceof List)) {
                logger.warning("Invalid YAML configuration: 'rules.drop' section is missing or not a list");
                return dropRulesBySpanKind;
            }

            List<Map<String, Object>> dropRules = (List<Map<String, Object>>) rules.get("drop");

            for (Map<String, Object> rule : dropRules) {
                if (!rule.containsKey("span_kind") || !rule.containsKey("attributes")) {
                    logger.warning("Invalid rule: missing 'span_kind' or 'attributes'");
                    continue;
                }

                String spanKindStr = (String) rule.get("span_kind");
                SpanKind spanKind;
                try {
                    spanKind = SpanKind.valueOf(spanKindStr);
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid span_kind: " + spanKindStr);
                    continue;
                }

                List<Map<String, Object>> attributesList = (List<Map<String, Object>>) rule.get("attributes");
                Map<AttributeKey<String>, Set<String>> attributePatterns = new HashMap<>();

                for (Map<String, Object> attributeEntry : attributesList) {
                    for (Map.Entry<String, Object> entry : attributeEntry.entrySet()) {
                        String attributeKey = entry.getKey();
                        List<String> patterns = (List<String>) entry.getValue();
                        Set<String> patternSet = new HashSet<>(patterns);
                        attributePatterns.put(AttributeKey.stringKey(attributeKey), patternSet);
                    }
                }

                // Get or create the attribute map for this SpanKind
                Map<AttributeKey<String>, Set<String>> spanKindAttributes = dropRulesBySpanKind.computeIfAbsent(spanKind, k -> new HashMap<>());

                // Merge the attribute patterns into the map
                for (Map.Entry<AttributeKey<String>, Set<String>> entry : attributePatterns.entrySet()) {
                    spanKindAttributes.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).addAll(entry.getValue());
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
