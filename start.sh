#! /bin/bash

# Check if Redis is already running
if ! docker ps --filter "name=redis-server" --filter "status=running" | grep -q redis-server; then
  echo "Starting Redis Docker container..."
  docker run --name redis-server -p 6379:6379 -d redis:latest
else
  echo "Redis is already running."
fi

# Check if OpenTelemetry Collector is already running
if ! pgrep -f otelcol-contrib > /dev/null; then
  echo "Starting OpenTelemetry Collector..."
  mkdir -p /tmp/otel-data/
  ./otelcol-contrib --config otel-collector-config.yaml &
else
  echo "OpenTelemetry Collector is already running."
fi