package com.devaar.telegrambot.controller;

import com.devaar.telegrambot.bot.TelegramBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RestController
@RequestMapping("/telegram/message")
@RequiredArgsConstructor
public class MessageController {
    private final TelegramBotService botService;

    // 📌 1. Kullanıcıya metin mesajı gönderme
    @PostMapping("/send")
    public String sendMessage(@RequestParam String chatId, @RequestParam String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            botService.execute(message);
            return "Mesaj başarıyla gönderildi!";
        } catch (TelegramApiException e) {
            return "Hata oluştu: " + e.getMessage();
        }
    }

    // 📌 2. Bir Telegram grubuna mesaj gönderme
    @PostMapping("/sendToGroup")
    public String sendToGroup(@RequestParam String groupId, @RequestParam String text) {
        return sendMessage(groupId, text);
    }

    // 📌 3. Fotoğraf gönderme
    @PostMapping("/sendPhoto")
    public String sendPhoto(@RequestParam String chatId, @RequestParam String photoUrl, @RequestParam String caption) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new org.telegram.telegrambots.meta.api.objects.InputFile(photoUrl));
        photo.setCaption(caption);

        try {
            botService.execute(photo);
            return "Fotoğraf başarıyla gönderildi!";
        } catch (TelegramApiException e) {
            return "Hata oluştu: " + e.getMessage();
        }
    }

    // 📌 4. Belge (PDF, TXT vb.) gönderme
    @PostMapping("/sendDocument")
    public String sendDocument(@RequestParam String chatId, @RequestParam String documentUrl) {
        SendDocument document = new SendDocument();
        document.setChatId(chatId);
        document.setDocument(new org.telegram.telegrambots.meta.api.objects.InputFile(documentUrl));

        try {
            botService.execute(document);
            return "Belge başarıyla gönderildi!";
        } catch (TelegramApiException e) {
            return "Hata oluştu: " + e.getMessage();
        }
    }

    // 📌 5. Konum gönderme
    @PostMapping("/sendLocation")
    public String sendLocation(@RequestParam String chatId, @RequestParam double latitude, @RequestParam double longitude) {
        SendLocation location = new SendLocation();
        location.setChatId(chatId);
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        try {
            botService.execute(location);
            return "Location sent succesfully";
        } catch (TelegramApiException e) {
            return "Oppss... " + e.getMessage();
        }
    }
}