package example.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.stereotype.Component;

@Component
public class MessageService {
    @WithSpan
    public String getMessage() {
        return "Hello, World!";
    }
}
