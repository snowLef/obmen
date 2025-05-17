package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.model.enums.ChangeBalanceType;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class ChangeBalanceTypeHandler implements UserStateHandler {

    private final TelegramSender telegramSender;
    private final UserService userService;
    private final MenuService menuService;

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        String input = message.getText();
        int msgId = message.getMessageId();
        ChangeBalanceType type = null;

        switch (input) {
            case "Пополнение" -> type = ChangeBalanceType.ADD;
            case "Вывод" -> type = ChangeBalanceType.WITHDRAWAL;
        }

        if (user.getStatus().equals(Status.AWAITING_CHANGE_BALANCE_TYPE)) {
            user.getCurrentDeal().setChangeBalanceType(type);
        }
        user.pushStatus(Status.AWAITING_FIRST_CURRENCY);
        userService.save(user);
        userService.addMessageToDel(chatId, msgId);
        menuService.sendSelectFullCurrency(chatId, "Выберите валюту получения");
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_CHANGE_BALANCE_TYPE;
    }
}
