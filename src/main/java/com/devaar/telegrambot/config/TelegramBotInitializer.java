package com.devaar.telegrambot.config;

import com.devaar.telegrambot.bot.TelegramBotService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotInitializer {

    private static boolean botRegistered = false;
    private static TelegramBotsApi telegramBotsApi;

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService bot) throws TelegramApiException {
        synchronized (this) {
            if (botRegistered) {
                return telegramBotsApi;
            }


            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            botRegistered = true;

            return telegramBotsApi;
        }
    }
}
