# tracing-hello-world

This repository contains multiple modules:

## Modules

### Spring Hello World Web Application

A simple Spring Boot web application that provides a "Hello, World!" REST endpoint.

For more details, see the [Spring Boot Hello World README](spring-boot-hello-world/README.md).

### Hello Server

A simple HTTP server that returns a "Hello, OpenTelemetry!" message with OpenTelemetry tracing.

For more details, see the [Hello Server README](hello-server/README.md).

### Start OTEL Collector

Download appropriate OpenTelemetry Collector binary for your OS from the [OpenTelemetry Collector Releases](https://github.com/open-telemetry/opentelemetry-collector-releases/releases

For example, download [this](https://github.com/open-telemetry/opentelemetry-collector-releases/releases/download/v0.124.1/otelcol-contrib_0.124.1_darwin_amd64.tar.gz)) and place it in `$PROJECT_ROOT`
```aiignore
 ./otelcol-contrib --config otel-collector-config.yaml
```
