package com.devaar.telegrambot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String receivedText = update.getMessage().getText();

            log.info("Received message: {}", receivedText);

            String responseText = switch (receivedText.toLowerCase()) {
                case "/start" ->
                        "Merhaba! Ben bir Telegram botuyum.Beni Amil yaratdi(subheli) /help komutunu kullanarak tüm özellikleri görebilirsiniz.";
                case "/help" -> """
                        📌 Komut Listesi:
                        - /start → Hoş geldin mesajı
                        - /info → Bot hakkında bilgi
                        - /time → Şu anki saat
                        - /date → Bugünün tarihi
                        - /weather <şehir> → Hava durumu
                        - /joke → Rastgele şaka
                        - /quote → Motivasyon sözü
                        - /news → Günlük haberler
                        - /fact → İlginç bilgi
                        - /calculate <işlem> → Matematik işlemi
                        - /poll <soru>? <cevap1> <cevap2> ... → Anket başlat
                        """;
                case "/info" ->
                        "Benim adım DevAar Bot. Java ve Spring Boot kullanılarak geliştirilmiş bir Telegram botuyum!";
                case "/time" -> "Şu anki saat: " + java.time.LocalTime.now();
                case "/date" -> "Bugünün tarihi: " + java.time.LocalDate.now();
                case "/joke" ->
                        "😂 Rastgele şaka: 'Neden bilgisayarlar asla aç kalmaz? Çünkü hep bir şeyler atıştırırlar!' ";
                case "/quote" -> "📜 Motivasyon Sözü: 'Başarı, sürekli tekrar edilen küçük çabaların toplamıdır.'";
                case "/fact" ->
                        "🧠 İlginç Bilgi: 'Dünyadaki tüm karıncaların toplam ağırlığı, tüm insanların toplam ağırlığına yakındır!'";
                default -> "Üzgünüm, bu komutu tanımıyorum. 🤖 /help yazarak tüm komutları görebilirsiniz.";
            };

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(responseText);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Oppss...", e);
            }
        }
    }
}
