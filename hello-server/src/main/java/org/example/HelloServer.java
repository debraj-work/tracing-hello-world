package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

public class HelloServer {
    private static final Logger logger = LoggerFactory.getLogger(HelloServer.class);
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final String REQUEST_COUNT_KEY = "hello_server:request_count";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HelloHandler());
        server.setExecutor(null);
        server.start();
        logger.info("Server started at http://localhost:8080/");
    }

    // Simple Redis client implementation
    static class RedisClient {
        private static final AtomicLong requestCount = new AtomicLong(0);

        public static void incrementRequestCount() {
            try (Socket socket = new Socket(REDIS_HOST, REDIS_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Send INCR command to Redis
                out.println("*2");
                out.println("$4");
                out.println("INCR");
                out.println("$" + REQUEST_COUNT_KEY.length());
                out.println(REQUEST_COUNT_KEY);

                // Read response (optional)
                String response = in.readLine();
                logger.info("Redis response: " + response);

            } catch (IOException e) {
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
            RedisClient.incrementRequestCount();

            String response = "Hello, OpenTelemetry!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            logger.info("Returning Response");
            os.write(response.getBytes());
            os.close();
        }
    }
}
