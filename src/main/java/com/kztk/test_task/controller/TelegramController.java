package com.kztk.test_task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
public class TelegramController {

    private static final Logger logger = LoggerFactory.getLogger(TelegramController.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Берём токен из application.properties
    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhook")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> handleWebAppData(
            HttpServletRequest request,
            @RequestBody(required = false) String rawData,
            @RequestParam Map<String, String> allParams) {

        try {
            logger.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());

            // Логируем входящие данные
            logger.info("\n=== Received Telegram Webhook Data ===");
            allParams.forEach((key, value) ->
                    logger.info("QUERY PARAM: {} = {}", key, value)
            );
            if (rawData != null && !rawData.isEmpty()) {
                logger.info("Raw JSON: {}", rawData);
            } else {
                logger.info("No request body");
            }
            logger.info("=== End of Data ===\n");

            // 1) Парсим Update
            com.fasterxml.jackson.databind.JsonNode updateJson = objectMapper.readTree(rawData);
            // 2) Проверяем, есть ли в Update объект "message"
            if (updateJson.has("message")) {
                com.fasterxml.jackson.databind.JsonNode messageNode = updateJson.get("message");

                // Получаем chat_id, чтобы знать, куда ответить
                long chatId = messageNode.get("chat").get("id").asLong();

                // Например, если пользователь прислал любой текст, мы отправляем ему клавиатуру
                sendKeyboard(chatId);
            }

        } catch (Exception e) {
            logger.error("Error processing request", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }

        // Telegram не нуждается в каком-либо теле ответа, просто возвращаем 200 OK
        return ResponseEntity.ok().build();
    }

    private void sendKeyboard(long chatId) {
        // Формируем URL вида: https://api.telegram.org/bot<token>/sendMessage
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        // Формируем тело запроса в виде Map (конвертируем в JSON)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chat_id", chatId);
        requestBody.put("text", "Выберите одну из кнопок:");

        // Построим объект ReplyKeyboardMarkup
        Map<String, Object> replyMarkup = new HashMap<>();
        replyMarkup.put("resize_keyboard", true);    // чтобы клавиатура сжималась
        replyMarkup.put("one_time_keyboard", true);  // чтобы скрывалась после нажатия
        // Структура потипу: {"keyboard": [[{"text": "Кнопка 1"}], [{"text": "Кнопка 2"}]] }
        List<List<Map<String, String>>> keyboard = new ArrayList<>();

        // Первая строка клавиатуры
        List<Map<String, String>> row1 = new ArrayList<>();
        Map<String, String> button1 = new HashMap<>();
        button1.put("text", "Привет");
        row1.add(button1);

        // Вторая строка клавиатуры
        List<Map<String, String>> row2 = new ArrayList<>();
        Map<String, String> button2 = new HashMap<>();
        button2.put("text", "Как дела?");
        row2.add(button2);

        keyboard.add(row1);
        keyboard.add(row2);

        replyMarkup.put("keyboard", keyboard);

        requestBody.put("reply_markup", replyMarkup);

        // Заголовки запроса
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Выполняем POST-запрос
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            logger.info("sendKeyboard: Telegram response = {}", response.getBody());
        } catch (Exception e) {
            logger.error("Ошибка при отправке keyboard:", e);
        }
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
