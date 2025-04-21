package org.example.handler.state;

import lombok.RequiredArgsConstructor;
import org.example.infra.TelegramSender;
import org.example.model.User;
import org.example.model.enums.DealType;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.ui.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class AwaitingCityNameHandler implements UserStateHandler {

    private final UserService userService;
    private final TelegramSender telegramSender;
    private final MenuService menuService;

    @Override
    public void handle(Message message, User user) {
        long chatId = message.getChatId();
        int msgId = message.getMessageId();
        String text = message.getText();

        userService.saveCityName(chatId, text);
        userService.addMessageToDel(chatId, msgId);

        DealType dealType = user.getCurrentDeal().getDealType();

        switch (dealType) {
            case TRANSPOSITION, INVOICE -> {
                userService.saveUserStatus(chatId, Status.AWAITING_FIRST_CURRENCY);
                menuService.sendSelectMultiplyCurrency(chatId, "Выберите несколько валют получения");
            }
        }
    }

    @Override
    public Status getSupportedStatus() {
        return Status.AWAITING_CITY_NAME;
    }
}

