package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HelloServer {
    private static final Logger logger = LoggerFactory.getLogger(HelloServer.class);
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HelloHandler());
        server.setExecutor(null);
        server.start();
        logger.info("Server started at http://localhost:8080/");
    }

    static class HelloHandler implements HttpHandler {
        @WithSpan
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Hello, OpenTelemetry!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            logger.info("Returning Response");
            os.write(response.getBytes());
            os.close();
        }
    }
}