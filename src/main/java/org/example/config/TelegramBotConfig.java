package org.example.config;

import org.example.bot.ObmenBot;
import org.example.interfaces.TelegramClient;
import org.example.service.UpdateRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

    @Bean
    public ObmenBot obmenBot(UpdateRouter updateRouter) {
        return new ObmenBot(updateRouter);
    }

    @Bean
    public TelegramClient telegramClient(ObmenBot obmenBot) {
        return obmenBot;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(ObmenBot obmenBot) throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(obmenBot);
        return api;
    }
}