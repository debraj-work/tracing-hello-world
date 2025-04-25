package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.service.MessageService;
import org.example.utils.CustomTracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class HelloWorldController {

    private final MessageService messageService;
    private final CustomTracer tracer;

    @GetMapping("/hello")
    public String sendGreetings() {
        return getGreetings();
    }

    private String getGreetings() {
        return tracer.inSpan("getGreetings", () -> messageService.getMessage());
    }
}