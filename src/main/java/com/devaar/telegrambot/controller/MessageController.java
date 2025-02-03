package com.devaar.telegrambot.controller;

import com.devaar.telegrambot.bot.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RestController
@RequestMapping("/telegram/message")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final TelegramBotService botService;

    @PostMapping("/send")
    public String sendMessage(@RequestParam String chatId,
                              @RequestParam(defaultValue = "Hello!") String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            synchronized (botService) {
                botService.execute(message);
            }
            log.info("Message sent successfully to chatId: {}", chatId);
            return "Message successfully sent!";
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId: {}. Error: {}", chatId, e.getMessage(), e);
            return "Telegram API Error: " + e.getMessage();
        }
    }

    @PostMapping("/sendToGroup")
    public String sendToGroup(@RequestParam String groupId,
                              @RequestParam(defaultValue = "Hello Group!") String text) {
        return sendMessage(groupId, text);
    }

    @PostMapping("/sendPhoto")
    public String sendPhoto(@RequestParam String chatId,
                            @RequestParam String photoUrl,
                            @RequestParam(defaultValue = "Photo") String caption) {
        if (!isValidUrl(photoUrl)) {
            log.warn("Invalid photo URL provided: {}", photoUrl);
            return "Invalid photo URL!";
        }

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(photoUrl));
        photo.setCaption(caption);

        try {
            synchronized (botService) {
                botService.execute(photo);
            }
            log.info("Photo successfully sent to chatId: {}", chatId);
            return "Photo successfully sent!";
        } catch (TelegramApiException e) {
            log.error("Failed to send photo to chatId: {}. Error: {}", chatId, e.getMessage(), e);
            return "Telegram API Error: " + e.getMessage();
        }
    }

    @PostMapping("/sendDocument")
    public String sendDocument(@RequestParam String chatId,
                               @RequestParam String documentUrl) {
        if (!isValidUrl(documentUrl)) {
            log.warn("Invalid document URL provided: {}", documentUrl);
            return "Invalid document URL!";
        }

        SendDocument document = new SendDocument();
        document.setChatId(chatId);
        document.setDocument(new InputFile(documentUrl));

        try {
            synchronized (botService) {
                botService.execute(document);
            }
            log.info("Document successfully sent to chatId: {}", chatId);
            return "Document successfully sent!";
        } catch (TelegramApiException e) {
            log.error("Failed to send document to chatId: {}. Error: {}", chatId, e.getMessage(), e);
            return "Telegram API Error: " + e.getMessage();
        }
    }

    @PostMapping("/sendLocation")
    public String sendLocation(@RequestParam String chatId,
                               @RequestParam double latitude,
                               @RequestParam double longitude) {
        SendLocation location = new SendLocation();
        location.setChatId(chatId);
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        try {
            synchronized (botService) {
                botService.execute(location);
            }
            log.info("Location successfully sent to chatId: {} (Lat: {}, Long: {})", chatId, latitude, longitude);
            return "Location successfully sent!";
        } catch (TelegramApiException e) {
            log.error("Failed to send location to chatId: {}. Error: {}", chatId, e.getMessage(), e);
            return "Telegram API Error: " + e.getMessage();
        }
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }
}
