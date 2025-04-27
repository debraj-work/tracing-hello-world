package org.example;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSampler;
import io.opentelemetry.instrumentation.annotations.AddingSpanAttributes;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.UrlAttributes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {

    @Value("${otel.service.name}")
    private String serviceName;

    @Bean
    public AutoConfigurationCustomizerProvider otelCustomizer() {
        return p ->
                p.addSamplerCustomizer(this::configureSampler)
                        .addSpanExporterCustomizer(this::configureSpanExporter);
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName);
    }

    /** suppress spans for actuator endpoints */
    private RuleBasedRoutingSampler configureSampler(Sampler fallback, ConfigProperties config) {
        // user_agent.original = Prometheus/2.47.0  and url.path /metrics
        return RuleBasedRoutingSampler.builder(SpanKind.SERVER, fallback)
                .drop(UrlAttributes.URL_PATH, "^/actuator")
                .build();
    }

    /**
     * Configuration for the OTLP exporter. This configuration will replace the default OTLP exporter,
     * and will add a custom header to the requests.
     */
    private SpanExporter configureSpanExporter(SpanExporter exporter, ConfigProperties config) {
//        if (exporter instanceof OtlpHttpSpanExporter) {
//            return ((OtlpHttpSpanExporter) exporter).toBuilder().setHeaders(this::headers).build();
//        }
        return exporter;
    }

//    private Map<String, String> headers() {
//        return Collections.singletonMap("Authorization", "Bearer " + refreshToken());
//    }
//
//    private String refreshToken() {
//        // e.g. read the token from a kubernetes secret
//        return "token";
//    }
}