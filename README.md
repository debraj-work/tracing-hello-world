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

```aiignore
 ./otelcol-contrib --config otel-collector-config.yaml
```
