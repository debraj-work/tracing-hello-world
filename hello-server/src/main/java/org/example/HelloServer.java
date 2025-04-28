package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class HelloServer {
    private static final Logger logger = LoggerFactory.getLogger(HelloServer.class);
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final String REQUEST_COUNT_KEY = "hello_server:request_count";
    private static final Instant START_TIME = Instant.now();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HelloHandler());
        server.createContext("/health", new HealthHandler());
        server.createContext("/metrics", new MetricsHandler());
        server.setExecutor(null);
        server.start();
        logger.info("Server started at http://localhost:8080/");
    }

    // Lettuce Redis client implementation
    static class LettuceRedisClient {
        private static final AtomicLong requestCount = new AtomicLong(0);
        private static final io.lettuce.core.RedisClient redisClient;
        private static final StatefulRedisConnection<String, String> connection;

        static {
            try {
                RedisURI redisURI = RedisURI.builder()
                    .withHost(REDIS_HOST)
                    .withPort(REDIS_PORT)
                    .withTimeout(Duration.ofSeconds(5))
                    .build();
                redisClient = io.lettuce.core.RedisClient.create(redisURI);
                connection = redisClient.connect();
                logger.info("Connected to Redis at {}:{}", REDIS_HOST, REDIS_PORT);

                // Register shutdown hook to close resources
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    logger.info("Shutting down Redis connection");
                    if (connection != null) {
                        connection.close();
                    }
                    if (redisClient != null) {
                        redisClient.shutdown();
                    }
                }));
            } catch (Exception e) {
                logger.error("Failed to initialize Redis connection", e);
                throw new RuntimeException("Failed to initialize Redis connection", e);
            }
        }

        public static void incrementRequestCount() {
            try {
                RedisCommands<String, String> commands = connection.sync();
                String response = commands.incr(REQUEST_COUNT_KEY).toString();
                logger.info("Redis response: {}", response);
            } catch (Exception e) {
                logger.error("Error connecting to Redis", e);
                // Fallback to local counter if Redis is unavailable
                requestCount.incrementAndGet();
            }
        }
    }

    static class HelloHandler implements HttpHandler {
        @WithSpan
        public void handle(HttpExchange exchange) throws IOException {
            // Increment request count in Redis
            LettuceRedisClient.incrementRequestCount();

            String response = "Hello, OpenTelemetry!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            logger.info("Returning Response");
            os.write(response.getBytes());
            os.close();
        }
    }

    static class HealthHandler implements HttpHandler {
        @WithSpan
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"UP\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            logger.info("Health check requested");
            os.write(response.getBytes());
            os.close();
        }
    }

    static class MetricsHandler implements HttpHandler {
        @WithSpan
        public void handle(HttpExchange exchange) throws IOException {
            long uptimeSeconds = Instant.now().getEpochSecond() - START_TIME.getEpochSecond();
            String response = "{\"uptime\":" + uptimeSeconds + "}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            logger.info("Metrics requested");
            os.write(response.getBytes());
            os.close();
        }
    }
}
