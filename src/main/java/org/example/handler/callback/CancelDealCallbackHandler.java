package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.service.ExchangeProcessor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CancelDealCallbackHandler implements CallbackCommandHandler {

    private final TelegramSender telegramSender;

    @Override
    public void handle(CallbackQuery query, User user) {
        long chatId = query.getMessage().getChatId();
        String[] parts = query.getData().split(":");
        String action = parts[0];
        long   dealId;
        int    origMsg;

        // Всегда парсим dealId:
        if (parts.length >= 2) {
            dealId = Long.parseLong(parts[1]);
        } else {
            // Без dealId ничего не делать
            return;
        }


        // origMsgId может быть на позиции 2, а может и отсутствовать
        if (parts.length >= 3) {
            origMsg = Integer.parseInt(parts[2]);
        } else {
            // берём текущее messageId той кнопки, по которой кликнули
            origMsg = query.getMessage().getMessageId();
        }

        // Сразу убираем старую кнопку, чтобы не было повторных кликов
        telegramSender.editMessageReplyMarkup(
                chatId,
                query.getMessage().getMessageId(),
                InlineKeyboardMarkup.builder().build()
        );

        // Шлём подтверждение «Вы уверены?»
        InlineKeyboardButton yes = InlineKeyboardButton.builder()
                .text("Да, отменить")
                .callbackData("confirm_cancel:" + dealId + ":" + origMsg)
                .build();
        InlineKeyboardButton no = InlineKeyboardButton.builder()
                .text("Нет, оставить")
                .callbackData("deny_cancel:" + dealId + ":" + origMsg)
                .build();
        InlineKeyboardMarkup confirm = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(yes, no))
                .build();

        telegramSender.sendWithMarkup(chatId,
                "Вы уверены, что хотите отменить сделку №" + dealId + "?",
                confirm
        );
    }

    @Override
    public boolean supports(String data) {
        return data.contains("cancel_deal");
    }

}

