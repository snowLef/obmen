package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.model.enums.AmountType;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class AwaitingSelectAmountHandler implements UserStateHandler {

    private final UserService userService;
    private final MenuService menuService;

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        int msgId = message.getMessageId();
        String text = message.getText();

        // Дальнейшее поведение зависит от выбора: "Получаю" или "Отдаю"
        if ("Отдаю".equals(text)) {
            user.setAmountType(AmountType.GIVE);
            user.setStatus(Status.AWAITING_SECOND_CURRENCY);
            userService.save(user);
            menuService.sendSelectCurrency(chatId, "Выберите валюту получения:");
        } else if ("Получаю".equals(text)) {
            user.setAmountType(AmountType.RECEIVE);
            user.setStatus(Status.AWAITING_SECOND_CURRENCY);
            userService.save(user);
            menuService.sendSelectCurrency(chatId, "Выберите валюту отдачи:");
        } else {
            menuService.sendSelectAmountType(chatId); // повторно покажем кнопки
        }

        userService.addMessageToDel(chatId, msgId);
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_SELECT_AMOUNT;
    }
}