package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.service.ExchangeProcessor;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@RequiredArgsConstructor
public class ConfirmCancelCallbackHandler implements CallbackCommandHandler {

    private final ExchangeProcessor exchangeProcessor;
    private final TelegramSender telegramSender;
    private final MenuService menuService;

    @Override
    public void handle(CallbackQuery query, User user) {
        long chatId = query.getMessage().getChatId();
        String data = query.getData();
        long dealId = Long.parseLong(data.split(":")[1]);

        // Убираем inline-кнопки под сообщением с запросом
        telegramSender.editMessageReplyMarkup(
                chatId,
                query.getMessage().getMessageId(),
                InlineKeyboardMarkup.builder().build()
        );

        if (data.startsWith("confirm_cancel:")) {
            exchangeProcessor.cancel(chatId, dealId);
        } else {
            telegramSender.sendText(chatId, "Отмена отмены сделки.");
            menuService.sendMainMenu(chatId);
        }
    }

    @Override
    public boolean supports(String data) {
        return data.contains("_cancel");
    }

}

