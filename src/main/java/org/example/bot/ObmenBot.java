package org.example.bot;

import org.example.interfaces.TelegramClient;
import org.example.service.UpdateRouter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ObmenBot extends TelegramLongPollingBot implements TelegramClient {
    private final UpdateRouter updateRouter;

    public ObmenBot(UpdateRouter updateRouter) {
        this.updateRouter = updateRouter;
    }

    @Override
    public String getBotUsername() {
        return "obmen_bot";
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateRouter.route(update);
    }

    @Override
    public void deleteMessage(DeleteMessage message) throws TelegramApiException {
        execute(message);
    }
}
