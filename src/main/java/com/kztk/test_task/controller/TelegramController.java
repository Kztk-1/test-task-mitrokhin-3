package com.kztk.test_task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class TelegramController {

    private static final Logger logger = LoggerFactory.getLogger(TelegramController.class);

    @PostMapping("/webhook")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> handleWebAppData(
            HttpServletRequest request,
            @RequestBody(required = false) String rawData,
            @RequestParam Map<String, String> allParams) {

        try {
            logger.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());

            logger.info("\n=== Received Telegram WebApp Data ===");

            // Печатаем параметры запроса
            logger.info("QUERY PARAMETERS:");
            allParams.forEach((key, value) ->
                    logger.info(key + " = " + value)
            );

            // Печатаем тело запроса
            logger.info("\nREQUEST BODY:");
            if (rawData != null && !rawData.isEmpty()) {
                logger.info(rawData);

                try {
                    // Парсим JSON для красивого вывода
                    ObjectMapper mapper = new ObjectMapper();
                    Object json = mapper.readValue(rawData, Object.class);
                    String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                    logger.info("Parsed JSON:\n" + prettyJson);
                } catch (Exception e) {
                    logger.error("Error parsing JSON: " + e.getMessage());
                }
            } else {
                logger.info("No request body received");
            }

            logger.info("=== End of Data ===\n");
        } catch (Exception e) {
            logger.error("Error processing request", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }

        return null;
    }

    @RequestMapping(value = "/webhook", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .build();
    }
}