package com.kztk.test_task.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TelegramController {

    @PostMapping("/webhook")
    public void handleWebAppData(
            @RequestBody(required = false) Map<String, Object> data,
            @RequestParam Map<String, String> allParams) {

        System.out.println("\n=== Received Telegram WebApp Data ===");

        // Печатаем параметры запроса (query parameters)
        System.out.println("QUERY PARAMETERS:");
        allParams.forEach((key, value) ->
                System.out.println(key + " = " + value)
        );

        // Печатаем тело запроса (JSON данные)
        System.out.println("\nREQUEST BODY:");
        if (data != null) {
            data.forEach((key, value) ->
                    System.out.println(key + " = " + value)
            );
        } else {
            System.out.println("No request body received");
        }

        System.out.println("=== End of Data ===\n");
    }
}