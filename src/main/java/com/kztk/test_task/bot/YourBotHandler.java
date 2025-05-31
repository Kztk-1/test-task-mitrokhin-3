package com.kztk.test_task.bot;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collections;

@Component
public class YourBotHandler extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final String webAppUrl = "https://test-task-mitrokhin-3-production.up.railway.app/webapp.html";

    // Используем аннотацию @Value для инъекции значений из properties
    public YourBotHandler(
            @Value("${BOT_TOKEN}") String botToken,
            @Value("${BOT_USERNAME}") String botUsername) {
        super(botToken); // Важно: передаем токен в родительский конструктор
        this.botToken = botToken;
        this.botUsername = botUsername;
    }

    @PostConstruct
    public void init() {
        System.out.println("Bot initialized with username: " + botUsername);
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if ("/start".equals(messageText)) {
                sendWebAppButton(chatId);
            }
        }
    }

    private void sendWebAppButton(long chatId) {
        InlineKeyboardButton webAppButton = new InlineKeyboardButton();
        webAppButton.setText("Send My Data");
        webAppButton.setWebApp(new WebAppInfo(webAppUrl));

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(Collections.singletonList(Collections.singletonList(webAppButton)));

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Click the button to send your Telegram data:");
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}