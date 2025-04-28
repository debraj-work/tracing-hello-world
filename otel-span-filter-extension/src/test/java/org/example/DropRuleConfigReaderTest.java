package org.example;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DropRuleConfigReaderTest {

    @Test
    public void testReadDropRulesFromYaml() {
        // Arrange
        DropRuleConfigReader reader = new DropRuleConfigReader();
        Path yamlPath = Paths.get("src/test/resources/sampler-drop-config.yaml");

        // Act
        Map<SpanKind, Map<AttributeKey<String>, Set<String>>> dropRules = reader.readDropRulesFromYaml(yamlPath);

        // Assert
        assertNotNull(dropRules, "Drop rules should not be null");
        assertEquals(2, dropRules.size(), "Should have rules for 2 span kinds");

        // Verify SERVER rules
        assertTrue(dropRules.containsKey(SpanKind.SERVER), "Should have rules for SERVER span kind");
        Map<AttributeKey<String>, Set<String>> serverRules = dropRules.get(SpanKind.SERVER);
        assertEquals(2, serverRules.size(), "SERVER should have 2 attribute rules");

        // Verify url.path attribute
        AttributeKey<String> urlPathKey = AttributeKey.stringKey("url.path");
        assertTrue(serverRules.containsKey(urlPathKey), "SERVER should have url.path attribute");
        Set<String> urlPathPatterns = serverRules.get(urlPathKey);
        assertEquals(2, urlPathPatterns.size(), "url.path should have 2 patterns");
        assertTrue(urlPathPatterns.contains("^/health$"), "url.path should contain ^/health$ pattern");
        assertTrue(urlPathPatterns.contains("^/metrics$"), "url.path should contain ^/metrics$ pattern");

        // Verify http.route attribute
        AttributeKey<String> httpRouteKey = AttributeKey.stringKey("http.route");
        assertTrue(serverRules.containsKey(httpRouteKey), "SERVER should have http.route attribute");
        Set<String> httpRoutePatterns = serverRules.get(httpRouteKey);
        assertEquals(2, httpRoutePatterns.size(), "http.route should have 2 patterns");
        assertTrue(httpRoutePatterns.contains("^/health$"), "http.route should contain ^/health$ pattern");
        assertTrue(httpRoutePatterns.contains("^/mad$"), "http.route should contain ^/mad$ pattern");

        // Verify CLIENT rules
        assertTrue(dropRules.containsKey(SpanKind.CLIENT), "Should have rules for CLIENT span kind");
        Map<AttributeKey<String>, Set<String>> clientRules = dropRules.get(SpanKind.CLIENT);
        assertEquals(2, clientRules.size(), "CLIENT should have 2 attribute rules");

        // Verify bac.def attribute
        AttributeKey<String> bacDefKey = AttributeKey.stringKey("bac.def");
        assertTrue(clientRules.containsKey(bacDefKey), "CLIENT should have bac.def attribute");
        Set<String> bacDefPatterns = clientRules.get(bacDefKey);
        assertEquals(2, bacDefPatterns.size(), "bac.def should have 2 patterns");
        assertTrue(bacDefPatterns.contains("^/health$"), "bac.def should contain ^/health$ pattern");
        assertTrue(bacDefPatterns.contains("^/metrics$"), "bac.def should contain ^/metrics$ pattern");

        // Verify lm.rd attribute
        AttributeKey<String> lmRdKey = AttributeKey.stringKey("lm.rd");
        assertTrue(clientRules.containsKey(lmRdKey), "CLIENT should have lm.rd attribute");
        Set<String> lmRdPatterns = clientRules.get(lmRdKey);
        assertEquals(2, lmRdPatterns.size(), "lm.rd should have 2 patterns");
        assertTrue(lmRdPatterns.contains("^/rod$"), "lm.rd should contain ^/rod$ pattern");
        assertTrue(lmRdPatterns.contains("^/cad$"), "lm.rd should contain ^/cad$ pattern");
    }
}
