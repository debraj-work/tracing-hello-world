package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.opentelemetry.api.trace.SpanKind;
import lombok.Data;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DropRuleConfig {
    private static final Logger logger = Logger.getLogger(DropRuleConfig.class.getName());
    private List<DropRule> drop;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DropRule {
        private SpanKind spanKind;
        private List<Map<String, Set<String>>> attributes;

        /**
         * Converts the list of attribute maps to a single map for easier processing.
         * 
         * @return A map containing all attribute keys and their pattern values
         */
        public Map<String, Set<String>> getAttributesAsMap() {
            val result = new HashMap<String, Set<String>>();
            if (attributes == null) {
                return result;
            }

            for (val attributeMap : attributes) {
                result.putAll(attributeMap);
            }

            return result;
        }
    }

    /**
     * Creates a DropRuleConfig from a YAML file.
     * 
     * @param yamlPath The path to the YAML file
     * @return A new DropRuleConfig instance
     */
    public static DropRuleConfig fromYaml(Path yamlPath) {
        try (val inputStream = Files.newInputStream(yamlPath)) {
            val mapper = new ObjectMapper(new YAMLFactory());

            // Create a wrapper class to match the YAML structure
            val rulesWrapper = mapper.readValue(inputStream, RulesWrapper.class);

            if (rulesWrapper == null || rulesWrapper.getRules() == null) {
                logger.warning("YAML file does not contain 'rules' section");
                return new DropRuleConfig();
            }

            return rulesWrapper.getRules();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to parse YAML file: " + e.getMessage(), e);
            return new DropRuleConfig();
        }
    }

    /**
     * Wrapper class to match the YAML structure with a 'rules' section.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RulesWrapper {
        private DropRuleConfig rules;
    }
}
