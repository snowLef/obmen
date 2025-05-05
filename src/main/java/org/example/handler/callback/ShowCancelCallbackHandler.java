package org.example.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.ui.InlineKeyboardBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@RequiredArgsConstructor
public class ShowCancelCallbackHandler implements CallbackCommandHandler {

    private final TelegramSender sender;

    @Override
    public boolean supports(String callback) {
        return callback.startsWith("show_cancel:");
    }

    @Override
    public void handle(CallbackQuery query, User user) {
        // callbackData = "show_cancel:<dealId>:<origMsgId>"
        long chatId = query.getMessage().getChatId();
        String[] parts = query.getData().split(":");
        long dealId  = Long.parseLong(parts[1]);
        int  origMsg = Integer.parseInt(parts[2]);

        InlineKeyboardMarkup confirmKb = InlineKeyboardBuilder.builder()
                .button("✅ Да, отменить",  "confirm_cancel:" + dealId + ":" + origMsg)
                .button("↩️ Нет, оставить", "cancel_cancel:"  + dealId + ":" + origMsg)
                .build();

        // редактируем именно оригинальное сообщение
        sender.editReplyMarkup(chatId, origMsg, confirmKb);
    }
}
