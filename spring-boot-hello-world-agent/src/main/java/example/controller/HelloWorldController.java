package example.controller;

import example.service.MessageService;
import example.utils.CustomTracer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class HelloWorldController {

    private final MessageService messageService;
    private final CustomTracer tracer;

    @GetMapping("/hello")
    public String sendGreetings() {
        log.info("Received request");
        return getGreetings();
    }

    private String getGreetings() {
        return tracer.inSpan("getGreetings", () -> messageService.getMessage());
    }
}